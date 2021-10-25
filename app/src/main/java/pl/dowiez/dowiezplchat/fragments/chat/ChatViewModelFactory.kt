package pl.dowiez.dowiezplchat.fragments.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.dowiez.dowiezplchat.data.ChatRepository

class ChatViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}