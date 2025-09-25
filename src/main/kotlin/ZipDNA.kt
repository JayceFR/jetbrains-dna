package org.example

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.util.zip.ZipFile
import kotlin.random.Random

import java.io.File as JFile



class ZipDNA(
    private val zipPath : String
){
    private val zipFile : ZipFile = ZipFile(zipPath)

    // The name of the zip file
    val name : String = zipFile.name.substringBefore(".zip").substringAfterLast("\\")

    private val files : MutableList<File> = mutableListOf()
    private val classNames: MutableSet<String> = mutableSetOf()
    private val methodNames: MutableSet<String> = mutableSetOf()
    private val fieldNames: MutableSet<String> = mutableSetOf()
    private val packageNames: MutableSet<String> = mutableSetOf()

    private var totalClasses = 0
    private var totalMethods = 0
    private var totalFields = 0

    // Recursive function that tokenizes the zip folder into list of files
    fun tokenize(zFile: ZipFile = zipFile, prefix: String = "") {
        val iterator = zFile.entries().asIterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val path = "$prefix${entry.name}"

            if (entry.isDirectory) continue

            val fileType = when {
                entry.isDirectory -> FileType.FOLDER
                entry.name.endsWith(".class") -> FileType.CLASS
                entry.name.endsWith(".jar") -> FileType.JAR
                else -> FileType.REGULAR_FILE
            }

            val currFile = File(
                path,
                entry.size,
                hashZipEntry(zFile, entry),
                fileType
            )

            if (fileType == FileType.JAR) {
                zFile.getInputStream(entry).use { input ->
                    val nestedBytes = input.readBytes()
                    ZipFile(tempJar(nestedBytes)).use { nestedZip ->
                        tokenize(nestedZip, "$path!")
                    }
                }
            }

            if (fileType == FileType.CLASS) {
                val classInfo = parse(zFile.getInputStream(entry).use { it.readBytes() })

                classInfo.let { classInfo ->
                    classNames.add(classInfo.className)
                    packageNames.add(classInfo.className.substringBeforeLast('.', ""))

                    methodNames.addAll(classInfo.methods.map { it.name })
                    fieldNames.addAll(classInfo.fields.map { it.name })

                    totalClasses++
                    totalMethods += classInfo.methods.size
                    totalFields += classInfo.fields.size
                }
            }

            files.add(currFile)
        }
    }

    private fun parse(bytes : ByteArray) : ClassInfo{
        val reader = ClassReader(bytes)
        val classNode = ClassNode()
        reader.accept(classNode, 0)
        val methods = classNode.methods.map {
            MethodInfo(
                it.name,
                it.desc,
                accessToString(it.access)
            )
        }
        val fields = classNode.fields.map {
            FieldInfo(
                it.name,
                it.desc,
                accessToString(it.access)
            )
        }

        return ClassInfo(
            className = classNode.name.replace('/', '.'),
            superClass = classNode.superName?.replace('/', '.'),
            interfaces = classNode.interfaces.map { it.replace('/', '.') },
            methods = methods,
            fields = fields
        )
    }

    private fun computeMinHash(){
        // Shingles
        val shingles = shinglise((classNames + fieldNames + methodNames), 3, 30)
        // Compute MinHash
        val signature = minHash(shingles.toSet(), 128, 12345L)
        
    }

    private fun <T> shinglise(tokens: Set<T>, k: Int, n: Int): List<Int> {
        val shingles = tokens
            .windowed(k, 1) // sliding window of size k
            .map { it.hashCode() } // hash each shingle

        return if (shingles.size >= n) {
            shingles.shuffled().take(n) // downsample
        } else {
            shingles + List(n - shingles.size) { 0 } // pad
        }
    }

    private fun minHash(shingles: Set<Int>, numHashes: Int, seed: Long = 0L): LongArray {
        require(numHashes > 0) { "numHashes must be > 0" }

        val rnd = Random(seed)
        val seeds = LongArray(numHashes) { rnd.nextLong() }

        val signature = LongArray(numHashes) { -1L }

        for (shingle in shingles) {
            val sh = shingle.toLong()
            for (i in 0 until numHashes) {
                val combined = sh xor seeds[i] // 64-bit mixing
                if (java.lang.Long.compareUnsigned(combined, signature[i]) < 0) {
                    signature[i] = combined
                }
            }
        }
        return signature
    }




    fun buildDNA(): DNA {
        return DNA(
            files = files,
            classNames = classNames,
            methodNames = methodNames,
            fieldNames = fieldNames,
            packageNames = packageNames,
            summary = SummaryStats(
                totalFiles = files.size,
                totalClasses = totalClasses,
                totalMethods = totalMethods,
                totalFields = totalFields
            )
        )
    }

    fun writeToJSON(path: String) {
        val dna = buildDNA()
        val json = Json { prettyPrint = true }.encodeToString(dna)
        JFile(path).writeText(json)
    }

    private fun accessToString(access: Int): String {
        val flags = mutableListOf<String>()
        if (access and Opcodes.ACC_PUBLIC != 0) flags.add("public")
        if (access and Opcodes.ACC_PRIVATE != 0) flags.add("private")
        if (access and Opcodes.ACC_PROTECTED != 0) flags.add("protected")
        if (access and Opcodes.ACC_STATIC != 0) flags.add("static")
        if (access and Opcodes.ACC_FINAL != 0) flags.add("final")
        if (access and Opcodes.ACC_ABSTRACT != 0) flags.add("abstract")
        return flags.joinToString(" ")
    }

    // Function that write bytes to a temp file for ZipFile to open
    private fun tempJar(bytes: ByteArray): java.io.File {
        val tmp = kotlin.io.path.createTempFile(suffix = ".jar").toFile()
        tmp.writeBytes(bytes)
        tmp.deleteOnExit()
        return tmp
    }


}