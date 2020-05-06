package org.example.connections

import org.example.doNothing
import java.time.LocalDateTime

class ClientThread(
    private val client: Client
) : Thread() {

    private val taskQueue: MutableList<Intention> = mutableListOf()

    private val receivedMessages: MutableList<UserAction> = mutableListOf()

    @Synchronized
    fun changeName(newName: String) {
        client.user.login = newName
    }

    var alive: Boolean = true
        private set

    init {
        start()
    }

    @Synchronized
    fun addTask(task: Intention) {
        taskQueue.add(task)
    }

    @Synchronized
    private fun getAllTasks(): List<Intention> {
        val res = mutableListOf<Intention>()
        res.addAll(taskQueue)
        taskQueue.clear()
        return res
    }

    @Synchronized
    private fun addMessage(task: UserAction) {
        receivedMessages.add(task)
    }

    @Synchronized
    fun getAllReceivedMessages(): List<UserAction> {
        val res = mutableListOf<UserAction>()
        res.addAll(receivedMessages)
        receivedMessages.clear()
        return res
    }

    var lastPingFromClient: LocalDateTime = LocalDateTime.now()

    val user: User
        get() = client.user

    private lateinit var readerThread: ReaderThread

    override fun run() {
        readerThread = ReaderThread()
        readerThread.start()
        listen {
            if (lastPingFromClient.isBefore(LocalDateTime.now().minusMinutes(1)))
                alive = false
            else sendAllIntentions()
        }
    }

    private fun sendAllIntentions() {
        val tasks = getAllTasks()
        tasks.forEach { client.outputStream.writeObject(it) }
    }

    private inner class ReaderThread : Thread() {
        override fun run() {
            listen {
                when (val action = client.inputStream.readObject()) {
                    !is UserAction -> doNothing()
                    is UserAction.Ping -> lastPingFromClient = LocalDateTime.now()
                    else -> addMessage(action)
                }
            }
        }
    }

    private fun listen(block: () -> Unit) {
        try {
            while (alive) {
                block.invoke()
                sleep(100)
            }
        } catch (e: Throwable) {
            alive = false
            e.printStackTrace()
        } finally {
            closeResources()
        }
    }

    private fun closeResources() {
        try {
            client.outputStream.close()
            client.inputStream.close()
            client.socket.close()
        } catch (e: Throwable) {
            e.printStackTrace()
            println("Could not close resources!")
        }
    }
}