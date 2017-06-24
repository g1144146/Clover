package clover

fun main(args: Array<String>) {
    if(args.isEmpty()) {
        return
    }
    Clover(args).run()
}