package pl.dowiez.dowiezplchat.fragments.conversations

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.VolleyError
import kotlinx.android.synthetic.main.conversation_fragment.*
import pl.dowiez.dowiezplchat.App
import pl.dowiez.dowiezplchat.MainActivity
import pl.dowiez.dowiezplchat.R
import pl.dowiez.dowiezplchat.data.ChatDatabase
import pl.dowiez.dowiezplchat.data.entities.Account
import pl.dowiez.dowiezplchat.data.entities.Conversation
import pl.dowiez.dowiezplchat.data.entities.ConversationAccountCrossRef
import pl.dowiez.dowiezplchat.databinding.ConversationFragmentBinding
import pl.dowiez.dowiezplchat.fragments.chat.ChatFragment
import pl.dowiez.dowiezplchat.helpers.api.ApiHelper
import pl.dowiez.dowiezplchat.helpers.api.IConversationDetailCallback
import pl.dowiez.dowiezplchat.helpers.api.IMyAccountCallback
import pl.dowiez.dowiezplchat.helpers.api.IMyConversationsCallback
import java.util.*
import kotlin.collections.ArrayList

class ConversationFragment : Fragment(), ConversationAdapter.IOnConversationClickListener {
    companion object {
        //        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            LoginFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
        fun newInstance() = ConversationFragment()
    }

    private lateinit var viewModel: ConversationViewModel
    private lateinit var viewModelFactory: ConversationViewModelFactory
    private lateinit var binding: ConversationFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.conversation_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.statusBarColor, typedValue, true)
        @ColorInt val color = typedValue.data
        requireActivity().window.statusBarColor = color
        binding = ConversationFragmentBinding.bind(view)

        viewModelFactory = ConversationViewModelFactory((requireActivity().application as App).conversationRepository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ConversationViewModel::class.java)

        ApiHelper.getMyAccountInfo(requireContext(), object : IMyAccountCallback {
            override fun onSuccess() {
                Log.i("ConversationFragment", "Got account info")
            }

            override fun onError(error: VolleyError) {
                Log.e("ConversationFragment", "Failed to get account info")
            }

        })

        ApiHelper.getMyConversations(requireContext(), object : IMyConversationsCallback {
            override fun onSuccess(conversations: ArrayList<Conversation>) {
                conversations.forEach {
                    ApiHelper.getConversationDetail(requireContext(), it.conversationId, object : IConversationDetailCallback {
                        override fun onSuccess(
                            conversationId: String,
                            name: String,
                            creationDate: Date,
                            category: Int,
                            lastMessage: String?,
                            lastMessageDate: Date?,
                            chatMembers: ArrayList<Account>
                        ) {
                            Log.i("ConversationFragment", conversationId)

                            ChatDatabase.instance!!.conversationDao().insert(
                                Conversation(
                                    conversationId,
                                    name,
                                    category,
                                    creationDate,
                                    when (lastMessage) {
                                        null -> "Jeszcze nic nie wysłano..."
                                        else -> lastMessage
                                    },
                                    when (lastMessageDate) {
                                        null -> creationDate
                                        else -> lastMessageDate
                                    }
                                )
                            )

                            chatMembers.forEach { account ->
                                Log.i("ConversationFragment", "Insert ${account.accountId}")
                                ChatDatabase.instance!!.accountDao().insert(account)

                                ChatDatabase.instance!!.conversationDao().insertCross(
                                    ConversationAccountCrossRef(
                                        conversationId,
                                        account.accountId
                                    )
                                )
                            }
                        }

                        override fun onError(error: VolleyError) {
                            Log.e("ConversationFragment", "Failed to get conversation's small detail")
                        }

                    })
                }
            }

            override fun onError(error: VolleyError) {
                Log.e("ConversationFragment", "Failed to get my conversations")
                Toast.makeText(requireActivity(), "Failed to connect to server", Toast.LENGTH_LONG).show()
            }
        })

        Log.i("ConversationFragment", "StartHubConnection")
        MainActivity.getService()?.startHubConnection()

        ConversationAdapter.context = requireContext()
        val adapter = ConversationAdapter(this)
        conversationList.adapter = adapter
        conversationList.layoutManager = LinearLayoutManager(requireContext())

        viewModel.allConversations.observe(viewLifecycleOwner) {
            it.let { adapter.submitList(it) }
        }
    }

    override fun onItemClick(conversation: Conversation) {
        Log.i("ConversationFragment", conversation.conversationId)
        (requireActivity() as MainActivity).loadFragment(ChatFragment.newInstance(conversation.conversationId))
    }
}
