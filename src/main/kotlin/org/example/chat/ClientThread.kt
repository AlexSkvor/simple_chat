package org.example.chat

import java.net.Socket

class ClientThread(private val socket: Socket) : Thread() {

    init {
        start()
    }

    override fun run() {

    }
}