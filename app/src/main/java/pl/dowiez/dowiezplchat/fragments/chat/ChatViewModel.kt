package pl.dowiez.dowiezplchat.fragments.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.dowiez.dowiezplchat.data.ChatRepository
import pl.dowiez.dowiezplchat.data.entities.ConversationWithAccounts
import pl.dowiez.dowiezplchat.data.entities.Message
import pl.dowiez.dowiezplchat.data.entities.MessageAndAccount

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    val conversationWithAccounts: LiveData<ConversationWithAccounts> = repository.getDetails().asLiveData()
    val allMessages: LiveData<List<Message>> = repository.getAll().asLiveData()

    fun insert(message: Message) = viewModelScope.launch {
        repository.insert(message)
    }
}