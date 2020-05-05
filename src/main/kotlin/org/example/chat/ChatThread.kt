package org.example.chat

import org.example.doNothing
import org.example.connections.ClientThread
import org.example.connections.Intention
import org.example.connections.User
import org.example.connections.UserAction
import org.example.`try`
import java.util.*

class ChatThread() : Thread() {

    init {
        start()
    }

    companion object {
        private const val PERIOD = 1000L
    }

    private val timer = Timer()

    private val clients: MutableList<ClientThread> = mutableListOf() //TODO move to independent class!

    private val otherChats: MutableList<Intention.Chat> = mutableListOf()


    private var mainChat: Intention.Chat = Intention.Chat("", "Main Chat", mutableListOf())

    @Synchronized
    fun addClient(clientThread: ClientThread) {
        clients.add(clientThread)
    }

    @Synchronized
    fun removeClient(userId: String) {
        val i = clients.indexOfFirst { it.user.uuid == userId }
        clients.removeAt(i)
    }

    override fun run() {

        `try` { timer.scheduleAtFixedRate(ChatTimerTask(), 0, PERIOD) }

        try {
            while (true) {
                collectAllMessages()
                sleep(100)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun collectAllMessages() {
        val forRemove = mutableListOf<User>()
        forEachClient { client ->
            if (!client.alive) forRemove.add(client.user)
            else client.getAllReceivedMessages().forEach { workAroundReceived(it, client.user) }
        }
        forRemove.forEach {user->
            removeClient(user.uuid)
            leaveMainChat(user)
            otherChats.forEach { leaveChat(it.chatId, user) }
        }
    }

    private fun workAroundReceived(action: UserAction, fromWho: User) {
        when (action) {
            is UserAction.Message -> workAroundMessage(action, fromWho)
            is UserAction.Login -> login(action.user)
            is UserAction.CloseChat -> closeChat(action.chatId)
            is UserAction.JoinUser -> joinUser(action)
            is UserAction.LeaveChat -> leaveChat(action.chatId, fromWho)
            is UserAction.CreateChat -> createChat(action.chatName, fromWho)
            UserAction.Ping -> doNothing()
        }
    }

    private fun login(user: User) {
        forClientWithUserId(user.uuid) { it.changeName(user.login) }
        mainChat.users.add(user)
        forClientWithUserId(user.uuid) { it.addTask(mainChat) }
        val userJoined = UserAction.JoinUser(mainChat.chatId, user)
        mainChat.users.sendUserJoined(userJoined)
    }

    private fun createChat(chatName: String, who: User) {
        val chat = Intention.Chat(
            chatId = UUID.randomUUID().toString(),
            chatName = chatName,
            users = mutableListOf(who)
        )

        otherChats.add(chat)
        forClientWithUserId(who.uuid) { client -> client.addTask(chat) }
    }

    private fun leaveChat(chatId: String, who: User) {
        val chat = otherChats.firstOrNull { it.chatId == chatId } ?: return
        chat.users.removeIf { it.uuid == who.uuid }
        forClientWithUserId(who.uuid) { client -> client.addTask(Intention.ChatClosed(chat.chatId)) }
        if (chat.users.isEmpty())
            otherChats.removeIf { it.chatId == chat.chatId }
        else {
            val intention = Intention.UserLeft(chatId, who)
            chat.users.forEach { forClientWithUserId(it.uuid) { client -> client.addTask(intention) } }
        }
    }

    private fun leaveMainChat(who: User) {
        mainChat.users.removeIf { it.uuid == who.uuid }
        forClientWithUserId(who.uuid) { client -> client.addTask(Intention.ChatClosed("")) }
        val intention = Intention.UserLeft("", who)
        mainChat.users.forEach { forClientWithUserId(it.uuid) { client -> client.addTask(intention) } }
    }

    private fun joinUser(action: UserAction.JoinUser) {
        val chat = otherChats.firstOrNull { it.chatId == action.chatId } ?: return
        chat.users.add(action.user)
        forClientWithUserId(action.user.uuid) { client -> client.addTask(chat) }
        if (action.chatId.isNotEmpty())
            otherChats.firstOrNull { it.chatId == action.chatId }?.users?.sendUserJoined(action)
    }

    private fun List<User>.sendUserJoined(action: UserAction.JoinUser) = forEach {
        val intention = Intention.UserJoined(
            chatId = action.chatId,
            user = action.user
        )
        forClientWithUserId(it.uuid) { client -> client.addTask(intention) }
    }

    private fun closeChat(chatId: String) {
        if (chatId.isNotEmpty())
            otherChats.firstOrNull { it.chatId == chatId }?.users?.chatWasClosed(chatId)
        otherChats.removeIf { it.chatId == chatId }
    }

    private fun List<User>.chatWasClosed(chatId: String) = forEach {
        val intention = Intention.ChatClosed(
            chatId = chatId
        )
        forClientWithUserId(it.uuid) { client -> client.addTask(intention) }
    }

    private fun workAroundMessage(message: UserAction.Message, fromWho: User) {

        val intention = Intention.Message(
            chatId = message.chatId,
            user = fromWho,
            message = message.message
        )

        if (message.chatId.isEmpty()) mainChat.users.sendMessage(intention)
        else otherChats.firstOrNull { it.chatId == message.chatId }?.users?.sendMessage(intention)
    }

    private fun List<User>.sendMessage(message: Intention.Message) = forEach {
        forClientWithUserId(it.uuid) { client -> client.addTask(message) }
    }

    @Synchronized
    private fun forClientWithUserId(userId: String, block: (ClientThread) -> Unit) {
        clients.firstOrNull { it.user.uuid == userId }?.let { block(it) }
    }

    private fun forEachClient(block: (ClientThread) -> Unit) {
        var i = 0
        while (true) {
            val client = getNextClient(i) ?: break
            block(client)
            i++
        }
    }

    @Synchronized
    fun getNextClient(index: Int): ClientThread? {
        require(index >= 0)
        return if (index in clients.indices) clients[index]
        else null
    }

    private inner class ChatTimerTask : TimerTask() {
        override fun run() {
            forEachClient { it.addTask(Intention.Ping) }
        }
    }

}