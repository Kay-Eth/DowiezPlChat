package pl.dowiez.dowiezplchat.helpers.api

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import pl.dowiez.dowiezplchat.data.entities.Account
import pl.dowiez.dowiezplchat.data.entities.Conversation
import pl.dowiez.dowiezplchat.data.entities.Message
import pl.dowiez.dowiezplchat.helpers.user.UserHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object ApiHelper {
    private const val API_BASE_URL: String = "https://dowiez.pl:5001/api/"

    private const val ACCOUNTS_LOGIN_ENDPOINT: String = "accounts/login"
    private const val ACCOUNTS_MY_ENDPOINT: String = "accounts/my"
    private const val ACCOUNT_GET_SMALL_ENDPOINT: String = "accounts/<ACCID>/small"

    private const val CONVERSATIONS_MY_ENDPOINT: String = "conversations/my"
    private const val CONVERSATIONS_DETAILS_ENDPOINT: String = "conversations/"
    private const val CONVERSATIONS_DETAILS_SMALL_ENDPOINT: String = "conversations/<CONVID>/small"

    private const val MESSAGES_CONVERSATION_ENDPOINT: String = "messages/conversation/"
    private const val MESSAGES_CONVERSATION_SMALL_ENDPOINT: String = "messages/conversation/<CONVID>/small"
    private const val MESSAGES_CONVERSATION_LAST_ENDPOINT: String = "messages/conversation/<CONVID>/last"
    private const val MESSAGES_CONVERSATION_AFTER_ENDPOINT: String = "messages/conversation/<CONVID>/after/"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS").apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun login(context: Context, email: String, password: String, callback: ILoginCallback) {
        val url = API_BASE_URL + ACCOUNTS_LOGIN_ENDPOINT
        var token = ""

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            JSONObject("{ \"email\": \"${email}\", \"password\": \"${password}\" }"),
            { response ->
                token = response["token"].toString()
                UserHelper.token = token
                UserHelper.saveToken(context)

                getMyAccountInfo(context, object : IMyAccountCallback {
                    override fun onSuccess() {
                        callback.onSuccess()
                    }

                    override fun onError(error: VolleyError) {
                        callback.onError(error)
                    }
                })
            },
            { error ->
                error.message?.let { Log.e("ApiHelper", it) }

                callback.onError(error)
            }
        )

        RequestsSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest)
    }

    fun getMyAccountInfo(context: Context, callback: IMyAccountCallback) {
        val url = API_BASE_URL + ACCOUNTS_MY_ENDPOINT

        val jsonObjectRequest = AuthorizedJsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                val accountId = response["accountId"].toString()
                val email = response["email"].toString()
                val firstName = response["firstName"].toString()
                val lastName = response["lastName"].toString()

                UserHelper.accountId = accountId
                UserHelper.email = email
                UserHelper.firstName = firstName
                UserHelper.lastName = lastName

                UserHelper.saveId(context)

                callback.onSuccess()
            }
        ) { error ->
            error.message?.let { Log.e("ApiHelper", it) }

            callback.onError(error)
        }

        RequestsSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest)
    }

    fun getAccountInfo(context: Context, accountId: String, callback: IGetAccountInfoCallback) {
        val url = API_BASE_URL + ACCOUNT_GET_SMALL_ENDPOINT.replace("<ACCID>", accountId)

        val jsonObjectRequest = AuthorizedJsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                val accountId = response["accountId"].toString()
                val email = response["email"].toString()
                val firstName = response["firstName"].toString()
                val lastName = response["lastName"].toString()

                callback.onSuccess(
                    Account(
                        accountId, email, firstName, lastName
                    )
                )
            }
        ) { error ->
            error.message?.let { Log.e("ApiHelper", it) }

            callback.onError(error)
        }

        RequestsSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest)
    }

    fun getMyConversations(context: Context, callback: IMyConversationsCallback) {
        val url = API_BASE_URL + CONVERSATIONS_MY_ENDPOINT
        val jsonArrayRequest = AuthorizedJsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                val list = ArrayList<Conversation>()

                for (i in 0 until response.length()) {
                    val item = response.getJSONObject(i)
                    list.add(
                        Conversation(
                            item["conversationId"].toString(),
                            null,
                            when (item["category"].toString()) {
                                "Normal" -> 0
                                "Group" -> 1
                                "Transport" -> 2
                                else -> -1
                            },
                            dateFormat.parse(item["creationDate"].toString()),
                            null,
                            null
                        )
                    )
                }

                callback.onSuccess(list)
            },
            { error ->
                error.message?.let { Log.e("ApiHelper", it) }

                callback.onError(error)
            }
        )
        RequestsSingleton.getInstance(context).addToRequestQueue(jsonArrayRequest)
    }

    fun getSmallConversationDetail(context: Context, conversationId: String, callback: IConversationSmallDetailCallback) {
        val url = API_BASE_URL + CONVERSATIONS_DETAILS_SMALL_ENDPOINT.replace("<CONVID>", conversationId)

        val jsonObjectRequest = AuthorizedJsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                val name = response["name"].toString()

                val type = when (response["category"].toString()) {
                    "Normal" -> 0
                    "Group" -> 1
                    "Transport" -> 2
                    else -> -1
                }

                val created = dateFormat.parse(response["creationDate"].toString())

                val lastMessage: String? = when (response.isNull("lastMessage")) {
                    true -> "Jeszcze nic nie wysłano..."
                    false -> response.getJSONObject("lastMessage").let {
                        val sender = it.getJSONObject("sender")
                        "${sender["firstName"]}: ${it["content"]}"
                    }
                }

                val lastMessageDate: Date? = when (response.isNull("lastMessage")) {
                    true -> created
                    false -> response.getJSONObject("lastMessage").let {
                        dateFormat.parse(it["sentDate"].toString())
                    }
                }

                callback.onSuccess(conversationId, name, created, type, lastMessage, lastMessageDate)
            },
            { error ->
                error.message?.let { Log.e("ApiHelper", it) }

                callback.onError(error)
            }
        )

        RequestsSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest)
    }

    fun getConversationDetail(context: Context, conversationId: String, callback: IConversationDetailCallback) {
        val url = API_BASE_URL + CONVERSATIONS_DETAILS_ENDPOINT + conversationId

        val jsonObjectRequest = AuthorizedJsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                val name = response["name"].toString()

                val type = when (response["category"].toString()) {
                    "Normal" -> 0
                    "Group" -> 1
                    "Transport" -> 2
                    else -> -1
                }

                val created = dateFormat.parse(response["creationDate"].toString())

                val lastMessage: String? = when (response.isNull("lastMessage")) {
                    true -> "Jeszcze nic nie wysłano..."
                    false -> response.getJSONObject("lastMessage").let {
                        val sender = it.getJSONObject("sender")
                        "${sender["firstName"]}: ${it["content"]}"
                    }
                }

                val lastMessageDate: Date? = when (response.isNull("lastMessage")) {
                    true -> created
                    false -> response.getJSONObject("lastMessage").let {
                        dateFormat.parse(it["sentDate"].toString())
                    }
                }

                val chatMembers: ArrayList<Account> = ArrayList<Account>()
                val array = response.getJSONArray("chatMembers")
                for (i in 0 until array.length()) {
                    val current = array.getJSONObject(i)
                    chatMembers.add(Account(
                        current["accountId"].toString(),
                        current["email"].toString(),
                        current["firstName"].toString(),
                        current["lastName"].toString()
                    ))
                }

                callback.onSuccess(conversationId, name, created, type, lastMessage, lastMessageDate, chatMembers)
            },
            { error ->
                error.message?.let { Log.e("ApiHelper", it) }

                callback.onError(error)
            }
        )

        RequestsSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest)
    }

    fun getAllMessages(context: Context, conversationId: String, callback: IMessagesCallback) {
        val url = API_BASE_URL + MESSAGES_CONVERSATION_ENDPOINT + conversationId
        val jsonArrayRequest = AuthorizedJsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                val list = ArrayList<Message>()

                for (i in 0 until response.length()) {
                    val item = response.getJSONObject(i)
                    list.add(
                        Message(
                            item["messageId"].toString(),
                            item["content"].toString(),
                            dateFormat.parse(item["sentDate"].toString())!!,
                            conversationId,
                            item["senderId"].toString()
                        )
                    )
                }

                callback.onSuccess(list)
            },
            { error ->
                error.message?.let { Log.e("ApiHelper", it) }

                callback.onError(error)
            }
        )
        RequestsSingleton.getInstance(context).addToRequestQueue(jsonArrayRequest)
    }

    fun getAfterMessages(context: Context, conversationId: String, messageId: String, callback: IMessagesCallback) {
        val url = API_BASE_URL + MESSAGES_CONVERSATION_AFTER_ENDPOINT + messageId
        val jsonArrayRequest = AuthorizedJsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                val list = ArrayList<Message>()

                for (i in 0 until response.length()) {
                    val item = response.getJSONObject(i)
                    list.add(
                        Message(
                            item["messageId"].toString(),
                            item["content"].toString(),
                            dateFormat.parse(item["sentDate"].toString())!!,
                            conversationId,
                            item["senderId"].toString()
                        )
                    )
                }

                callback.onSuccess(list)
            },
            { error ->
                error.message?.let { Log.e("ApiHelper", it) }

                callback.onError(error)
            }
        )
        RequestsSingleton.getInstance(context).addToRequestQueue(jsonArrayRequest)
    }
}