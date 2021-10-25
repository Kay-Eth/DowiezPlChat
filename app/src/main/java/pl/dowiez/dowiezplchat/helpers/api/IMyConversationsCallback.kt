package pl.dowiez.dowiezplchat.helpers.api

import pl.dowiez.dowiezplchat.data.entities.Conversation

interface IMyConversationsCallback : IApiCallback {
    fun onSuccess(conversations: ArrayList<Conversation>)
}