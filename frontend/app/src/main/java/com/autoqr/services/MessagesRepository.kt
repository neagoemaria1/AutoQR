package com.autoqr.services

object MessagesRepository {
    private val messages = mutableListOf<String>()
    fun getAllMessages(): List<String> = messages
    fun saveMessage(msg: String) { messages.add(0, msg) }
}
