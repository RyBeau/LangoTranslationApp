package nz.ac.uclive.oam23.tbc

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class TranslationsViewModel(private val translationRepository: TranslationRepository): ViewModel() {

    private var _selectedIndex = MutableLiveData(-1)
    val selectedIndex: LiveData<Int>
        get() = _selectedIndex

//    private var _tempTranslationsList = MutableLiveData<MutableList<Translation>>(arrayListOf(
//        Translation("1/1/2021", "Test Text"),
//        Translation("2/1/2021", "Test Text"),
//        Translation("3/1/2021", "Test Text Longer"),
//        Translation("4/1/2021", "Test Text"),
//        Translation("5/1/2021", "Test Text"),
//        Translation("6/1/2021", "Test Text Longer"),
//        Translation("7/1/2021", "Test Text"),
//        Translation("8/1/2021", "Test Text"),
//        Translation("9/1/2021", "Test Text Longest Text of all"),
//        Translation("10/1/2021", "Test Text"))
//    )
//    val tempTranslationsList: LiveData<MutableList<PreviousTranslation>>
//        get() = _tempTranslationsList
    val translationsList: LiveData<List<Translation>> = translationRepository.translations.asLiveData()

    fun setSelectedIndex(position: Int) {
        _selectedIndex.value = position
    }

    fun addTranslation(translation: Translation) = viewModelScope.launch {
        translationRepository.insert(translation)
    }

    // TODO: Change delete and edit functions to take in Translation parameters rather than indices
    fun deleteTranslation(index: Int) {
        // delete that translation. if it's -1 tho or > len then that's an issue
    }

    fun editTranslation(index: Int) {
        // edit that translation. if it's -1 tho or > len then that's an issue
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
