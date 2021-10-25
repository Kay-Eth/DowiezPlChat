package pl.dowiez.dowiezplchat.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Account(
    @PrimaryKey val accountId: String,
    val email: String,
    val firstName: String,
    val lastName: String
)
