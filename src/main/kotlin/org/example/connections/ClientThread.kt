package org.example.connections

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

    private lateinit var readerThread : ReaderThread

    override fun run() {
        try {
            readerThread = ReaderThread()
            readerThread.start()
            while (alive) {
                if (lastPingFromClient.isBefore(LocalDateTime.now().minusMinutes(1))) {
                    alive = false
                    break
                }
                sendAllIntentions()
                sleep(100)
            }
        } catch (e: Throwable) {
            alive = false
            e.printStackTrace()
            System.err.println("$user left with error!")
        } finally {
            try {
                client.inputStream.close()
                client.outputStream.close()
                client.socket.close()
            } catch (e: Throwable) {
                e.printStackTrace()
                println("Could not close resources!")
            }
        }
    }

    private fun sendAllIntentions() {
        val tasks = getAllTasks()
        tasks.forEach { client.outputStream.writeObject(it) }
    }

    private inner class ReaderThread : Thread() {
        override fun run() {
            try {
                while (alive) {
                    val action = client.inputStream.readObject()
                    if (action !is UserAction) continue
                    if (action is UserAction.Ping) lastPingFromClient = LocalDateTime.now()
                    else addMessage(action)
                    sleep(100)
                }
            } catch (e: Throwable) {
                alive = false//TODO leave all chats user action!
                e.printStackTrace()
            } finally {
                try {
                    client.inputStream.close()
                    client.outputStream.close()
                    client.socket.close()
                } catch (e: Throwable) {
                    e.printStackTrace()
                    println("Could not close resources!")
                }
            }
        }
    }
}