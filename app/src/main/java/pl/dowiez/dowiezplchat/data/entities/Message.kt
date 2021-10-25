package pl.dowiez.dowiezplchat.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Message(
    @PrimaryKey val messageId: String,
    val content: String,
    val sendDate: Date,
    val conversationId: String,
    val senderId: String
)
