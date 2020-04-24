package org.example.chat

import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

typealias Letter = String

data class Message(
    val login: String,
    val message: Letter,
    val time: LocalDateTime = LocalDateTime.now(),
    val users: List<String> = emptyList()
) : Serializable {

    private companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss:SSS")
        private const val PING_NAME = "ping"
        private const val PING_MESSAGE: Letter = "ping_letter"
        val Ping = Message(PING_NAME, PING_MESSAGE)
    }

    fun withLogin(login: String) = copy(login = login)
    fun withMessage(message: Letter) = copy(message = message)
    fun withTime(time: LocalDateTime) = copy(time = time)
    fun withUsers(users: List<String>) = copy(users = users)

    val strDate: String
        get() = formatter.format(time)

    fun isPing() = login == PING_NAME && message == PING_MESSAGE
}