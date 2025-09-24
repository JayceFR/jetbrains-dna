package org.example

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.util.zip.ZipFile

class ZipDNA(
    private val zipPath : String
){
    private val zipFile : ZipFile = ZipFile(zipPath)

    // The name of the zip file
    val name : String = zipFile.name.substringBefore(".zip").substringAfterLast("\\")

    // All the files inside the zipFile
    val files : MutableList<File> = mutableListOf()

    // Recursive function that tokenizes the zip folder into list of files
    fun tokenize(zFile: ZipFile = zipFile, prefix: String = "") {
        val iterator = zFile.entries().asIterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val path = "$prefix${entry.name}"

            if (entry.isDirectory) continue

            val fileType = when{
                entry.isDirectory -> FileType.FOLDER
                entry.name.endsWith(".class") -> FileType.CLASS
                entry.name.endsWith(".jar") -> FileType.JAR
                else -> FileType.REGULAR_FILE
            }

            val currFile : File = File(
                path,
                entry.size,
                hashZipEntry(zFile, entry),
                fileType
            )

            // If the entry is a .jar, open it as a nested Zip using recursion
            if (fileType == FileType.JAR) {
                zFile.getInputStream(entry).use { input ->
                    val nestedBytes = input.readBytes()
                    ZipFile(tempJar(nestedBytes)).use { nestedZip ->
                        tokenize(nestedZip, "$path!")
                    }
                }
            }

            if (fileType == FileType.CLASS){
                currFile.classInfo = parse(zFile.getInputStream(entry).use { it.readBytes() })
            }

            files.add(currFile)

        }
    }

    fun parse(bytes : ByteArray) : ClassInfo{
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