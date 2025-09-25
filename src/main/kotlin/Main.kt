package org.example

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println(
            """
            Usage:
              dna tokenize <zipPath> <outJson>
              dna compare <json1> <json2>
              dna compare-many <jsonPath>
            """.trimIndent()
        )
        return
    }

    when (args[0]) {
        "tokenize" -> {
            if (args.size < 3) {
                println("Usage: dna tokenize <zipPath> <outJson>")
                return
            }
            val zipPath = args[1]
            val outJson = args[2]

            val dna = ZipDNA(zipPath)
            dna.tokenize()
            dna.writeToJSON(outJson)
            println("✅ DNA written to $outJson")
        }

        "compare" -> {
            if (args.size < 3) {
                println("Usage: dna compare <json1> <json2>")
                return
            }
            val json1 = args[1]
            val json2 = args[2]

            val comparisonResult = CompareOneToOneDNA(json1, json2).compare()
            println(comparisonResult.prettyPrint())
        }

        "compare-many" -> {
            if (args.size < 2) {
                println("Usage: dna compare-many <jsonPath>")
                return
            }
            val jsonPath = args[1]

            val results = CompareOneToManyDNA(jsonPath).compare()
            if (results.isEmpty()){
                println("No Matches Found! Good To Go!")
            }
            else{
                println("Alert! Matches found! with the following plugins :")
                results.forEach { println(it.name) }
                println("----------------")
                results.forEach { println(it.prettyPrint()) }
            }
        }

        else -> {
            println("❌ Unknown command: ${args[0]}")
        }
    }
}