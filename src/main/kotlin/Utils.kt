package org.example

import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

// Extension function to print the list of files
fun MutableList<File>.println() : Unit{
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