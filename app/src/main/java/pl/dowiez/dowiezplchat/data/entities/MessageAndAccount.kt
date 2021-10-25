package pl.dowiez.dowiezplchat.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class MessageAndAccount(
    @Embedded val message: Message,
    @Relation(
        parentColumn = "messageId",
        entityColumn = "accountId"
    )
    val account: Account
)
