package org.example

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
    while (iterator.hasNext()){
        val entry : ZipEntry = iterator.next()
        fileDNA.add(
            FileFingerPrint(
                entry.name,
                entry.size
            )
        )
    }
    fileDNA.println()
}

fun DNA.println() : Unit{
    println(this.joinToString("\n"))
}
