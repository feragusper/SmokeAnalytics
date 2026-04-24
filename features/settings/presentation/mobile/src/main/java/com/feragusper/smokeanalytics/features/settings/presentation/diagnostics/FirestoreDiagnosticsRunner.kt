package com.feragusper.smokeanalytics.features.settings.presentation.diagnostics

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

data class FirestoreDiagnosticsReport(
    val successful: Boolean,
    val details: String,
)

class FirestoreDiagnosticsRunner @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @param:ApplicationContext private val appContext: Context,
) {
    suspend operator fun invoke(): FirestoreDiagnosticsReport {
        val steps = mutableListOf<String>()
        var success = true
        fun line(value: String) {
            steps += value
        }

        val nowMillis = System.currentTimeMillis()
        val probeId = "play-${nowMillis}-${UUID.randomUUID().toString().take(8)}"
        val options = runCatching { FirebaseApp.getInstance().options }.getOrNull()
        val signingSummary = appContext.signingDigestSummary()
        val installer = appContext.installerPackageName()
        val uid = auth.currentUser?.uid

        line("SmokeAnalytics Firestore diagnostics")
        line("result=pending")
        line("probeId=$probeId")
        line("package=${appContext.packageName}")
        line("installer=$installer")
        line("version=${appContext.versionSummary()}")
        line("firebaseProject=${options?.projectId ?: "unknown"}")
        line("firebaseAppId=${options?.applicationId ?: "unknown"}")
        line("apiKey=${options?.apiKey.redactedApiKey()}")
        line(signingSummary)
        line("authUid=${uid ?: "missing"}")

        if (uid == null) {
            val details = steps.withResult("FAIL", "No authenticated Firebase user.")
            Log.e(TAG, details)
            return FirestoreDiagnosticsReport(successful = false, details = details)
        }

        val smokeDocument = firestore
            .collection("users")
            .document(uid)
            .collection("smokes")
            .document("_diagnostic_$probeId")
        val preferencesDocument = firestore
            .collection("users")
            .document(uid)
            .collection("profile")
            .document("preferences")

        success = runStep("smoke.set", smokeDocument.path, steps) {
            smokeDocument.set(
                mapOf(
                    "timestampMillis" to 0.0,
                    "diagnosticProbe" to true,
                    "probeId" to probeId,
                    "createdAtMillis" to nowMillis,
                    "packageName" to appContext.packageName,
                    "installer" to installer,
                )
            ).await()
        } && success

        success = runStep("smoke.serverRead", smokeDocument.path, steps) {
            val snapshot = smokeDocument.get(Source.SERVER).await()
            check(snapshot.exists()) { "document missing after set" }
            val data = snapshot.data.orEmpty()
            check(snapshot.getDouble("timestampMillis") == 0.0) {
                "timestampMillis=${snapshot.getDouble("timestampMillis")}"
            }
            check(snapshot.getString("probeId") == probeId) {
                "probeId=${snapshot.getString("probeId")}"
            }
            "keys=${data.keys.sorted()}"
        } && success

        success = runStep("smoke.delete", smokeDocument.path, steps) {
            smokeDocument.delete().await()
        } && success

        success = runStep("smoke.deleteVerify", smokeDocument.path, steps) {
            val snapshot = smokeDocument.get(Source.SERVER).await()
            check(!snapshot.exists()) { "document still exists after delete" }
        } && success

        success = runStep("preferences.merge", preferencesDocument.path, steps) {
            preferencesDocument.set(
                mapOf(
                    "diagnostics" to mapOf(
                        "lastProbeId" to probeId,
                        "lastProbeMillis" to nowMillis,
                        "packageName" to appContext.packageName,
                        "installer" to installer,
                    )
                ),
                SetOptions.merge(),
            ).await()
        } && success

        success = runStep("preferences.serverRead", preferencesDocument.path, steps) {
            val snapshot = preferencesDocument.get(Source.SERVER).await()
            check(snapshot.exists()) { "preferences document missing after merge" }
            val diagnostics = snapshot.get("diagnostics") as? Map<*, *>
            check(diagnostics?.get("lastProbeId") == probeId) {
                "diagnostics.lastProbeId=${diagnostics?.get("lastProbeId")}"
            }
            "keys=${snapshot.data.orEmpty().keys.sorted()}"
        } && success

        val details = steps.withResult(if (success) "PASS" else "FAIL")
        if (success) {
            Log.i(TAG, details)
        } else {
            Log.e(TAG, details)
        }
        return FirestoreDiagnosticsReport(successful = success, details = details)
    }

    private suspend fun runStep(
        name: String,
        path: String,
        steps: MutableList<String>,
        block: suspend () -> Any? = {},
    ): Boolean =
        try {
            val result = withTimeout(STEP_TIMEOUT_MILLIS) { block() }
            steps += "$name=PASS path=$path${result?.let { " $it" }.orEmpty()}"
            true
        } catch (error: Throwable) {
            steps += "$name=FAIL path=$path ${error.firestoreSummary()}"
            false
        }

    private companion object {
        const val TAG = "FirestoreDiagnostics"
        const val STEP_TIMEOUT_MILLIS = 20_000L
    }
}

private fun List<String>.withResult(result: String, reason: String? = null): String =
    mapIndexed { index, line ->
        if (index == 1) {
            buildString {
                append("result=").append(result)
                if (reason != null) append(" reason=").append(reason)
            }
        } else {
            line
        }
    }.joinToString(separator = "\n")

private fun Throwable.firestoreSummary(): String {
    val firestoreException = (this as? FirebaseFirestoreException) ?: (cause as? FirebaseFirestoreException)
    val type = this::class.simpleName ?: "Throwable"
    val code = firestoreException?.code?.name?.let { " code=$it" }.orEmpty()
    val message = when (this) {
        is TimeoutCancellationException -> "timed out"
        else -> message?.takeIf { it.isNotBlank() } ?: cause?.message?.takeIf { it.isNotBlank() }
    }
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

private fun Context.versionSummary(): String =
    runCatching {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toString()
        }
        "${info.versionName ?: "unknown"} ($code)"
    }.getOrElse { "unknown" }

private fun Context.signingDigestSummary(): String =
    signingCertificateBytes()
        .firstOrNull()
        ?.let { bytes ->
            "runtimeSha1=${bytes.digest("SHA-1")}\nruntimeSha256=${bytes.digest("SHA-256")}"
        }
        ?: "runtimeSha1=unknown\nruntimeSha256=unknown"

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
