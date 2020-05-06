package org.example

import org.example.chat.ChatServer

fun main(args: Array<String>) {
    val server = ChatServer()
    println("Server created!")
    server.start()
}