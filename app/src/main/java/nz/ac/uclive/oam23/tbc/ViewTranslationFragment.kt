package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels


/**
 * Fragment for viewing saved translations
 */
class ViewTranslationFragment : Fragment() {

    private val viewModel: TranslationsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Inflates the fragment layout
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_translation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tempFill()
    }

    fun tempFill() {
        val original_text = view?.findViewById<TextView>(R.id.originalTextEdit)
        val translated_text = view?.findViewById<TextView>(R.id.translatedText)
        val location = view?.findViewById<TextView>(R.id.locationEdit)
        val note = view?.findViewById<TextView>(R.id.noteEdit)
        val date = view?.findViewById<TextView>(R.id.date)

        if (viewModel.selectedIndex.value != null && viewModel.selectedIndex.value != -1) {
            val translation = viewModel.tempTranslationsList.value?.get(viewModel.selectedIndex.value!!)
            if (translation != null) {
                date?.text = translation.date
                original_text?.text = translation.originalText
                translated_text?.text = translation.translatedText
                location?.text = "1 One Street, One Suburb, One City, 1111,  One Country"
                note?.text = translation.note
            } else {
                // TODO: make an error message...
                date?.text = "1/11/1111"
                original_text?.text = "これをわざわざ翻訳しないでください"
                translated_text?.text = "Do not bother translating this"
                location?.text = "1 One Street, One Suburb, One City, 1111,  One Country"
                note?.text = "This is a text note to test the note."
            }
        } else {
            // TODO: make an error message...
            date?.text = "1/11/1111"
            original_text?.text = "これをわざわざ翻訳しないでください"
            translated_text?.text = "Do not bother translating this"
            location?.text = "1 One Street, One Suburb, One City, 1111,  One Country"
            note?.text = "This is a text note to test the note."
        }
    }
}