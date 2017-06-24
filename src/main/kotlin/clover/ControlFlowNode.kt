package clover

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Label
import org.objectweb.asm.tree.JumpInsnNode

class ControlFlowNode {
    val parents:  LinkedHashSet<Label> = linkedSetOf<Label>()
    val children: LinkedHashSet<Label> = linkedSetOf<Label>()
    var lineType: Int = 0

    fun add(type: String, label: Label) {
        if(type == "parent") parents.add(label)
        else                 children.add(label)
    }

    fun addInstruction(insn: JumpInsnNode) {
        children.add(insn.label.label)
        lineType = lineType or when(insn.opcode) {
            Opcodes.GOTO -> LineType.EXIT
            else         -> LineType.ENTRY
        }
    }
}