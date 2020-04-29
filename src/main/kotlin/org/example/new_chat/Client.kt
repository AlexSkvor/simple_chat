package org.example.new_chat

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.*

data class Client(
    val socket: Socket,
    val user: User = User(uuid = UUID.randomUUID().toString()),
    val inputStream: ObjectInputStream= ObjectInputStream(socket.getInputStream()),
    val outputStream: ObjectOutputStream= ObjectOutputStream(socket.getOutputStream())
) {
    init {
        outputStream.flush()
    }
}