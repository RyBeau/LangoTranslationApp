package nz.ac.uclive.oam23.tbc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController


/**
 * Fragment for viewing saved translations
 */
class ViewTranslationFragment : NoNavFragment() {

    private lateinit var translation: Translation

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
        return inflater.inflate(R.layout.fragment_view_translation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val key = requireArguments().getLong("translationKey")
        if (key == (-1).toLong()){
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
        view.findViewById<Button>(R.id.editTranslationButton).setOnClickListener {
            val bundle = bundleOf("translationKey" to translation.id)
            view.findNavController().navigate(R.id.action_navigation_viewTranslation_to_navigation_saveEdit, bundle)
        }
        view.findViewById<Button>(R.id.shareTranslationButton).setOnClickListener {
            var message = "Wow! I just translated this image using Lango!\n"
            message += "Original Text:" + translation.originalText + "\n"
            message += "Translation: " + translation.translatedText + "\n"
            message += "Date: " + translation.date + "\n"
            composeMmsMessage(message)
         }
    }

    fun composeMmsMessage(message: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setData(Uri.parse("mms:"))
            putExtra("sms_body", message)
        }
        startActivity(intent)
    }

    private fun fillTextViews() {
        val originalText = view?.findViewById<TextView>(R.id.originalText)
        val translatedText = view?.findViewById<TextView>(R.id.translatedText)
        val location = view?.findViewById<TextView>(R.id.locationText)
        val note = view?.findViewById<TextView>(R.id.noteText)
        val date = view?.findViewById<TextView>(R.id.date)

        originalText?.text = translation.originalText
        translatedText?.text = translation.translatedText
        location?.text = translation.locationString
        note?.text = translation.note
        date?.text = translation.date.toString()
    }
}