package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels


/**
 * Fragment for viewing saved translations
 */
class ViewTranslationFragment : Fragment() {

    private lateinit var translation: Translation;

    private val viewModel: TranslationsViewModel by activityViewModels() {
        TranslationsViewModelFactory((activity?.application as TBCApplication).repository)
    }

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
        val mainActivity = activity as MainActivity
        mainActivity.setLocation(MainActivity.Location.VIEW_TRANSLATION)
        var key: Long = 0
        if (requireArguments().getString("translationKey") != null){
            key = requireArguments().getLong("translationKey")
        } else {
            Toast.makeText(
                    requireActivity(),
                    getString(R.string.translation_not_found),
                    Toast.LENGTH_LONG
            ).show()
            requireActivity().onBackPressed()
        }
        viewModel.getTranslation(key).observe(viewLifecycleOwner, { dbTranslation ->
            translation = dbTranslation
            fillTextViews()
        })
        return inflater.inflate(R.layout.fragment_view_translation, container, false)
    }

    private fun fillTextViews() {
        val originalText = view?.findViewById<TextView>(R.id.originalText)
        val translatedText = view?.findViewById<TextView>(R.id.translatedText)
        val location = view?.findViewById<TextView>(R.id.location)
        val note = view?.findViewById<TextView>(R.id.note)
        val date = view?.findViewById<TextView>(R.id.date)

        originalText?.text = translation.originalText
        translatedText?.text = translation.translatedText
        location?.text = translation.location.toString()
        note?.text = translation.note
        date?.text = translation.date.toString()
    }
}