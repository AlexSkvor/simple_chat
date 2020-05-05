package org.example.connections

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

data class Client(
    val socket: Socket,
    val user: User,
    val inputStream: ObjectInputStream,
    val outputStream: ObjectOutputStream
) {
    fun prepare() {
        outputStream.flush()
        val intention = Intention.YourId(user.uuid)
        outputStream.writeObject(intention)
    }
}