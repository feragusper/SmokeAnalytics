package com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions

import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber

/**
 * Retrieves the version name defined in the app's build.gradle file.
 *
 * @return The version name of the application or null if not found.
 */
fun Context.versionName() = try {
    packageManager.getPackageInfo(packageName, 0).versionName
} catch (e: PackageManager.NameNotFoundException) {
    Timber.e("Unable to find the name: $e")
    null
}

