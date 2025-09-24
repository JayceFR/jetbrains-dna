package org.example

enum class FileType{
    FOLDER,
    CLASS,
    JAR,
    REGULAR_FILE
}

data class File(
    val fileName : String,
    val size : Long,
    val hash : String,
    val type : FileType,
)
