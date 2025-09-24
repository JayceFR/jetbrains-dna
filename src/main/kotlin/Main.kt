package org.example

import java.util.zip.ZipFile

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val zipPath : String = "C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\azure-toolkit-for-rider-4.5.4-signed.zip"

    val dna : ZipDNA = ZipDNA(zipPath)
    dna.tokenize()
    dna.files.println()

}