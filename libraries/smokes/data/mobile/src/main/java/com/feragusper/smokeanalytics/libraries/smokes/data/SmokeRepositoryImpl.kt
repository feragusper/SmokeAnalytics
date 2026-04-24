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
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Query.Direction
import com.google.firebase.firestore.Source
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Instant
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmokeRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val appContext: Context,
) : SmokeRepository {

    interface FirestoreCollection {
        companion object {
            const val USERS = "users"
            const val SMOKES = "smokes"
        }
    }

    override suspend fun addSmoke(date: Instant, location: GeoPoint?) {
        runFirestoreCall("add smoke", smokesPath()) {
            val document = smokesQuery().document()
            document.set(
                SmokeEntity(
                    timestampMillis = date.toEpochMilliseconds().toDouble(),
                    latitude = location?.latitude,
                    longitude = location?.longitude,
                )
            ).await()
            val serverSnapshot = document.get(Source.SERVER).await()
            check(serverSnapshot.exists()) {
                "Firestore add smoke verification failed: ${document.path} is missing on server."
            }
        }
    }

    override suspend fun editSmoke(id: String, date: Instant, location: GeoPoint?) {
        runFirestoreCall("edit smoke", "${smokesPath()}/$id") {
            val document = smokesQuery().document(id)
            val preservedLocation = location ?: document
                .get(Source.SERVER)
                .await()
                .getGeoPoint()

            document.set(
                SmokeEntity(
                    timestampMillis = date.toEpochMilliseconds().toDouble(),
                    latitude = preservedLocation?.latitude,
                    longitude = preservedLocation?.longitude,
                )
            ).await()
            val serverSnapshot = document.get(Source.SERVER).await()
            check(serverSnapshot.exists()) {
                "Firestore edit smoke verification failed: ${document.path} is missing on server."
            }
        }
    }

    override suspend fun deleteSmoke(id: String) {
        runFirestoreCall("delete smoke", "${smokesPath()}/$id") {
            val document = smokesQuery().document(id)
            document.delete().await()
            val serverSnapshot = document.get(Source.SERVER).await()
            check(!serverSnapshot.exists()) {
                "Firestore delete smoke verification failed: ${document.path} still exists on server."
            }
        }
    }

    override suspend fun fetchSmokes(
        startDate: Instant?,
        endDate: Instant?
    ): List<Smoke> {
        val startMillis = (startDate ?: firstInstantThisMonth()).toEpochMilliseconds().toDouble()
        val endMillis = (endDate ?: nextDayStartInstant()).toEpochMilliseconds().toDouble()

        val query: Query = smokesQuery()
            .orderBy(SmokeEntity.Fields.TIMESTAMP_MILLIS, Direction.DESCENDING)
            .whereGreaterThanOrEqualTo(SmokeEntity.Fields.TIMESTAMP_MILLIS, startMillis)
            .whereLessThan(SmokeEntity.Fields.TIMESTAMP_MILLIS, endMillis)

        return runFirestoreCall("fetch smokes", smokesPath()) {
            val result = query.get(Source.SERVER).await()
            val previousDocument = smokesQuery()
                .orderBy(SmokeEntity.Fields.TIMESTAMP_MILLIS, Direction.DESCENDING)
                .whereLessThan(SmokeEntity.Fields.TIMESTAMP_MILLIS, startMillis)
                .limit(1)
                .get(Source.SERVER)
                .await()
                .documents
                .firstOrNull()

            val documents = previousDocument?.let { result.documents + it } ?: result.documents
            val instants = documents.mapNotNull { it.getInstant() }

            result.documents.mapIndexedNotNull { index, document ->
                val currentInstant = instants.getOrNull(index) ?: return@mapIndexedNotNull null
                val previousInstant = instants.getOrNull(index + 1)

                Smoke(
                    id = document.id,
                    date = currentInstant,
                    timeElapsedSincePreviousSmoke = currentInstant.timeAfter(previousInstant),
                    location = document.getGeoPoint(),
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
        val millis = getDouble(SmokeEntity.Fields.TIMESTAMP_MILLIS) ?: return null
        return Instant.fromEpochMilliseconds(millis.toLong())
    }

    private fun DocumentSnapshot.getGeoPoint(): GeoPoint? {
        val latitude = getDouble(SmokeEntity.Fields.LATITUDE) ?: return null
        val longitude = getDouble(SmokeEntity.Fields.LONGITUDE) ?: return null
        return GeoPoint(latitude = latitude, longitude = longitude)
    }

    private companion object {
        const val FIRESTORE_TIMEOUT_MILLIS = 15_000L
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
