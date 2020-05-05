package org.example

fun doNothing() {

}

fun `try`(action: () -> Unit) {
    try {
        action.invoke()
    }catch (e: Throwable){
        e.printStackTrace()
    }
}

inline fun <reified T> T.alsoPrintDebug(msg: String) =
    also { println("$msg...$this") }