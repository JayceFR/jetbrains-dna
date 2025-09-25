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
    private val zipPath: String,
    private val globalPath: String = "Global.json"
) {
    private val zipFile: ZipFile = ZipFile(zipPath)
    val name: String = zipFile.name.substringBefore(".zip").substringAfterLast("\\")
    private val files: MutableList<File> = mutableListOf()
    private val classNames: MutableSet<String> = mutableSetOf()
    private val methodNames: MutableSet<String> = mutableSetOf()
    private val fieldNames: MutableSet<String> = mutableSetOf()
    private val packageNames: MutableSet<String> = mutableSetOf()
    private var totalClasses = 0
    private var totalMethods = 0
    private var totalFields = 0

    private fun registerDNA(dna: DNA, path: String) {
        val global = loadGlobal(globalPath)
        val dnaId = global.noOfDNAs + 1
        dna.id = dnaId
        global.aboutDNA[dnaId] = path
        for (bucket in dna.bucketIndices) {
            global.bucket.computeIfAbsent(bucket) { mutableListOf() }.add(dnaId)
        }
        global.noOfDNAs = dnaId
        saveGlobal(global, globalPath)
    }

    fun tokenize(zFile: ZipFile = zipFile, prefix: String = "") {
        val iterator = zFile.entries().asIterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val path = "$prefix${entry.name}"
            if (entry.isDirectory) continue

            val fileType = when {
                entry.name.endsWith(".class") -> FileType.CLASS
                entry.name.endsWith(".jar") -> FileType.JAR
                else -> FileType.REGULAR_FILE
            }

            val currFile = File(path, entry.size, hashZipEntry(zFile, entry), fileType)

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
                classNames.add(classInfo.className)
                packageNames.add(classInfo.className.substringBeforeLast('.', ""))
                methodNames.addAll(classInfo.methods.map { it.name })
                fieldNames.addAll(classInfo.fields.map { it.name })
                totalClasses++
                totalMethods += classInfo.methods.size
                totalFields += classInfo.fields.size
            }

            files.add(currFile)
        }
    }

    private fun parse(bytes: ByteArray): ClassInfo {
        val reader = ClassReader(bytes)
        val classNode = ClassNode()
        reader.accept(classNode, 0)
        val methods = classNode.methods.map {
            MethodInfo(it.name, it.desc, accessToString(it.access))
        }
        val fields = classNode.fields.map {
            FieldInfo(it.name, it.desc, accessToString(it.access))
        }
        return ClassInfo(
            className = classNode.name.replace('/', '.'),
            superClass = classNode.superName?.replace('/', '.'),
            interfaces = classNode.interfaces.map { it.replace('/', '.') },
            methods = methods,
            fields = fields
        )
    }

    private fun computeMinHash(): List<Int> {
        val tokens = classNames + methodNames + fieldNames
        val shingles = shinglise(tokens, 3)                  // deterministic shingles
        val signature = minHash(shingles, 128, 12345L).toList()      // minhash signature

        val NO_OF_BANDS = 32
        val bandSize = (signature.size / NO_OF_BANDS).coerceAtLeast(1)
        val bands = signature.chunked(bandSize)
        return bands.map { it.toLongArray().contentHashCode() }  // band hash buckets
    }

    private fun shinglise(tokens: Set<String>, k: Int): List<Int> {
        val sortedTokens = tokens.toList().sorted()
        return if (sortedTokens.size < k) {
            sortedTokens.map { it.hashCode() }
        } else {
            sortedTokens.windowed(k, 1).map { shingle ->
                shingle.joinToString("-").hashCode()
            }
        }
    }

    private fun minHash(shingles: List<Int>, numHashes: Int, seed: Long = 0L): LongArray {
        val rnd = Random(seed)
        val seeds = LongArray(numHashes) { rnd.nextLong() }
        val signature = LongArray(numHashes) { Long.MAX_VALUE }

        for (shingle in shingles) {
            val sh = shingle.toLong() and 0xFFFFFFFFL
            for (i in 0 until numHashes) {
                val combined = sh xor seeds[i]
                if (combined < signature[i]) signature[i] = combined
            }
        }
        return signature
    }

    private fun buildDNA(): DNA {
        return DNA(
            id = 0,
            bucketIndices = computeMinHash(),
            name = name,
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
        registerDNA(dna, path)
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

    private fun tempJar(bytes: ByteArray): java.io.File {
        val tmp = kotlin.io.path.createTempFile(suffix = ".jar").toFile()
        tmp.writeBytes(bytes)
        tmp.deleteOnExit()
        return tmp
    }
}
