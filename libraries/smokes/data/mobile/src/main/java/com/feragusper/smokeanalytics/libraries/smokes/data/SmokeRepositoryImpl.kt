package com.feragusper.smokeanalytics.libraries.smokes.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.feragusper.smokeanalytics.libraries.architecture.domain.firstInstantThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentMonthStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentWeekStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.isInCurrentDayBucket
import com.feragusper.smokeanalytics.libraries.architecture.domain.isInCurrentMonthBucket
import com.feragusper.smokeanalytics.libraries.architecture.domain.isInCurrentWeekBucket
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeAfter
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl.FirestoreCollection.Companion.SMOKES
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl.FirestoreCollection.Companion.USERS
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeRelationship
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.noteOrNull
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.skippedFlag
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.smokeRelationshipFromFields
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.triggerKeys
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Query.Direction
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Instant
import java.security.MessageDigest

class SmokeRepositoryImpl constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val appContext: Context,
) : SmokeRepository {

    interface FirestoreCollection {
        companion object {
            const val USERS = "users"
            const val SMOKES = "smokes"
        }
    }

    // Writes are offline-first: Firestore applies them to the local persistent cache
    // immediately and syncs to the server in the background when connectivity returns.
    // We deliberately do not await the server acknowledgement, so adding/editing/deleting
    // a smoke works with no connection instead of timing out.

    override suspend fun addSmoke(date: Instant, location: GeoPoint?): String =
        runFirestoreCall("add smoke", smokesPath()) {
            val document = smokesQuery().document()
            document.set(smokePayload(date, location))
            document.id
        }

    override suspend fun setSmokeRelationship(id: String, relationship: SmokeRelationship) {
        runFirestoreCall("set smoke relationship", "${smokesPath()}/$id") {
            // Merge so the timestamp/location written when the smoke was logged are preserved.
            smokesQuery().document(id).set(relationshipPayload(relationship), SetOptions.merge())
        }
    }

    override suspend fun editSmoke(id: String, date: Instant, location: GeoPoint?) {
        runFirestoreCall("edit smoke", "${smokesPath()}/$id") {
            val document = smokesQuery().document(id)
            // Default source: server when online, local cache when offline.
            val preservedLocation = location ?: document.get().await().getGeoPoint()
            document.set(smokePayload(date, preservedLocation))
        }
    }

    override suspend fun deleteSmoke(id: String) {
        runFirestoreCall("delete smoke", "${smokesPath()}/$id") {
            smokesQuery().document(id).delete()
        }
    }

    override suspend fun fetchSmokes(
        startDate: Instant?,
        endDate: Instant?
    ): List<Smoke> {
        val startMillis = (startDate ?: firstInstantThisMonth()).toEpochMilliseconds().toDouble()
        val endMillis = (endDate ?: nextDayStartInstant()).toEpochMilliseconds().toDouble()

        return runFirestoreCall("fetch smokes", smokesPath()) {
            val canonicalDocuments = fetchSmokeQuery(SmokeEntity.Fields.TIMESTAMP_MILLIS, startMillis, endMillis)
            val legacyDocuments = fetchSmokeQuery(LegacySmokeFields.TIMESTAMP_MILLIS, startMillis, endMillis)

            val currentRecords = (canonicalDocuments.current + legacyDocuments.current)
                .mapNotNull { it.toSmokeRecord() }
                .distinctBy { it.id }
                .sortedByDescending { it.instant }
            val previousRecord = listOfNotNull(canonicalDocuments.previous, legacyDocuments.previous)
                .mapNotNull { it.toSmokeRecord() }
                .maxByOrNull { it.instant }
            val timelineInstants = currentRecords.map { it.instant } + listOfNotNull(previousRecord?.instant)

            currentRecords.mapIndexed { index, record ->
                val previousInstant = timelineInstants.getOrNull(index + 1)
                Smoke(
                    id = record.id,
                    date = record.instant,
                    timeElapsedSincePreviousSmoke = record.instant.timeAfter(previousInstant),
                    location = record.location,
                    relationship = record.relationship,
                )
            }
        }
    }

    override suspend fun fetchSmokeCount(dayStartHour: Int, manualDayStartEpochMillis: Long?): SmokeCount {
        val monthStart = currentMonthStartInstant(
            dayStartHour = dayStartHour,
            manualDayStartEpochMillis = manualDayStartEpochMillis,
        )
        val weekStart = currentWeekStartInstant(
            dayStartHour = dayStartHour,
            manualDayStartEpochMillis = manualDayStartEpochMillis,
        )
        return fetchSmokes(
            startDate = if (weekStart < monthStart) weekStart else monthStart,
            endDate = nextDayStartInstant(
                dayStartHour = dayStartHour,
                manualDayStartEpochMillis = manualDayStartEpochMillis,
            ),
        ).toSmokeCountListResult(dayStartHour, manualDayStartEpochMillis)
    }

    private fun List<Smoke>.toSmokeCountListResult(dayStartHour: Int, manualDayStartEpochMillis: Long?) = SmokeCount(
        today = filterToday(dayStartHour, manualDayStartEpochMillis),
        week = filterThisWeek(dayStartHour, manualDayStartEpochMillis).size,
        month = filterThisMonth(dayStartHour, manualDayStartEpochMillis).size,
        lastSmoke = firstOrNull(),
    )

    private fun List<Smoke>.filterToday(dayStartHour: Int, manualDayStartEpochMillis: Long?) =
        filter { it.date.isInCurrentDayBucket(dayStartHour = dayStartHour, manualDayStartEpochMillis = manualDayStartEpochMillis) }

    private fun List<Smoke>.filterThisWeek(dayStartHour: Int, manualDayStartEpochMillis: Long?) =
        filter { it.date.isInCurrentWeekBucket(dayStartHour = dayStartHour, manualDayStartEpochMillis = manualDayStartEpochMillis) }

    private fun List<Smoke>.filterThisMonth(dayStartHour: Int, manualDayStartEpochMillis: Long?) =
        filter { it.date.isInCurrentMonthBucket(dayStartHour = dayStartHour, manualDayStartEpochMillis = manualDayStartEpochMillis) }

    private fun smokesQuery() = firebaseAuth.currentUser?.uid?.let { uid ->
        firebaseFirestore.collection(smokesPath(uid))
    } ?: throw IllegalStateException("User not logged in")

    private fun smokesPath(uid: String = firebaseAuth.currentUser?.uid ?: "missing") = "$USERS/$uid/$SMOKES"

    private suspend fun fetchSmokeQuery(
        timestampField: String,
        startMillis: Double,
        endMillis: Double,
    ): SmokeDocuments {
        val result = queryByTimestamp(timestampField)
            .whereGreaterThanOrEqualTo(timestampField, startMillis)
            .whereLessThan(timestampField, endMillis)
            .get()
            .await()
        val previousDocument = queryByTimestamp(timestampField)
            .whereLessThan(timestampField, startMillis)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        return SmokeDocuments(current = result.documents, previous = previousDocument)
    }

    private fun queryByTimestamp(timestampField: String): Query =
        smokesQuery().orderBy(timestampField, Direction.DESCENDING)

    private suspend fun <T> runFirestoreCall(operation: String, path: String, block: suspend () -> T): T =
        try {
            withTimeout(FIRESTORE_TIMEOUT_MILLIS) {
                block()
            }
        } catch (e: TimeoutCancellationException) {
            throw IllegalStateException(
                "Firestore $operation timed out after ${FIRESTORE_TIMEOUT_MILLIS / 1_000}s. ${firebaseDiagnostics(path)}",
                e,
            )
        } catch (e: Exception) {
            throw IllegalStateException(
                "Firestore $operation failed: ${e.firestoreSummary()}. ${firebaseDiagnostics(path)}",
                e,
            )
        }

    private fun firebaseDiagnostics(path: String): String {
        val options = runCatching { FirebaseApp.getInstance().options }.getOrNull()
        return buildString {
            append("Firebase project=").append(options?.projectId ?: "unknown")
            append(", appId=").append(options?.applicationId ?: "unknown")
            append(", apiKey=").append((options?.apiKey).redactedApiKey())
            append(", path=").append(path)
            append(", package=").append(appContext.packageName)
            append(", installer=").append(appContext.installerPackageName())
            append(", ").append(appContext.signingDigestSummary())
            append(".")
        }
    }

    private fun DocumentSnapshot.getInstant(): Instant? {
        val millis = getDouble(SmokeEntity.Fields.TIMESTAMP_MILLIS)
            ?: getDouble(LegacySmokeFields.TIMESTAMP_MILLIS)
            ?: return null
        return Instant.fromEpochMilliseconds(millis.toLong())
    }

    private fun DocumentSnapshot.getGeoPoint(): GeoPoint? {
        val latitude = getDouble(SmokeEntity.Fields.LATITUDE) ?: getDouble(LegacySmokeFields.LATITUDE) ?: return null
        val longitude = getDouble(SmokeEntity.Fields.LONGITUDE) ?: getDouble(LegacySmokeFields.LONGITUDE) ?: return null
        return GeoPoint(latitude = latitude, longitude = longitude)
    }

    private fun DocumentSnapshot.toSmokeRecord(): SmokeRecord? {
        val instant = getInstant() ?: return null
        return SmokeRecord(
            id = id,
            instant = instant,
            location = getGeoPoint(),
            relationship = getRelationship(),
        )
    }

    private fun DocumentSnapshot.getRelationship(): SmokeRelationship {
        @Suppress("UNCHECKED_CAST")
        val triggers = get(SmokeEntity.Fields.TRIGGERS) as? List<String>
        return smokeRelationshipFromFields(
            triggers = triggers,
            note = getString(SmokeEntity.Fields.TRIGGER_NOTE),
            skipped = getBoolean(SmokeEntity.Fields.RELATIONSHIP_SKIPPED),
        )
    }

    private fun smokePayload(date: Instant, location: GeoPoint?): Map<String, Any?> =
        mapOf(
            SmokeEntity.Fields.TIMESTAMP_MILLIS to date.toEpochMilliseconds().toDouble(),
            SmokeEntity.Fields.LATITUDE to location?.latitude,
            SmokeEntity.Fields.LONGITUDE to location?.longitude,
        )

    private fun relationshipPayload(relationship: SmokeRelationship): Map<String, Any?> =
        mapOf(
            SmokeEntity.Fields.TRIGGERS to relationship.triggerKeys(),
            SmokeEntity.Fields.TRIGGER_NOTE to relationship.noteOrNull(),
            SmokeEntity.Fields.RELATIONSHIP_SKIPPED to relationship.skippedFlag(),
        )

    private companion object {
        const val FIRESTORE_TIMEOUT_MILLIS = 15_000L
    }

    private data class SmokeDocuments(
        val current: List<DocumentSnapshot>,
        val previous: DocumentSnapshot?,
    )

    private data class SmokeRecord(
        val id: String,
        val instant: Instant,
        val location: GeoPoint?,
        val relationship: SmokeRelationship = SmokeRelationship.Untracked,
    )

    private object LegacySmokeFields {
        const val TIMESTAMP_MILLIS = "a"
        const val LATITUDE = "b"
        const val LONGITUDE = "c"
    }
}

