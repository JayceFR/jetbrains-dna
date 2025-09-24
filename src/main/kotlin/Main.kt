package org.example

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val azurePath = "C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\azure-toolkit-for-rider-4.5.4-signed.zip"
    val dartPath = "C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\dart-253.22441.25.zip"
    val zipPath : String = dartPath

//    val dna : ZipDNA = ZipDNA(zipPath)
//    dna.tokenize()

//    dna.writeToJSON("C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\out\\dart.json")

    val azureJson = "C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\out\\azure.json"
    val dartJson = "C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\out\\dart.json"

    val comparisonResult = CompareOneToOneDNA(azureJson, dartJson).compare()
    println(comparisonResult.fieldSimilarity)
    println(comparisonResult.methodSimilarity)
    println(comparisonResult.classSimilarity)
}