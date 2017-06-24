package clover

import org.objectweb.asm.Opcodes as Op
import org.objectweb.asm.MethodVisitor as MVisitor
import org.objectweb.asm.tree.ClassNode

class CloverClassNode: ClassNode(Op.ASM6) {
    override fun visitMethod(access: Int, name: String, desc: String, sig: String?, exs: Array<String>?): MVisitor {
//        println("$name $desc")
        val visitor: CloverMethodNode = CloverMethodNode(access, name, desc, sig, exs)
//        val _visitor: OffsetMethodVisitor = OffsetMethodVisitor(visitor)
        methods.add(visitor)
        return visitor
    }
}