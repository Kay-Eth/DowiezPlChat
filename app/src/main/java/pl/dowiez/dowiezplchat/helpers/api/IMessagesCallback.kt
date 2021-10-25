package pl.dowiez.dowiezplchat.helpers.api

import pl.dowiez.dowiezplchat.data.entities.Message

interface IMessagesCallback : IApiCallback {
    fun onSuccess(messages: ArrayList<Message>)
}