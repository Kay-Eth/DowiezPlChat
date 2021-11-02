package pl.dowiez.dowiezplchat.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.dowiez.dowiezplchat.data.entities.Conversation
import pl.dowiez.dowiezplchat.data.entities.Account
import pl.dowiez.dowiezplchat.data.entities.ConversationAccountCrossRef
import pl.dowiez.dowiezplchat.data.entities.ConversationWithAccounts

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(conversation: Conversation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCross(conversationAccountCrossRef: ConversationAccountCrossRef)

    @Query("SELECT * FROM Conversation ORDER BY lastMessageDate DESC")
    fun getAll(): Flow<List<Conversation>>

    @Transaction
    @Query("SELECT * FROM Conversation WHERE conversationId = :conversationId")
    fun get(conversationId: String): Flow<ConversationWithAccounts>

    @Query("SELECT * FROM Conversation WHERE conversationId = :conversationId")
    fun getSingle(conversationId: String): Conversation

    @Delete
    fun delete(conversation: Conversation)
}