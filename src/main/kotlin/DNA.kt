package org.example

import kotlinx.serialization.Serializable

enum class FileType{
    FOLDER,
    CLASS,
    JAR,
    REGULAR_FILE
}

@Serializable
data class DNA(
    var id : Int,
    val bucketIndices : List<Int>,
    val classNames: Set<String>,
    val methodNames: Set<String>,
    val fieldNames: Set<String>,
    val packageNames: Set<String>,
    val summary: SummaryStats,
    val files : List<File>
)

@Serializable
data class SummaryStats(
    val totalFiles: Int,
    val totalClasses: Int,
    val totalMethods: Int,
    val totalFields: Int
)

@Serializable
data class File(
    val fileName : String,
    val size : Long,
    val hash : String,
    val type : FileType,
    // Could add classInfo for detailed review
//    var classInfo: ClassInfo? = null
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
