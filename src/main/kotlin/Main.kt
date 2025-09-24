package org.example

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val azurePath = "C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\azure-toolkit-for-rider-4.5.4-signed.zip"
    val dart2Path = "C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\dart-253.22441.25.zip"
    var dart1Path = "C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\dart-253.17525.83.zip"

    val azureJson = "C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\out\\azure.json"
    val dart2Json = "C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\out\\dart.json"
    val dart1Json = "C:\\Users\\jayce\\Documents\\Scripts\\dna\\data\\out\\dart1.json"
//    val zipPath : String = dart1Path
//
//    val dna : ZipDNA = ZipDNA(zipPath)
//    dna.tokenize()
//
//    dna.writeToJSON(dart1Json)

    val comparisonResult = CompareOneToOneDNA(dart1Json, dart2Json).compare()
    println("Field Similarity : ${comparisonResult.fieldSimilarity}")
    println("Method Similarity : ${comparisonResult.methodSimilarity}")
    println("Class Similarity : ${comparisonResult.classSimilarity}")

    println("Overall Similarity : ${comparisonResult.overallSimilarity}")

}