package org.example

import java.util.zip.ZipFile

class ZipDNA(
    private val zipPath : String
){
    private val zipFile : ZipFile = ZipFile(zipPath)

    // The name of the zip file
    val name : String = zipFile.name.substringBefore(".zip").substringAfterLast("\\")

    // All the files inside the zipFile
    val files : MutableList<File> = mutableListOf()

    // Recursive function that tokenizes the zip folder into list of files
    fun tokenize(zFile: ZipFile = zipFile, prefix: String = "") {
        val iterator = zFile.entries().asIterator()
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

            files.add(
                File(
                    path,
                    entry.size,
                    hashZipEntry(zFile, entry),
                    fileType
                )
            )

            // If the entry is a .jar, open it as a nested Zip using recursion
            if (entry.name.endsWith(".jar")) {
                zFile.getInputStream(entry).use { input ->
                    val nestedBytes = input.readBytes()
                    ZipFile(tempJar(nestedBytes)).use { nestedZip ->
                        tokenize(nestedZip, "$path!")
                    }
                }
            }
        }
    }

    // Function that write bytes to a temp file for ZipFile to open
    private fun tempJar(bytes: ByteArray): java.io.File {
        val tmp = kotlin.io.path.createTempFile(suffix = ".jar").toFile()
        tmp.writeBytes(bytes)
        tmp.deleteOnExit()
        return tmp
    }


}