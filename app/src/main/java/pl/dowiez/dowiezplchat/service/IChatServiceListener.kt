package pl.dowiez.dowiezplchat.service

import pl.dowiez.dowiezplchat.data.entities.Message

interface IChatServiceListener {
    fun onMessageReceived(message: Message)
}