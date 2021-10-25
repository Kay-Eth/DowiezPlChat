package pl.dowiez.dowiezplchat.fragments.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.dowiez.dowiezplchat.data.ConversationRepository

class ConversationViewModelFactory(private val repository: ConversationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
            return ConversationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}