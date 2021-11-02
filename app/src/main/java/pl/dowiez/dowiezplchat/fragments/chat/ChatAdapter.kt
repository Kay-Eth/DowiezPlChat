package pl.dowiez.dowiezplchat.fragments.chat

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.room.Database
import pl.dowiez.dowiezplchat.R
import pl.dowiez.dowiezplchat.data.ChatDatabase
import pl.dowiez.dowiezplchat.data.daos.AccountDao
import pl.dowiez.dowiezplchat.data.entities.Account
import pl.dowiez.dowiezplchat.data.entities.Message
import pl.dowiez.dowiezplchat.helpers.user.UserHelper

import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter : ListAdapter<Message, ChatAdapter.MessageViewHolder>(MessagesComparator()) {
    companion object {
        lateinit var context: Context

        const val MSG_TYPE_LEFT = 0
        const val MSG_TYPE_RIGHT = 1

        val time: SimpleDateFormat = SimpleDateFormat("HH:mm")
        val date: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
    }

    lateinit var accounts: List<Account>

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageIconIV: ImageView? = view.findViewById(R.id.messageIconTV)
        val messageSenderTV: TextView? = view.findViewById(R.id.messageSenderTV)
        val messageContentTV: TextView = view.findViewById(R.id.messageContentTV)
        val messageDateTV: TextView = view.findViewById(R.id.messageDateTV)

        lateinit var message: Message
    }

    class MessagesComparator : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.messageId == oldItem.messageId
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.content == newItem.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.MessageViewHolder {
        return if (viewType == MSG_TYPE_LEFT) {
            val entryView = LayoutInflater.from(parent.context).inflate(R.layout.message_left_entry, parent, false)
            MessageViewHolder(entryView)
        } else {
            val entryView = LayoutInflater.from(parent.context).inflate(R.layout.message_right_entry, parent, false)
            MessageViewHolder(entryView)
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message: Message = getItem(position)
        holder.message = message
        val account = accounts.find { it.accountId == message.senderId }

        if (account != null)
            holder.messageSenderTV?.text = "${account.firstName} ${account.lastName}"
        holder.messageContentTV.text = message.content

        val calNow = Calendar.getInstance()
        calNow.time = Date()
        val calMessage = Calendar.getInstance()
        calMessage.time = message.sendDate

        val sameDay = calNow.get(Calendar.DAY_OF_YEAR) == calMessage.get(Calendar.DAY_OF_YEAR) && calNow.get(
            Calendar.YEAR) == calMessage.get(Calendar.YEAR);
        if (sameDay) {
            holder.messageDateTV.text = time.format(message.sendDate)
        } else {
            holder.messageDateTV.text = date.format(message.sendDate)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == UserHelper.accountId) 1 else 0
    }
}