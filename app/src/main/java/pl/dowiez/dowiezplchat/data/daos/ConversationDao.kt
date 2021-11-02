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

    @Delete
    fun remove(conversation: Conversation)

    @Query("DELETE FROM ConversationAccountCrossRef WHERE conversationId = :conversationId AND accountId = :accountId")
    fun removeCross(conversationId: String, accountId: String)

    @Query("SELECT * FROM Conversation ORDER BY lastMessageDate DESC")
    fun getAll(): Flow<List<Conversation>>

    @Query("SELECT * FROM Conversation")
    fun getAllSlow(): List<Conversation>

    @Transaction
    @Query("SELECT * FROM Conversation WHERE conversationId = :conversationId")
    fun get(conversationId: String): Flow<ConversationWithAccounts>

    @Transaction
    @Query("SELECT * FROM Conversation WHERE conversationId = :conversationId")
    fun getSlow(conversationId: String): ConversationWithAccounts

    @Query("SELECT * FROM Conversation WHERE conversationId = :conversationId")
    fun getSingle(conversationId: String): Conversation

    @Delete
    fun delete(conversation: Conversation)
}