package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation


/**
 * Fragment for viewing saved translations
 */
class ViewTranslationFragment : Fragment() {

    private val viewModel: TranslationsViewModel by activityViewModels() {
        TranslationsViewModelFactory((activity?.application as TBCApplication).repository)
    }

    /**
     * Inflates the fragment layout
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainActivity = activity as MainActivity
        mainActivity.setLocation(MainActivity.Location.VIEW_TRANSLATION)

        val view = inflater.inflate(R.layout.fragment_view_translation, container, false)

        view.findViewById<Button>(R.id.editTranslationButton).setOnClickListener {
            Navigation.findNavController(view!!).navigate(R.id.action_navigation_viewTranslation_to_navigation_saveEdit)
        }

        return view
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
            val translation = viewModel.translationsList.value?.get(viewModel.selectedIndex.value!!)
            if (translation != null) {
                date?.text = translation.date.toString()
                original_text?.text = translation.originalText
                translated_text?.text = translation.translatedText
                location?.text = translation.location.toString()
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