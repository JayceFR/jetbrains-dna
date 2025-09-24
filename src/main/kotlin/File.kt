package org.example

import kotlinx.serialization.Serializable

enum class FileType{
    FOLDER,
    CLASS,
    JAR,
    REGULAR_FILE
}

@Serializable
data class File(
    val fileName : String,
    val size : Long,
    val hash : String,
    val type : FileType,
    var classInfo: ClassInfo? = null
)

@Serializable
data class ClassInfo(
    val className: String,
    val superClass: String?,
    val interfaces: List<String>,
    val methods: List<MethodInfo>,
    val fields: List<FieldInfo>
)

@Serializable
data class MethodInfo(
    val name: String,
    val descriptor: String,
    val access: String
)

@Serializable
data class FieldInfo(
    val name: String,
    val descriptor: String,
    val access: String
)
