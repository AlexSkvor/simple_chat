package org.example.chat

import org.example.connections.ClientThread

interface ClientsManager {
    fun addClient(clientThread: ClientThread)
    fun removeClient(userId: String)
    fun forEachClient(block: (ClientThread) -> Unit)
    fun forClientWithUserId(userId: String, block: (ClientThread) -> Unit)
}

class SynchronizedClientManager: ClientsManager {

    private val clients: MutableList<ClientThread> = mutableListOf()

    @Synchronized
    override fun addClient(clientThread: ClientThread) {
        clients.add(clientThread)
    }

    @Synchronized
    override fun removeClient(userId: String) {
        val i = clients.indexOfFirst { it.user.uuid == userId }
        clients.removeAt(i)
    }

    @Synchronized
    override fun forClientWithUserId(userId: String, block: (ClientThread) -> Unit) {
        clients.firstOrNull { it.user.uuid == userId }?.let { block(it) }
    }

    override fun forEachClient(block: (ClientThread) -> Unit) {
        var i = 0
        while (true) {
            val client = getNextClient(i) ?: break
            block(client)
            i++
        }
    }

    @Synchronized
    private fun getNextClient(index: Int): ClientThread? {
        require(index >= 0)
        return if (index in clients.indices) clients[index]
        else null
    }
}