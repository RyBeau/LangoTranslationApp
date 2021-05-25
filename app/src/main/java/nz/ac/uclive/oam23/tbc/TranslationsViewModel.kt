package nz.ac.uclive.oam23.tbc

import android.widget.Toast
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import android.util.Log.d
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.RequestQueue

class TranslationsViewModel(private val translationRepository: TranslationRepository): ViewModel() {

    val translationsList: LiveData<List<Translation>> = translationRepository.translations.asLiveData()

    // TODO: Once using navigation, switch to passing translation as a parameter on navigation to other fragments
    private var _selectedIndex = MutableLiveData(-1)
    val selectedIndex: LiveData<Int>
        get() = _selectedIndex

    fun getTranslation(key: Long): LiveData<Translation>{
        return translationRepository.getTranslation(key).asLiveData()
    }

    fun setSelectedIndex(position: Int) {
        _selectedIndex.value = position
    }

    fun addTranslation(translation: Translation) = viewModelScope.launch {
        translationRepository.insert(translation)
    }

    // TODO: ??? Change delete and edit functions to take in Translation parameters rather than indices
    fun deleteTranslation(index: Int) = viewModelScope.launch {
        // delete that translation. if it's -1 tho or > len then that's an issue
        if (index < 0 || translationsList.value == null || index > translationsList.value!!.size) {
            // we've got a problem...
        } else {
            translationRepository.delete(translationsList.value!![index])
        }
    }

    fun editTranslation(updatedTranslation: Translation) = viewModelScope.launch {
        translationRepository.update(updatedTranslation)
    }

}

class TranslationsViewModelFactory(private val repository: TranslationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TranslationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TranslationsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
