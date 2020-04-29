package org.example.chat

import org.example.new_chat.Client
import org.example.new_chat.ClientThread
import org.example.new_chat.User
import org.example.new_chat.UserAction
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
                var client: Socket? = null
                while (client == null) {
                    client = socketListener.accept()
                }
                chatThread.addClient(ClientThread(Client(client)))
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