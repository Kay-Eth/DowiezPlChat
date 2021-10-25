package pl.dowiez.dowiezplchat.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pl.dowiez.dowiezplchat.data.daos.AccountDao
import pl.dowiez.dowiezplchat.data.daos.ConversationDao
import pl.dowiez.dowiezplchat.data.daos.MessageDao
import pl.dowiez.dowiezplchat.data.entities.Account
import pl.dowiez.dowiezplchat.data.entities.Conversation
import pl.dowiez.dowiezplchat.data.entities.ConversationAccountCrossRef
import pl.dowiez.dowiezplchat.data.entities.Message

@Database(entities = [Account::class, Conversation::class, Message::class, ConversationAccountCrossRef::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object {
        var instance: ChatDatabase? = null
        private const val DB_NAME = "ChatDatabase"

        fun initDatabase(context: Context) {
            if (instance == null) {
                synchronized(ChatDatabase::class.java) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(context.applicationContext, ChatDatabase::class.java, DB_NAME)
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
        }
    }
}