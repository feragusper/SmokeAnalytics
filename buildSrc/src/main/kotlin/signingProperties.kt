import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

fun signingProperties(rootDir: File, fileName: String): Properties {
    val properties = Properties()
    val propertiesFile = File(rootDir, fileName)

    if (propertiesFile.isFile) {
        InputStreamReader(FileInputStream(propertiesFile), Charsets.UTF_8).use { reader ->
            properties.load(reader)
        }
    }
    return properties
}
