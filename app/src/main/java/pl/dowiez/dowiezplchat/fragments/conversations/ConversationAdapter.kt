package pl.dowiez.dowiezplchat.fragments.conversations

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import pl.dowiez.dowiezplchat.R
import pl.dowiez.dowiezplchat.data.ChatDatabase
import pl.dowiez.dowiezplchat.data.entities.Conversation
import pl.dowiez.dowiezplchat.helpers.api.ApiHelper
import pl.dowiez.dowiezplchat.helpers.api.IConversationSmallDetailCallback
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(private val listener: IOnConversationClickListener) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(ConversationsComparator()) {
    companion object {
        lateinit var context: Context

        val time: SimpleDateFormat = SimpleDateFormat("HH:mm")
        val date: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
    }


    inner class ConversationViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val convIconIV: ImageView = view.findViewById(R.id.iconIV)
        val convNameTV: TextView = view.findViewById(R.id.conversationNameTV)
        val convTimeTV: TextView = view.findViewById(R.id.timeTV)
        val convLastTV: TextView = view.findViewById(R.id.conversationLastMessageTV)

        lateinit var conversation: Conversation

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onItemClick(conversation)
        }
    }

    class ConversationsComparator : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.conversationId == newItem.conversationId
        }

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.lastMessage == newItem.lastMessage
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val entryView = LayoutInflater.from(parent.context).inflate(R.layout.conversation_entry, parent, false)
        return ConversationViewHolder(entryView)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation: Conversation = getItem(position)
        holder.conversation = conversation

        when (conversation.type) {
            0 -> holder.convIconIV.setImageDrawable(context.getDrawable(R.drawable.ic_person))
            1 -> holder.convIconIV.setImageDrawable(context.getDrawable(R.drawable.ic_group))
            2 -> holder.convIconIV.setImageDrawable(context.getDrawable(R.drawable.ic_transport))
            else -> holder.convIconIV.setImageDrawable(context.getDrawable(R.drawable.ic_password))
        }

        if (conversation.name != null)
            holder.convNameTV.text = conversation.name
        if (conversation.lastMessageDate != null) {
            val calNow = Calendar.getInstance()
            calNow.time = Date()
            val calMessage = Calendar.getInstance()
            calMessage.time = conversation.lastMessageDate

            val sameDay =
                calNow.get(Calendar.DAY_OF_YEAR) == calMessage.get(Calendar.DAY_OF_YEAR) && calNow.get(
                    Calendar.YEAR
                ) == calMessage.get(Calendar.YEAR);
            if (sameDay) {
                holder.convTimeTV.text = time.format(conversation.lastMessageDate)
            } else {
                holder.convTimeTV.text = date.format(conversation.lastMessageDate)
            }
        }
        if (conversation.lastMessage != null)
            holder.convLastTV.text = conversation.lastMessage

//        ApiHelper.getSmallConversationDetail(context, conversation.conversationId, object : IConversationSmallDetailCallback {
//            override fun onSuccess(
//                conversationId: String,
//                name: String,
//                creationDate: Date,
//                category: Int,
//                lastMessage: String?,
//                lastMessageDate: Date?
//            ) {
//                ChatDatabase.instance!!.conversationDao().insert(
//                    Conversation(
//                        conversationId,
//                        name,
//                        category,
//                        creationDate,
//                        lastMessage,
//                        lastMessageDate
//                    )
//                )
//            }
//
//            override fun onError(error: VolleyError) {
//                Log.e("ConversationAdapter", "getSmallConversationDetail Failed")
//            }
//        })
    }

    interface IOnConversationClickListener {
        fun onItemClick(conversation: Conversation)
    }
}