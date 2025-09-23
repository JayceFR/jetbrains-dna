package org.example

import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

typealias DNA = MutableList<FileFingerPrint>
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val zipPath : String = "C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\azure-toolkit-for-rider-4.5.4-signed.zip"
    val zipFile : ZipFile = ZipFile(zipPath)

    val name : String = zipFile.name.substringBefore(".zip").substringAfterLast("\\")
    println("Opened ${name}")
    val iterator : Iterator<ZipEntry> = zipFile.entries().asIterator()
    val fileDNA : DNA = mutableListOf()
    processZip(zipFile, fileDNA)
//    while (iterator.hasNext()){
//        val entry : ZipEntry = iterator.next()
//        fileDNA.add(
//            FileFingerPrint(
//                entry.name,
//                entry.size,
//                hashZipEntry(zipFile, entry)
//            )
//        )
//    }
    fileDNA.println()
}

fun DNA.println() : Unit{
    println(this.joinToString("\n"))
}

// Function returns the hash of the entry as a string of hex
fun hashZipEntry(zipFile : ZipFile, entry : ZipEntry, algorithm : String = "SHA-256"): String{

    // Separate the files into 4kb of memory at once to not overload the RAM
    val buffer = ByteArray(1024 * 4) // 4KB buffer
    val digest = MessageDigest.getInstance(algorithm)

    zipFile.getInputStream(entry).use { input ->
        var bytesRead : Int
        while (input.read(buffer).also { bytesRead = it } != -1){
            digest.update(buffer, 0, bytesRead)
        }
    }

    // Hash the digest as convert to understandable hex
    return digest.digest().joinToString("") {"%02x".format(it)}
}

fun processZip(zipFile: ZipFile, dna: DNA, prefix: String = "") {
    val iterator = zipFile.entries().asIterator()
    while (iterator.hasNext()) {
        val entry = iterator.next()
        val path = "$prefix${entry.name}"

        if (entry.isDirectory) continue

        // Record this entry
        dna.add(FileFingerPrint(path, entry.size, hashZipEntry(zipFile, entry)))

        // If the entry is a .jar, open it as a nested Zip using recursion
        if (entry.name.endsWith(".jar")) {
            zipFile.getInputStream(entry).use { input ->
                val nestedBytes = input.readBytes()
                ZipFile(tempJar(nestedBytes)).use { nestedZip ->
                    processZip(nestedZip, dna, "$path!")
                }
            }
        }
    }
}

// Helper: write bytes to a temp file for ZipFile to open
fun tempJar(bytes: ByteArray): java.io.File {
    val tmp = kotlin.io.path.createTempFile(suffix = ".jar").toFile()
    tmp.writeBytes(bytes)
    tmp.deleteOnExit()
    return tmp
}

