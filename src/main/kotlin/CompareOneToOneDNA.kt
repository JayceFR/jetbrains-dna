package org.example

import kotlinx.serialization.json.Json
import java.io.File as JFile

class CompareOneToOneDNA(
    private val path1: String,
    private val path2: String
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val dna1: DNA = json.decodeFromString(JFile(path1).readText())
    private val dna2: DNA = json.decodeFromString(JFile(path2).readText())

    fun compare(): ComparisonResult {
        // Compare sets
        val commonClasses = dna1.classNames.intersect(dna2.classNames)
        val uniqueClasses1 = dna1.classNames - dna2.classNames
        val uniqueClasses2 = dna2.classNames - dna1.classNames

        val commonMethods = dna1.methodNames.intersect(dna2.methodNames)
        val uniqueMethods1 = dna1.methodNames - dna2.methodNames
        val uniqueMethods2 = dna2.methodNames - dna1.methodNames

        val commonFields = dna1.fieldNames.intersect(dna2.fieldNames)
        val uniqueFields1 = dna1.fieldNames - dna2.fieldNames
        val uniqueFields2 = dna2.fieldNames - dna1.fieldNames

        val commonPackages = dna1.packageNames.intersect(dna2.packageNames)

        // File-level overlap (by filename or hash)
        val fileHashes1 = dna1.files.map { it.hash }.toSet()
        val fileHashes2 = dna2.files.map { it.hash }.toSet()
        val commonFiles = fileHashes1.intersect(fileHashes2)

        // Simple similarity metric (Jaccard index on class names, for example)
        val classSimilarity = jaccard(dna1.classNames, dna2.classNames)
        val methodSimilarity = jaccard(dna1.methodNames, dna2.methodNames)
        val fieldSimilarity = jaccard(dna1.fieldNames, dna2.fieldNames)

        val overallSimilarity = computeOverallSimilarity()

        return ComparisonResult(
            commonClasses, uniqueClasses1, uniqueClasses2,
            commonMethods, uniqueMethods1, uniqueMethods2,
            commonFields, uniqueFields1, uniqueFields2,
            commonPackages,
            commonFiles,
            classSimilarity, methodSimilarity, fieldSimilarity, overallSimilarity
        )
    }

    fun computeOverallSimilarity(): Double {
        // Weights: tune as you like
        val weightFiles = 0.5
        val weightClasses = 0.25
        val weightMethods = 0.15
        val weightFields = 0.1

        val fileSimilarity = jaccard(
            dna1.files.map { it.hash }.toSet(),
            dna2.files.map { it.hash }.toSet()
        )

        val classSimilarity = jaccard(dna1.classNames, dna2.classNames)
        val methodSimilarity = jaccard(dna1.methodNames, dna2.methodNames)
        val fieldSimilarity = jaccard(dna1.fieldNames, dna2.fieldNames)

        return (fileSimilarity * weightFiles
                + classSimilarity * weightClasses
                + methodSimilarity * weightMethods
                + fieldSimilarity * weightFields)
    }

    private fun jaccard(a: Set<String>, b: Set<String>): Double {
        if (a.isEmpty() && b.isEmpty()) return 1.0
        return a.intersect(b).size.toDouble() / a.union(b).size.toDouble()
    }
}

data class ComparisonResult(
    val commonClasses: Set<String>,
    val uniqueClasses1: Set<String>,
    val uniqueClasses2: Set<String>,

    val commonMethods: Set<String>,
    val uniqueMethods1: Set<String>,
    val uniqueMethods2: Set<String>,

    val commonFields: Set<String>,
    val uniqueFields1: Set<String>,
    val uniqueFields2: Set<String>,

    val commonPackages: Set<String>,
    val commonFiles: Set<String>,

    val classSimilarity: Double,
    val methodSimilarity: Double,
    val fieldSimilarity: Double,
    val overallSimilarity: Double
)
