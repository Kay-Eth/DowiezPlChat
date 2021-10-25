package pl.dowiez.dowiezplchat.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import pl.dowiez.dowiezplchat.data.daos.ConversationDao
import pl.dowiez.dowiezplchat.data.daos.MessageDao
import pl.dowiez.dowiezplchat.data.entities.Conversation
import pl.dowiez.dowiezplchat.data.entities.ConversationWithAccounts
import pl.dowiez.dowiezplchat.data.entities.Message
import pl.dowiez.dowiezplchat.data.entities.MessageAndAccount

class ChatRepository(private val conversationId: String, private val conversationDao: ConversationDao, private val messageDao: MessageDao) {
    private var conversation: Flow<ConversationWithAccounts> = conversationDao.get(conversationId)
    private var allMessages: Flow<List<Message>> = messageDao.getFromConv(conversationId)

    @WorkerThread
    suspend fun insert(message: Message) {
        messageDao.insert(message)
    }

    fun getDetails(): Flow<ConversationWithAccounts> {
        return conversation
    }

    fun getAll(): Flow<List<Message>> {
        return allMessages
    }
}