package pl.dowiez.dowiezplchat.data.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class AccountWithConversations(
    @Embedded val account: Account,
    @Relation(
        parentColumn = "accountId",
        entityColumn = "conversationId",
        associateBy = Junction(ConversationAccountCrossRef::class)
    )
    val conversations: List<Conversation>
)
