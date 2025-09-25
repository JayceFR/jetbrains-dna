package org.example

import kotlinx.serialization.Serializable

@Serializable
data class Global(
    val noOfDNAs : Int,
    // Maps between the DNA_ID to the Path of JSON data
    val aboutDNA : HashMap<Int, String>,
    // Maps between the bucket to DNA_ID
    val bucket : HashMap<Int, Int>
)