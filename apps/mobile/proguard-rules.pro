# Project specific ProGuard rules for Smoke Analytics.
# These rules help optimize and obfuscate your code while preserving necessary functionality.
# You can control the set of applied configuration files using the 'proguardFiles' setting in build.gradle.
# For more details, see: http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JavaScript, uncomment the following block
# and replace 'fqcn.of.javascript.interface.for.webview' with the fully-qualified class name
# of your JavaScript interface to prevent it from being obfuscated.
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment the following line to preserve line number information for debugging stack traces.
# This is useful when analyzing crash reports.
#-keepattributes SourceFile,LineNumberTable

# If you preserve line number information, you can also hide the original source file name by uncommenting:
#-renamesourcefileattribute SourceFile
