package clover

import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes as Op
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LookupSwitchInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MultiANewArrayInsnNode
import org.objectweb.asm.tree.VarInsnNode
import org.objectweb.asm.tree.TableSwitchInsnNode
import org.objectweb.asm.tree.TypeInsnNode

class CloverMethodNode(access: Int, name: String, desc: String,
                       sig: String?, exs: Array<String>?): MethodNode(Op.ASM6, access, name, desc, sig, exs) {
    // Pair(line number, opcodes of the line)
    val lineInstsMap = linkedMapOf<Label, Pair<Int, ControlFlowNode>>()

    override fun visitInsn(opcode: Int) = add(InsnNode(opcode))
    override fun visitLdcInsn(cst: Any) = add(LdcInsnNode(cst))
    override fun visitVarInsn(opcode: Int, _var: Int)     = add(VarInsnNode(opcode, _var))
    override fun visitIntInsn(opcode: Int, operand: Int)  = add(IntInsnNode(opcode, operand))
    override fun visitIincInsn(_var: Int, increment: Int) = add(IincInsnNode(_var, increment))
    override fun visitTypeInsn(opcode: Int, type: String) = add(TypeInsnNode(opcode, type))
    override fun visitJumpInsn(opcode: Int, label: Label) = addJumpNode(JumpInsnNode(opcode, LabelNode(label)))
    override fun visitMultiANewArrayInsn(desc: String, dims: Int) = add(MultiANewArrayInsnNode(desc, dims))
    override fun visitFieldInsn(opcode: Int, owner: String, name: String, desc: String)
        = add(FieldInsnNode(opcode, owner, name, desc))
    override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean)
        = add(MethodInsnNode(opcode, owner, name, desc, itf))
    override fun visitInvokeDynamicInsn(name: String, desc: String, bsm: Handle, vararg bsmArgs: Any)
        = add(InvokeDynamicInsnNode(name, desc, bsm, *bsmArgs))
    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label, vararg labels: Label)
        = add(TableSwitchInsnNode(min, max, getLabelNode(dflt), *_getLabelNodes(*labels)))
    override fun visitLookupSwitchInsn(dflt: Label, keys: IntArray, labels: Array<Label>)
        = add(LookupSwitchInsnNode(getLabelNode(dflt), keys, _getLabelNodes(*labels)))

    override fun visitLabel(label: Label) {
        val lineNumber = lineInstsMap.size
        lineInstsMap.put(label, lineNumber to ControlFlowNode())
        super.visitLabel(label)
    }

    override fun visitEnd() {
        val nodes: Array<Label> = lineInstsMap.keys.toTypedArray()
        lineInstsMap.forEach { label, (lineNumber, node) ->
            // set parent and child node
            if(lineNumber in (1 until nodes.size)) {
                val beforeLabel: Label = nodes[lineNumber - 1]
                val beforeNode: ControlFlowNode = lineInstsMap[beforeLabel]!!.second
                val beforeType = beforeNode.lineType
                if(beforeType != LineType.EXIT && beforeType != LineType.LOOP_EXIT) {
                    addParentAndChildLabel(beforeLabel, label)
                }
            }
            // set jump point node
            if(node.lineType != LineType.NORMAL) {
                if(node.lineType == LineType.EXIT) {
                    val childLabel: Label = node.children.first()
                    val jumpedLineNumber = lineInstsMap[childLabel]!!.first
                    if(jumpedLineNumber < lineNumber) {
                        node.lineType = LineType.LOOP_EXIT
                        lineInstsMap[childLabel]!!.second.lineType = LineType.LOOP_ENTRY
                    }
                }
                node.children.filter { it != label } // not own
                             .forEach { addParentAndChildLabel(label, it) }
            }
        }
    }

    private fun addParentAndChildLabel(parent: Label, child: Label) {
        lineInstsMap[parent]!!.second.add("child", child)
        lineInstsMap[child]!!.second.add("parent", parent)
    }

    private fun addJumpNode(jump: JumpInsnNode) {
        if(jump.opcode != Op.JSR) lineInstsMap[getLabel()]!!.second.addInstruction(jump)
        add(jump)
    }

    private fun add(insn: AbstractInsnNode) = super.instructions.add(insn)


    /**
     * this method's processing is same as org.objectweb.asm.tree.MethodNode.getLabelNodes(Array<Label>).
     * @see org.objectweb.asm.tree.MethodNode.getLabelNodes
     */
    private fun _getLabelNodes(vararg labels: Label): Array<LabelNode>
        = labels.map { super.getLabelNode(it) } .toTypedArray()
    private fun getLabel(): Label = lineInstsMap.keys.last()
}