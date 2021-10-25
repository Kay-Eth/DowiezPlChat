package pl.dowiez.dowiezplchat.fragments.conversations

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.dowiez.dowiezplchat.data.ConversationRepository
import pl.dowiez.dowiezplchat.data.entities.Conversation

class ConversationViewModel(private val repository: ConversationRepository) : ViewModel() {
    val allConversations: LiveData<List<Conversation>> = repository.getAll().asLiveData()

    fun insert(conversation: Conversation) = viewModelScope.launch {
        repository.insert(conversation)
    }
}