package org.example

import java.util.zip.ZipFile

// Recursive function that tokenizes the zip folder into list of files
fun tokenize(zipFile: ZipFile, dna: DNA, prefix: String = "") {
    val iterator = zipFile.entries().asIterator()
    while (iterator.hasNext()) {
        val entry = iterator.next()
        val path = "$prefix${entry.name}"

        if (entry.isDirectory) continue

        val fileType = when{
            entry.isDirectory -> FileType.FOLDER
            entry.name.endsWith(".class") -> FileType.CLASS
            entry.name.endsWith(".jar") -> FileType.JAR
            else -> FileType.REGULAR_FILE
        }

        dna.add(
            File(
                path,
                entry.size,
                hashZipEntry(zipFile, entry),
                fileType
            )
        )

        // If the entry is a .jar, open it as a nested Zip using recursion
        if (entry.name.endsWith(".jar")) {
            zipFile.getInputStream(entry).use { input ->
                val nestedBytes = input.readBytes()
                ZipFile(tempJar(nestedBytes)).use { nestedZip ->
                    tokenize(nestedZip, dna, "$path!")
                }
            }
        }
    }
}

// Function that write bytes to a temp file for ZipFile to open
fun tempJar(bytes: ByteArray): java.io.File {
    val tmp = kotlin.io.path.createTempFile(suffix = ".jar").toFile()
    tmp.writeBytes(bytes)
    tmp.deleteOnExit()
    return tmp
}