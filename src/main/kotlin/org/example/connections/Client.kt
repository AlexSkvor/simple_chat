package org.example.connections

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.*

data class Client(
    val socket: Socket,
    val user: User = User(uuid = UUID.randomUUID().toString()),
    val outputStream: ObjectOutputStream = ObjectOutputStream(socket.getOutputStream()).also { it.flush() },
    val inputStream: ObjectInputStream = ObjectInputStream(socket.getInputStream())
) {
    init {
        outputStream.writeObject(Intention.YourId(user.uuid))
    }
}