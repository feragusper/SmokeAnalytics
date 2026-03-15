import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

private const val PRODUCT_VERSION_KEY = "product.version"

fun smokeGitCode(rootDir: File): Int {
    val stdout = ByteArrayOutputStream()
    val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
        .directory(rootDir)
        .redirectErrorStream(true)
        .start()

    process.inputStream.use { inputStream ->
        inputStream.copyTo(stdout)
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        throw IllegalStateException("Git command failed with exit code $exitCode")
    }

    return stdout.toString().trim().toInt()
}

fun smokeVersionProperties(rootDir: File): Properties {
    val properties = Properties()
    val propertiesFile = File(rootDir, "version.properties")
    if (!propertiesFile.isFile) {
        throw IllegalStateException("Missing version.properties at ${propertiesFile.absolutePath}")
    }

    InputStreamReader(FileInputStream(propertiesFile), Charsets.UTF_8).use { reader ->
        properties.load(reader)
    }
    return properties
}

fun smokeProductVersion(rootDir: File): String =
    smokeVersionProperties(rootDir).getProperty(PRODUCT_VERSION_KEY)
        ?: throw IllegalStateException("Missing $PRODUCT_VERSION_KEY in version.properties")

fun smokeAndroidVersionName(rootDir: File): String =
    "${smokeProductVersion(rootDir)}.${smokeGitCode(rootDir)}"

fun smokeWebVersionName(
    rootDir: File,
    env: String,
): String = buildString {
    append(smokeProductVersion(rootDir))
    append("+web.")
    append(smokeGitCode(rootDir))
    if (env != "prod") append("-").append(env)
}

fun smokePlatformTag(
    platform: String,
    versionName: String,
): String = "$platform/v$versionName"
