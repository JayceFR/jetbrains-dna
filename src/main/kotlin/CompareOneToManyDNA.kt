package org.example

import kotlinx.serialization.json.Json
import java.io.File

class CompareOneToManyDNA(
    private val path: String,
    private val globalPath: String = "Global.json"
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val dna: DNA = json.decodeFromString(File(path).readText())

    // Holds the ids of the dna that match
    private val outputSet : MutableSet<Int> = mutableSetOf()

    fun compare() : List<ComparisonResult>{
        val global = loadGlobal(globalPath)
        dna.bucketIndices.forEach { bucketIdx ->
            global.bucket[bucketIdx]?.forEach { element ->
                outputSet.add(element)
            }
        }
        outputSet.remove(dna.id)
        val returnList : MutableList<ComparisonResult> = mutableListOf()
        outputSet.forEach { element ->
            global.aboutDNA[element]?.let { otherDNAPath ->
                returnList.add(
                    CompareOneToOneDNA(path, otherDNAPath).compare()
                )
            }
        }
        return returnList
    }
}