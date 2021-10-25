package pl.dowiez.dowiezplchat.helpers.api

import java.util.*

interface IConversationSmallDetailCallback : IApiCallback {
    fun onSuccess(
        conversationId: String,
        name: String,
        creationDate: Date,
        category: Int,
        lastMessage: String?,
        lastMessageDate: Date?
    )
}