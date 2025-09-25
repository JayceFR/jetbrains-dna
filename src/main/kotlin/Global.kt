package org.example

import kotlinx.serialization.Serializable

@Serializable
data class Global(
    var noOfDNAs : Int = 0,
    // Maps between the DNA_ID to the Path of JSON data
    val aboutDNA : HashMap<Int, String> = hashMapOf(),
    // Maps between the bucket to DNA_ID
    val bucket : HashMap<Int, MutableList<Int>> = hashMapOf()
)