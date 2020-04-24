package org.example.chat

import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

class ChatServer {

    private val config by lazy { Config() }
    private val socketListener by lazy { ServerSocket(config.port) }

    fun start() {
        try {
            while (true) {
                var client: Socket? = null
                while (client == null){
                    client = socketListener.accept()
                }
                ClientThread(client)
            }
        } catch (e: SocketException) {
            System.err.println("SocketException")
            e.printStackTrace()
        } catch (e: IOException) {
            System.err.println("IOException")
            e.printStackTrace()
        }
    }
}