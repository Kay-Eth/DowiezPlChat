package pl.dowiez.dowiezplchat.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.VolleyError
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.CompletableObserver
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import pl.dowiez.dowiezplchat.MainActivity
import pl.dowiez.dowiezplchat.R
import pl.dowiez.dowiezplchat.data.ChatDatabase
import pl.dowiez.dowiezplchat.data.entities.Account
import pl.dowiez.dowiezplchat.data.entities.Conversation
import pl.dowiez.dowiezplchat.data.entities.ConversationAccountCrossRef
import pl.dowiez.dowiezplchat.data.entities.Message
import pl.dowiez.dowiezplchat.helpers.api.ApiHelper
import pl.dowiez.dowiezplchat.helpers.api.IConversationDetailCallback
import pl.dowiez.dowiezplchat.helpers.api.IGetAccountInfoCallback
import pl.dowiez.dowiezplchat.helpers.user.UserHelper
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatService : Service() {
    companion object {
        private const val CHANNEL_MESSAGES_ID: String = "DowiezPlChatServiceMessagesChannel"
        private const val CHANNEL_CONVERSATIONS_ID: String = "DowiezPlChatServiceConversationsChannel"

        private const val CHAT_ENDPOINT: String = "https://dowiez.pl:5001/hubs/chat"

        private var hubConnection: HubConnection? = null

        fun getState() : HubConnectionState? {
            return hubConnection?.connectionState
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS").apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    inner class ChatBinder : Binder() {
        fun getService(): ChatService = this@ChatService
    }

    private val binder = ChatBinder()

    override fun onCreate() {
        super.onCreate()
        createMessageNotificationChannel()
        createConversationsNotificationChannel()
        Log.i("Service", "Service onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        Log.i("Service", "Service onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
//        hubConnection?.stop()
//        hubConnection = null
        return super.onUnbind(intent)
    }

    fun startHubConnection() {
        Log.i("ChatService", "Start Hub Connection...")

        hubConnection = HubConnectionBuilder.create(CHAT_ENDPOINT)
            .shouldSkipNegotiate(false)
            .withAccessTokenProvider(Single.defer {
                Single.just(
                    UserHelper.token
                )
            }).build()

        hubConnection!!.on(
            "Send", { conversationId, senderId, messageId, sentDate, message -> receiveMessage(conversationId, senderId, messageId, sentDate, message) },
            String::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
            String::class.java
        )

        hubConnection!!.on(
            "ChatJoin", { conversationId, accountId -> groupJoin(conversationId, accountId) },
            String::class.java,
            String::class.java
        )

        hubConnection!!.on(
            "ChatLeave", { conversationId, accountId -> groupLeave(conversationId, accountId) },
            String::class.java,
            String::class.java
        )

        hubConnection!!.on(
            "JoinConv", { conversationId -> joinConv(conversationId) },
            String::class.java
        )

        hubConnection!!.on(
            "LeaveConv", { conversationId -> leaveConv(conversationId) },
            String::class.java
        )

        hubConnection!!.on(
            "ChatRemoved", { conversationId -> convRemoved(conversationId) },
            String::class.java
        )

        hubConnection!!.onClosed {
            Log.e("ChatService", "Closed. Reconnecting...")
            while (hubConnection!!.connectionState != HubConnectionState.CONNECTED) {
                startConnection()
                Thread.sleep(5000)
            }
        }

        startConnection()
    }

    fun endHubConnection() {
        hubConnection!!.stop()
    }

    private fun startConnection() {
        hubConnection!!.start().subscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onComplete() {
                Log.i("ChatService", "Connected")
            }

            override fun onError(e: Throwable) {
                Log.e("ChatService", "Connection failed: " + e.message)
            }
        })
    }

    private fun receiveMessage(conversationId: String, senderId: String, messageId: String, sentDate: String, message: String) {
        Log.d("ChatService", "Recieve: $conversationId, $senderId, $messageId, $message")
        val mess = Message(messageId, message, dateFormat.parse(sentDate), conversationId, senderId)
        ChatDatabase.instance!!.messageDao().insert(mess)
        ChatDatabase.instance!!.conversationDao().getSingle(conversationId)?.also { it ->

            if (senderId != UserHelper.accountId) {
                if (MainActivity.isActivityVisible())
                {
                    if (MainActivity.getConversationId() != conversationId)
                        it.name?.let { it1 -> createMessageNotification(conversationId, it1, message) }
                }
                else
                {
                    it.name?.let { it1 -> createMessageNotification(conversationId, it1, message) }
                }
            }

            ChatDatabase.instance!!.conversationDao().insert(
                Conversation(
                    it.conversationId,
                    it.name,
                    it.type,
                    it.created,
                    message,
                    dateFormat.parse(sentDate)
                )
            )
        }
    }

    private fun groupJoin(conversationId: String, accountId: String) {
        Log.i("ChatService", "Account $accountId joining $conversationId, ")
        ApiHelper.getAccountInfo(applicationContext, accountId, object : IGetAccountInfoCallback {
            override fun onSuccess(account: Account) {
                Log.i("ChatService", "Got account info: ${account.accountId}")
                ChatDatabase.instance!!.accountDao().insert(account)
            }

            override fun onError(error: VolleyError) {
                Log.e("ChatService", "Failed to get account info")
            }
        })
    }

    private fun groupLeave(conversationId: String, accountId: String) {
        Log.i("ChatService", "Account $accountId leaving $conversationId, ")
        ChatDatabase.instance!!.conversationDao().removeCross(conversationId, accountId)
    }

    private fun joinConv(conversationId: String) {
        ApiHelper.getConversationDetail(applicationContext, conversationId, object : IConversationDetailCallback {
            override fun onSuccess(
                conversationId: String,
                name: String,
                creationDate: Date,
                category: Int,
                lastMessage: String?,
                lastMessageDate: Date?,
                chatMembers: ArrayList<Account>
            ) {
                chatMembers.forEach {
                    ChatDatabase.instance!!.accountDao().insert(it)
                }

                ChatDatabase.instance!!.conversationDao().insert(Conversation(
                    conversationId,
                    name,
                    category,
                    creationDate,
                    lastMessage,
                    lastMessageDate
                ))

                chatMembers.forEach {
                    ChatDatabase.instance!!.conversationDao().insertCross(
                        ConversationAccountCrossRef(
                            conversationId, it.accountId
                        )
                    )
                }

                createConversationNotification(conversationId, name, "Dołączono do konwersacji!", category)
            }

            override fun onError(error: VolleyError) {
                Log.e("ChatService", "Failed to get conversation details")
            }
        })
    }

    private fun leaveConv(conversationId: String) {
        val conversationWithAccounts = ChatDatabase.instance!!.conversationDao().getSlow(conversationId)
        ChatDatabase.instance!!.messageDao().removeFromConv(conversationWithAccounts.conversation.conversationId)
        conversationWithAccounts.accounts.forEach {
            ChatDatabase.instance!!.conversationDao().removeCross(conversationId, it.accountId)
        }
        conversationWithAccounts.conversation.name?.let {
            createConversationNotification(conversationId,
                it, "Opuszczono konwersację!", conversationWithAccounts.conversation.type)
        }
        ChatDatabase.instance!!.conversationDao().remove(conversationWithAccounts.conversation)
    }

    private fun convRemoved(conversationId: String) {
        val conversationWithAccounts = ChatDatabase.instance!!.conversationDao().getSlow(conversationId)
        ChatDatabase.instance!!.messageDao().removeFromConv(conversationWithAccounts.conversation.conversationId)
        conversationWithAccounts.accounts.forEach {
            ChatDatabase.instance!!.conversationDao().removeCross(conversationId, it.accountId)
        }
        conversationWithAccounts.conversation.name?.let {
            createConversationNotification(conversationId,
                it, "Konwersacja zakończona!", conversationWithAccounts.conversation.type)
        }
        ChatDatabase.instance!!.conversationDao().remove(conversationWithAccounts.conversation)
    }

    fun invokeSendMessage(conversationId: String, message: String, callback: IChatInvokeSendCallback) {
        Log.i("ChatService", "Invoke Send: $conversationId, $message")
        if (hubConnection!!.connectionState != HubConnectionState.CONNECTED)
            callback.onError(Exception("Hub Connection not started yet"))

        hubConnection!!.invoke(Boolean::class.java, "SendToConversation", conversationId, message)
            .subscribe(object : SingleObserver<Boolean> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Boolean) {
                    Log.i("ChatService", "Success")
                    callback.onSuccess()
                }

                override fun onError(e: Throwable) {
                    Log.i("ChatService", "Error")
                    callback.onError(e)
                }
            })
    }

    private fun createMessageNotification(conversationId: String, title: String, text: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MainActivity.INTENT_CONVERSATION_ID_KEY, conversationId)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        var builder = NotificationCompat.Builder(this, CHANNEL_MESSAGES_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(conversationId, 0, builder.build())
        }
    }

    private fun createConversationNotification(conversationId: String, title: String, text: String, type: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MainActivity.INTENT_CONVERSATION_ID_KEY, conversationId)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        var builder = NotificationCompat.Builder(this, CHANNEL_CONVERSATIONS_ID).apply {
            when (type) {
                0 -> setSmallIcon(R.drawable.ic_person)
                1 -> setSmallIcon(R.drawable.ic_group)
                2 -> setSmallIcon(R.drawable.ic_transport)
                else -> setSmallIcon(R.drawable.ic_conversation)
            }
            setContentTitle(title)
            setContentText(text)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }

        with(NotificationManagerCompat.from(this)) {
            notify(conversationId, 0, builder.build())
        }
    }

    private fun createMessageNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_messages_name)
            val descriptionText = getString(R.string.channel_messages_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_MESSAGES_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createConversationsNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_conversations_name)
            val descriptionText = getString(R.string.channel_conversations_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_CONVERSATIONS_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}