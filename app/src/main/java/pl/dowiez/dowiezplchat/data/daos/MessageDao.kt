package pl.dowiez.dowiezplchat.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.dowiez.dowiezplchat.data.entities.Message
import pl.dowiez.dowiezplchat.data.entities.MessageAndAccount

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg messages: Message)

//    @Query("SELECT * FROM Message WHERE conversationId = :conversationId")
//    fun getFromConv(conversationId: String): Flow<List<Message>>

    @Transaction
    @Query("SELECT * FROM Message WHERE conversationId = :conversationId ORDER BY sendDate ASC")
    fun getFromConv(conversationId: String): Flow<List<Message>>
}