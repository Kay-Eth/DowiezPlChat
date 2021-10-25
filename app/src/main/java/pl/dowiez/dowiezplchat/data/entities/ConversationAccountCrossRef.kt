package pl.dowiez.dowiezplchat.data.entities

import androidx.room.Entity

@Entity(primaryKeys = ["conversationId", "accountId"])
data class ConversationAccountCrossRef(
    val conversationId: String,
    val accountId: String
)
