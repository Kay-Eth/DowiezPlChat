package pl.dowiez.dowiezplchat.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Conversation(
    @PrimaryKey val conversationId: String,
    val name: String?,
    val type: Int,
    val created: Date,
    val lastMessage: String?,
    val lastMessageDate: Date?
)
