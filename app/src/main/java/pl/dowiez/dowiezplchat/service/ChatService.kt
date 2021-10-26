package pl.dowiez.dowiezplchat.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.android.volley.VolleyError
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.Single
import pl.dowiez.dowiezplchat.data.ChatDatabase
import pl.dowiez.dowiezplchat.data.entities.Account
import pl.dowiez.dowiezplchat.data.entities.Message
import pl.dowiez.dowiezplchat.helpers.api.ApiHelper
import pl.dowiez.dowiezplchat.helpers.api.IGetAccountInfoCallback
import pl.dowiez.dowiezplchat.helpers.user.UserHelper
import java.text.SimpleDateFormat
import java.util.*

class ChatService : Service() {
    companion object {
        private const val CHAT_ENDPOINT: String = "https://dowiez.pl:5001/hubs/chat"

        private var hubConnection: HubConnection? = null
        private var chatServiceListener: IChatServiceListener? = null

        fun getState() : HubConnectionState? {
            return hubConnection?.connectionState
        }

        fun setChatServiceListener(listener: IChatServiceListener?) {
            chatServiceListener = listener
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
        hubConnection?.stop()
        hubConnection = null
        return super.onUnbind(intent)
    }

    fun startHubConnection() {
        hubConnection = HubConnectionBuilder.create(CHAT_ENDPOINT)
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
            "GroupJoin", { conversationId, accountId -> groupJoin(conversationId, accountId) },
            String::class.java,
            String::class.java
        )

        hubConnection!!.start()
    }

    private fun receiveMessage(conversationId: String, senderId: String, messageId: String, sentDate: String, message: String) {
        Log.d("ChatService", "Recieve: $conversationId, $senderId, $messageId, $message")
        val mess = Message(messageId, message, dateFormat.parse(sentDate), conversationId, senderId)
        ChatDatabase.instance!!.messageDao().insert(mess)
    }

    private fun groupJoin(conversationId: String, accountId: String) {
        Log.i("ChatService", "Account joining: $conversationId, $accountId")
        ApiHelper.getAccountInfo(applicationContext, accountId, object : IGetAccountInfoCallback {
            override fun onSuccess(account: Account) {
                ChatDatabase.instance!!.accountDao().insert(account)
            }

            override fun onError(error: VolleyError) {
                Log.e("ChatService", "Failed to get account info")
            }
        })
    }

    fun sendMessage(conversationId: String, message: String) {
        Log.i("ChatService", "Send: $conversationId, $message")
        hubConnection!!.send("SendToConversation", conversationId, message)
    }
}