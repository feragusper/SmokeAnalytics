package com.feragusper.smokeanalytics.libraries.preferences.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferencesRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @ApplicationContext private val appContext: Context,
) : UserPreferencesRepository {

    override suspend fun fetch(): UserPreferences {
        val snapshot = runFirestoreProfileCall("fetch preferences") {
            document().get(Source.SERVER).await()
        }
        return snapshot.toUserPreferencesEntity()?.toDomain() ?: UserPreferences()
    }

    override suspend fun update(preferences: UserPreferences) {
        runFirestoreProfileCall("update preferences") {
            document().set(
                UserPreferencesEntity(
                    packPrice = preferences.packPrice,
                    cigarettesPerPack = preferences.cigarettesPerPack.toLong(),
                    dayStartHour = preferences.dayStartHour.toLong(),
                    bedtimeHour = preferences.bedtimeHour.toLong(),
                    manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                    locationTrackingEnabled = preferences.locationTrackingEnabled,
                    currencySymbol = preferences.currencySymbol,
                    accountTier = preferences.accountTier.name,
                    activeGoalType = preferences.activeGoal?.type?.name,
                    activeGoalMetricValue = preferences.activeGoal?.metricValue,
                )
            )
                .await()
            val serverSnapshot = document().get(Source.SERVER).await()
            check(serverSnapshot.exists()) {
                "Firestore update preferences verification failed: ${document().path} is missing on server."
            }
        }
    }

    private fun document() = firestore.collection("users")
        .document(auth.currentUser?.uid ?: throw IllegalStateException("User not logged in"))
        .collection("profile")
        .document(UserPreferencesEntity.DOCUMENT)

    private suspend fun <T> runFirestoreProfileCall(operation: String, block: suspend () -> T): T =
        try {
            withTimeout(FIRESTORE_PROFILE_TIMEOUT_MILLIS) {
                block()
            }
        } catch (e: TimeoutCancellationException) {
            throw IllegalStateException(
                "Firestore $operation timed out after ${FIRESTORE_PROFILE_TIMEOUT_MILLIS / 1_000}s. ${firebaseDiagnostics()}",
                e,
            )
        } catch (e: Exception) {
            throw IllegalStateException(
                "Firestore $operation failed: ${e.firestoreSummary()}. ${firebaseDiagnostics()}",
                e,
            )
        }

    private fun firebaseDiagnostics(): String {
        val options = runCatching { FirebaseApp.getInstance().options }.getOrNull()
        return buildString {
            append("Firebase project=").append(options?.projectId ?: "unknown")
            append(", appId=").append(options?.applicationId ?: "unknown")
            append(", apiKey=").append((options?.apiKey).redactedApiKey())
            append(", path=users/").append(auth.currentUser?.uid ?: "missing")
            append("/profile/").append(UserPreferencesEntity.DOCUMENT)
            append(", package=").append(appContext.packageName)
            append(", installer=").append(appContext.installerPackageName())
            append(", ").append(appContext.signingDigestSummary())
            append(".")
        }
    }

    private companion object {
        const val FIRESTORE_PROFILE_TIMEOUT_MILLIS = 15_000L
    }
}

private fun DocumentSnapshot.toUserPreferencesEntity(): UserPreferencesEntity? {
    if (!exists()) return null

    return UserPreferencesEntity(
        packPrice = numberOrNull(UserPreferencesEntity.PACK_PRICE)?.toDouble() ?: 0.0,
        cigarettesPerPack = numberOrNull(UserPreferencesEntity.CIGARETTES_PER_PACK)?.toLong() ?: 20,
        dayStartHour = numberOrNull(UserPreferencesEntity.DAY_START_HOUR)?.toLong() ?: 6,
        bedtimeHour = numberOrNull(UserPreferencesEntity.BEDTIME_HOUR)?.toLong() ?: 22,
        manualDayStartEpochMillis = numberOrNull(UserPreferencesEntity.MANUAL_DAY_START_EPOCH_MILLIS)?.toLong(),
        locationTrackingEnabled = booleanOrNull(UserPreferencesEntity.LOCATION_TRACKING_ENABLED) ?: false,
        currencySymbol = stringOrNull(UserPreferencesEntity.CURRENCY_SYMBOL) ?: "€",
        accountTier = stringOrNull(UserPreferencesEntity.ACCOUNT_TIER) ?: "Free",
        activeGoalType = stringOrNull(UserPreferencesEntity.ACTIVE_GOAL_TYPE),
        activeGoalMetricValue = numberOrNull(UserPreferencesEntity.ACTIVE_GOAL_METRIC_VALUE)?.toDouble(),
    )
}

private fun DocumentSnapshot.numberOrNull(field: String): Number? =
    runCatching { getDouble(field) }.getOrNull()
        ?: runCatching { getLong(field) }.getOrNull()
        ?: stringOrNull(field)?.toDoubleOrNull()

private fun DocumentSnapshot.stringOrNull(field: String): String? =
    runCatching { getString(field) }.getOrNull()

private fun DocumentSnapshot.booleanOrNull(field: String): Boolean? =
    runCatching { getBoolean(field) }.getOrNull()

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
