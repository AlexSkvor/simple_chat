package org.example.chat

import org.example.alsoPrintDebug
import org.example.connections.Client
import org.example.connections.ClientThread
import java.net.ServerSocket
import java.net.Socket

class ChatServer {

    private val config by lazy { Config() }
    private val socketListener by lazy { ServerSocket(config.port) }
    private val chatThread: ChatThread by lazy { ChatThread() }

    fun start() {
        try {
            while (true) {
                var socket: Socket? = null
                alsoPrintDebug("Started waiting for next client!")
                while (socket == null)
                    socket = socketListener.accept()
                chatThread.addClient(ClientThread(Client(socket)))
                Thread.sleep(100)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}