package clover

import java.nio.file.Files
import java.nio.file.Paths
import org.objectweb.asm.ClassReader as Reader
import org.objectweb.asm.ClassWriter as Writer
import org.objectweb.asm.tree.MethodNode

class Clover(args: Array<String>) {
    init {
        try {
            args.map { Paths.get(it)          }
                .map { Files.readAllBytes(it) }
                .map { Reader(it)             }
                .map {
                    val visitor: CloverClassNode = CloverClassNode()
                    it.accept(visitor, 0)
                    visitor
                }
                .forEach {
                    val methods: List<MethodNode> = it.methods as List<MethodNode>
                    methods.map { it as CloverMethodNode }
                }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}