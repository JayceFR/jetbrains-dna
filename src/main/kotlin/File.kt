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

data class ClassInfo(
    val className: String,
    val superClass: String?,
    val interfaces: List<String>,
    val methods: List<MethodInfo>,
    val fields: List<FieldInfo>
)

data class MethodInfo(
    val name: String,
    val descriptor: String,
    val access: String
)

data class FieldInfo(
    val name: String,
    val descriptor: String,
    val access: String
)
