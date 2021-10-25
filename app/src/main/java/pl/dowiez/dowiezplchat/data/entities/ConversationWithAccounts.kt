package pl.dowiez.dowiezplchat.data.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ConversationWithAccounts(
    @Embedded val conversation: Conversation,
    @Relation(
        parentColumn = "conversationId",
        entityColumn = "accountId",
        associateBy = Junction(ConversationAccountCrossRef::class)
    )
    val accounts: List<Account>
)
