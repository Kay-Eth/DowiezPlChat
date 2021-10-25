package pl.dowiez.dowiezplchat.data

import android.app.Application
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import pl.dowiez.dowiezplchat.data.daos.AccountDao
import pl.dowiez.dowiezplchat.data.daos.ConversationDao
import pl.dowiez.dowiezplchat.data.daos.MessageDao
import pl.dowiez.dowiezplchat.data.entities.Conversation
import pl.dowiez.dowiezplchat.data.entities.ConversationWithAccounts

class ConversationRepository(private val conversationDao: ConversationDao) {
    private var allConversations: Flow<List<Conversation>> = conversationDao.getAll()

    @WorkerThread
    suspend fun insert(conversation: Conversation) {
        conversationDao.insert(conversation)
    }

    fun delete(conversation: Conversation) {

    }

    fun getAll(): Flow<List<Conversation>> {
        return allConversations
    }
}