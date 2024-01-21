package com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions

import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber

fun Context.versionName() = try {
    packageManager.getPackageInfo(packageName, 0).versionName
} catch (e: PackageManager.NameNotFoundException) {
    Timber.e("Unable to find the name: $e")
    null
}

