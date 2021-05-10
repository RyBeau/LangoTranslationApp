package nz.ac.uclive.oam23.tbc

import android.util.Log.d
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TranslationsViewModel: ViewModel() {

    private var _selectedIndex = MutableLiveData(1)
    val selectedIndex: LiveData<Int>
        get() = _selectedIndex

    fun tempStartingValue() {
        val translation = PreviousTranslation("1/2/2000", "OG Text", "Translated text.", "This is a very happy note! :D")
        _tempTranslationsList.value?.add(1, translation)
    }

    private var _tempTranslationsList = MutableLiveData<MutableList<PreviousTranslation>>(arrayListOf(
        PreviousTranslation("1/1/2021", "Test Text"),
        PreviousTranslation("2/1/2021", "Test Text"),
        PreviousTranslation("3/1/2021", "Test Text Longer"),
        PreviousTranslation("4/1/2021", "Test Text"),
        PreviousTranslation("5/1/2021", "Test Text"),
        PreviousTranslation("6/1/2021", "Test Text Longer"),
        PreviousTranslation("7/1/2021", "Test Text"),
        PreviousTranslation("8/1/2021", "Test Text"),
        PreviousTranslation("9/1/2021", "Test Text Longest Text of all"),
        PreviousTranslation("1/1/2021", "Test Text"),
        PreviousTranslation("2/1/2021", "Test Text"),
        PreviousTranslation("3/1/2021", "Test Text Longer"),
        PreviousTranslation("4/1/2021", "Test Text"),
        PreviousTranslation("5/1/2021", "Test Text"),
        PreviousTranslation("6/1/2021", "Test Text Longer"),
        PreviousTranslation("7/1/2021", "Test Text"),
        PreviousTranslation("8/1/2021", "Test Text"),
        PreviousTranslation("9/1/2021", "Test Text Longest Text of all"),
        PreviousTranslation("10/1/2021", "Test Text"))
    )
    val tempTranslationsList: LiveData<MutableList<PreviousTranslation>>
        get() = _tempTranslationsList

    fun setSelectedIndex(position: Int) {
        _selectedIndex.value = position
    }

    fun deleteTranslation(index: Int) {
        // delete that translation. if it's -1 tho or > len then that's an issue
    }

    fun editTranslation(translation: PreviousTranslation, index: Int = selectedIndex.value ?: -1) {
        // edit that translation. if it's -1 tho or > len then that's an issue
        d("Test", "Edited it!")
        val updatingTrans = _tempTranslationsList.value?.get(index)
        if (updatingTrans != null) {
            updatingTrans.originalText = translation.originalText
            updatingTrans.translatedText = translation.translatedText
            updatingTrans.note = translation.note
            _tempTranslationsList.value?.set(index, updatingTrans)
        }
        d("Test", updatingTrans.toString())
    }

}