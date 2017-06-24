package clover

object LineType {
    const val NORMAL:     Int = 0x00
    const val ENTRY:      Int = 0x01
    const val EXIT:       Int = 0x02
    const val LOOP_ENTRY: Int = 0x04
    const val LOOP_EXIT:  Int = 0x08
    const val ONE_LINE_BREAK:   Int = ENTRY or EXIT

    fun get(type: Int) = when(type) {
        NORMAL     -> "Normal"
        ENTRY      -> "Entry"
        EXIT       -> "Exit"
        LOOP_ENTRY -> "Loop Entry"
        LOOP_EXIT  -> "Loop Exit"
        else       -> "One Line"
    }
}