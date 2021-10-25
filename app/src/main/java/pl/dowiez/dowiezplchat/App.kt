package pl.dowiez.dowiezplchat

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import pl.dowiez.dowiezplchat.data.ChatDatabase
import pl.dowiez.dowiezplchat.data.ChatRepository
import pl.dowiez.dowiezplchat.data.ConversationRepository

class App : Application() {
    companion object {
        const val CHANNEL_ID = "dowiezPlChatServiceChannel"
    }

    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy {
        ChatDatabase.initDatabase(this)
        ChatDatabase.instance!!
    }

    val conversationRepository by lazy {
        ConversationRepository(database.conversationDao())
    }

    override fun onCreate() {
        super.onCreate()

        ChatDatabase.initDatabase(this)
    }
}