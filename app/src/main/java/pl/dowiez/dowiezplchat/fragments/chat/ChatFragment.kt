package pl.dowiez.dowiezplchat.fragments.chat

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import kotlinx.android.synthetic.main.fragment_chat.*
import pl.dowiez.dowiezplchat.App
import pl.dowiez.dowiezplchat.MainActivity
import pl.dowiez.dowiezplchat.R
import pl.dowiez.dowiezplchat.data.ChatDatabase
import pl.dowiez.dowiezplchat.data.ChatRepository
import pl.dowiez.dowiezplchat.data.entities.Message
import pl.dowiez.dowiezplchat.databinding.FragmentChatBinding
import pl.dowiez.dowiezplchat.helpers.api.ApiHelper
import pl.dowiez.dowiezplchat.helpers.api.IMessagesCallback
import pl.dowiez.dowiezplchat.service.ChatService
import pl.dowiez.dowiezplchat.service.IChatServiceListener

class ChatFragment : Fragment(), IChatServiceListener {
    companion object {
        const val ARG_CONVERSATION_ID = "conversationId"

        @JvmStatic
        fun newInstance(conversationId: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONVERSATION_ID, conversationId)
                }
            }
    }

    private var conversationId: String = "00000000-0000-0000-0000-000000000000"
    private lateinit var viewModel: ChatViewModel
    private lateinit var viewModelFactory: ChatViewModelFactory
    private lateinit var binding : FragmentChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            conversationId = it.getString(ARG_CONVERSATION_ID).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        @ColorInt val color = typedValue.data
        requireActivity().window.statusBarColor = color
        binding = FragmentChatBinding.bind(view)

        viewModelFactory = ChatViewModelFactory(ChatRepository(
            conversationId,
            (requireActivity().application as App).database.conversationDao(),
            (requireActivity().application as App).database.messageDao()
        ))
        viewModel = ViewModelProvider(this, viewModelFactory).get(ChatViewModel::class.java)

        chatTB.inflateMenu(R.menu.chat_menu)

        ChatAdapter.context = requireContext()
        messagesList.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }

        val adapter = ChatAdapter().apply {
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    messagesList.scrollToPosition(positionStart)
                }
            })
        }
        messagesList.adapter = adapter

        messagesList.apply {
            setHasFixedSize(true)
        }

        viewModel.conversationWithAccounts.observe(viewLifecycleOwner) {
            it.let {
                adapter.accounts = it.accounts
                chatTB.title = it.conversation.name
                chatTB.setTitleTextColor(requireContext().getColor(R.color.white))
                if (it.conversation.type != 0) {
                    chatTB.subtitle = "Liczba uczestników: ${it.accounts.size}"
                    chatTB.setSubtitleTextColor(requireContext().getColor(R.color.white))
                }
            }
        }
        
        if (viewModel.allMessages.value != null) {
            val lastId = viewModel.allMessages.value!!.last().messageId
            Log.i("ChatFragment", "Getting messages after $lastId")
            ApiHelper.getAfterMessages(requireContext(), conversationId, lastId, object : IMessagesCallback {
                override fun onSuccess(messages: ArrayList<Message>) {
                    messages.forEach {
                        ChatDatabase.instance!!.messageDao().insert(it)
                    }

                    ChatService.setChatServiceListener(this@ChatFragment)
                }

                override fun onError(error: VolleyError) {
                    Log.e("ChatFragment", "Failed to get messages after $lastId")
                }
            })
        } else {
            Log.i("ChatFragment", "Getting all messages")
            ApiHelper.getAllMessages(requireContext(), conversationId, object : IMessagesCallback {
                override fun onSuccess(messages: ArrayList<Message>) {
                    messages.forEach {
                        ChatDatabase.instance!!.messageDao().insert(it)
                    }
                }

                override fun onError(error: VolleyError) {
                    Log.e("ChatFragment", "Failed to get messages")
                }
            })
        }

        viewModel.allMessages.observe(viewLifecycleOwner) {
            it.let { adapter.submitList(it) }
        }

        sendIV.setOnClickListener { this@ChatFragment.onSendPressed() }
    }

    override fun onDestroyView() {
        ChatService.setChatServiceListener(null)
        super.onDestroyView()
    }

    private fun onSendPressed() {
        val message = newMessageTV.text.toString()
        newMessageTV.setText("")
        if (message.isNotEmpty())
            MainActivity.getService()?.sendMessage(conversationId, message)
    }

    override fun onMessageReceived(message: Message) {
        viewModel.insert(message)
    }
}