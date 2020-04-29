package org.example

import org.example.chat.ChatServer
import java.time.format.DateTimeFormatter

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss:SSS")

fun main(args: Array<String>) {
    val server = ChatServer()
    println("Hello world!")
    server.start()
}