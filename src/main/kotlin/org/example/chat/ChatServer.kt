package org.example.chat

import org.example.alsoPrintDebug
import org.example.connections.Client
import org.example.connections.ClientThread
import org.example.connections.User
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.*

class ChatServer {

    private val config by lazy { Config() }
    private val socketListener by lazy { ServerSocket(config.port) }

    private val chatThread: ChatThread by lazy { ChatThread() }

    fun start() {
        try {
            while (true) {
                var socket: Socket? = null
                alsoPrintDebug("started waiting")
                while (socket == null) {
                    socket = socketListener.accept()
                }
                acceptUser(socket)
                Thread.sleep(100)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun acceptUser(socket: Socket){
        val user = User(uuid = UUID.randomUUID().toString())
        val output = ObjectOutputStream(socket.getOutputStream())
        output.flush()
        val input = ObjectInputStream(socket.getInputStream())
        val c = Client(socket, user, input, output)
        c.prepare()
        chatThread.addClient(ClientThread(c))
    }
}