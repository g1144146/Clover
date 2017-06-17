package clover

import java.lang.Byte as JByte
import java.lang.Short as JShort
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.MethodVisitor as Visitor

class OffsetMethodVisitor(mv: Visitor): Visitor(Opcodes.ASM6, mv) {
    var offset = 0

    override fun visitInsn(opcode: Int) {
        println("$offset: ${Instructions.types[opcode]} (visitInsn)")
        addOffset(0)
        super.visitInsn(opcode)
    }

    override fun visitVarInsn(opcode: Int, _var: Int) {
        println("$offset: ${Instructions.types[opcode]}, $_var (visitVarinsn)")
        if((opcode == Opcodes.RET) || _var in (0..3)) addOffset(0) else addOffset(JByte.BYTES)
        super.visitVarInsn(opcode, _var)
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        println("$offset: ${Instructions.types[opcode]}, $operand (visitIntInsn)")
        if(opcode == Opcodes.SIPUSH) addOffset(JShort.BYTES) else addOffset(JByte.BYTES)
        super.visitIntInsn(opcode, operand)
    }

    override fun visitIincInsn(_var: Int, increment: Int) {
        println("$offset: iinc, $increment (visitIincInsn)")
        addOffset(JByte.BYTES * 2)
        super.visitIincInsn(_var, increment)
    }

    override fun visitJumpInsn(opcode: Int, label: Label) {
        println("$offset: ${Instructions.types[opcode]}, ${label.toString()} (visitJumpInsn)")
        addOffset(JShort.BYTES)
        super.visitJumpInsn(opcode, label)
    }

    override fun visitLdcInsn(cst: Any) {
        println("$offset: ldc, $cst (visitLdcInsn)")
        addOffset(JByte.BYTES)
        super.visitLdcInsn(cst)
    }

    override fun visitTypeInsn(opcode: Int, type: String) {
        println("$offset: ${Instructions.types[opcode]}, $type (visitTypeInsn)")
        addOffset(JShort.BYTES)
        super.visitTypeInsn(opcode, type)
    }

    override fun visitMultiANewArrayInsn(desc: String, dims: Int) {
        println("$offset: multianewarray, $desc, $dims (visitMultiANewArrayInsn)")
        addOffset(JShort.BYTES + JByte.BYTES)
        super.visitMultiANewArrayInsn(desc, dims)
    }

    override fun visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) {
        println("$offset: ${Instructions.types[opcode]}, $desc $owner.$name (visitFieldInsn)")
        addOffset(JShort.BYTES)
        super.visitFieldInsn(opcode, owner, name, desc)
    }

    override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
        println("$offset: ${Instructions.types[opcode]}, $owner.$name.$desc (visitMethodInsn)")
        val _offset: Int = if(opcode == Opcodes.INVOKEINTERFACE) JShort.BYTES + (JByte.BYTES * 2) else JShort.BYTES
        addOffset(_offset)
        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }

    override fun visitInvokeDynamicInsn(name: String, desc: String, bsm: Handle, vararg bsmArgs: Any) {
        println("$offset: invokedynamic, $name.$desc (visitInvokeDynamicInsn)")
        addOffset(JShort.BYTES * 2)
        super.visitInvokeDynamicInsn(name, desc, bsm, *bsmArgs)
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label, vararg labels: Label) {
        println("$offset: tableswitch (visitTableSwitchInsn)")
        addOffset(JShort.BYTES, true) // opcode kind byte
        addOffsetForSwitch()
        addOffset(Integer.BYTES * 3, true) // default + low + high
        labels.indices.forEach { addOffset(Integer.BYTES, true) }
        super.visitTableSwitchInsn(min, max, dflt, *labels)
    }

    override fun visitLookupSwitchInsn(dflt: Label, keys: IntArray, labels: Array<out Label>) {
        println("$offset: lookupswitch (visitLookupSwitchInsn)")
        addOffset(JShort.BYTES, true) // opcode kind byte
        addOffsetForSwitch()
        addOffset(Integer.BYTES * 2, true) // default + match_length
        labels.indices.forEach { addOffset(Integer.BYTES * 2, true) }
        super.visitLookupSwitchInsn(dflt, keys, labels)
    }

    private fun addOffsetForSwitch() {
        if(offset % 4 == 0) return
        addOffset(JShort.BYTES, true)
        addOffsetForSwitch()
    }

    private fun addOffset(_offset: Int, isSwitch: Boolean) {
        // +1: opcode kind byte
        offset += if(isSwitch) _offset else (_offset + 1)
    }

    private fun addOffset(_offset: Int) = addOffset(_offset, false)
}