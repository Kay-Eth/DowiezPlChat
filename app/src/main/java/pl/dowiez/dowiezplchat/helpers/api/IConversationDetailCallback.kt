package pl.dowiez.dowiezplchat.helpers.api

import pl.dowiez.dowiezplchat.data.entities.Account
import java.util.*
import kotlin.collections.ArrayList

interface IConversationDetailCallback : IApiCallback {
    fun onSuccess(
        conversationId: String,
        name: String,
        creationDate: Date,
        category: Int,
        lastMessage: String?,
        lastMessageDate: Date?,
        chatMembers: ArrayList<Account>
    )
}