private fun Throwable.firestoreSummary(): String {
    val firestoreException = (this as? FirebaseFirestoreException) ?: (cause as? FirebaseFirestoreException)
    val type = this::class.simpleName ?: "Throwable"
    val code = firestoreException?.code?.name?.let { " code=$it" }.orEmpty()
    val message = message?.takeIf { it.isNotBlank() } ?: cause?.message?.takeIf { it.isNotBlank() }
    return buildString {
        append(type).append(code)
        if (message != null) append(": ").append(message)
    }
}

private fun String?.redactedApiKey(): String =
    when {
        isNullOrBlank() -> "unknown"
        length <= 8 -> "configured"
        else -> "sha256:${sha256().take(12)},suffix:${takeLast(6)}"
    }

private fun String.sha256(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(toByteArray())
        .toHex()

private fun Context.installerPackageName(): String =
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.getInstallSourceInfo(packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstallerPackageName(packageName)
        }
    }.getOrNull() ?: "unknown"

private fun Context.signingDigestSummary(): String =
    signingCertificateBytes()
        .firstOrNull()
        ?.let { bytes ->
            "runtimeSha1=${bytes.digest("SHA-1")},runtimeSha256=${bytes.digest("SHA-256")}"
        }
        ?: "runtimeSha1=unknown,runtimeSha256=unknown"

@Suppress("DEPRECATION")
private fun Context.signingCertificateBytes(): List<ByteArray> =
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager
                .getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                .signingInfo
                ?.let { signingInfo ->
                    if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                }
                .orEmpty()
                .map { it.toByteArray() }
        } else {
            packageManager
                .getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                .signatures
                .orEmpty()
                .map { it.toByteArray() }
        }
    }.getOrDefault(emptyList())

private fun ByteArray.digest(algorithm: String): String =
    MessageDigest.getInstance(algorithm)
        .digest(this)
        .toHex()

private fun ByteArray.toHex(): String =
    joinToString(separator = "") { "%02x".format(it) }
