package clover

import java.nio.file.Files
import java.nio.file.Paths
import org.objectweb.asm.Opcodes
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Label
import org.objectweb.asm.ClassWriter as Writer
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.JumpInsnNode

class Clover(args: Array<String>) {
    private val readers: List<ClassReader> = args.map { Paths.get(it)          }
                                                 .map { Files.readAllBytes(it) }
                                                 .map { ClassReader(it)        }
    fun run() {
        readers.map { reader ->
            val classNode: CloverClassNode = CloverClassNode()
            reader.accept(classNode, 0)
            classNode
        }
        .forEach { classNode ->
            classNode.methods
                .map { it as CloverMethodNode }
                .map { it.lineInstsMap to it.name        }
                .map { (instsMap, name) -> //: LinkedHashMap<Label, Pair<Int, ControlFlowNode>> ->
                    println(name)
                    instsMap.forEach { label, (line, node) ->
                        println("[$label (${LineType.get(node.lineType)})]")
                        println("    parents:  ${node.parents}")
                        println("    children: ${node.children}")
                    }
//                    typeMap.filter { it.value == LineType.EXIT }
//                           .map { entry: Map.Entry<Label, Int> ->
//                               val label = entry.key
//                               val currentLine = instsMap[label]!!.first
//                               instsMap[label]!!.second
//                                   .filter { it.opcode == Opcodes.GOTO }
//                                   .map {
//                                       it.label
//                                   }
//                           }
//                    typeMap.filter { it.value == LineType.LOOP_EXIT }
//                            .map { entry: Map.Entry<Label, Int> ->
//                                val label = entry.key
//                                val currentLine = instsMap[label]!!.first
//                                instsMap[label]!!.second
//                                        .filter { it.opcode == Opcodes.GOTO }
//                                        .map {
//                                            it.label
//                                        }
//                            }
                }
        }
    }
}