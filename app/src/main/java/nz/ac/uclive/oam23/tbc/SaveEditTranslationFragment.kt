package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

private const val SAVE_MODE = true

class SaveEditTranslationFragment : Fragment() {

    private val viewModel: TranslationsViewModel by activityViewModels()

//    var toolbar: Toolbar? = null
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        toolbar = view?.findViewById<Toolbar>(R.id.toolbar)
//        toolbar?.inflateMenu(R.menu.edit_save_menu)
//
//        toolbar?.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.back_action -> {
//                    Toast.makeText(context, "Back", Toast.LENGTH_SHORT).show()
//                    true
//                }
//                R.id.delete_action -> {
//                    Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show()
//                    viewModel.deleteTranslation(viewModel.selectedIndex.value!!)
//                    true
//                }
//                else -> false
//            }
//        }
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.back_action -> {
                Toast.makeText(context, "Back", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.delete_action -> {
                Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }

        return super.onOptionsItemSelected(item)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainActivity = activity as MainActivity
        mainActivity.setLocation(MainActivity.Location.SAVE_EDIT_TRANSLATION)
        return inflater.inflate(R.layout.fragment_save_edit_translation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        tempFill()

        val original_text = view?.findViewById<EditText>(R.id.originalTextEdit).text.toString()
        val translated_text = view?.findViewById<TextView>(R.id.translatedText).text.toString()
        val location = view?.findViewById<EditText>(R.id.locationEdit).text.toString()
        val note = view?.findViewById<EditText>(R.id.noteEdit).text.toString()
        val date = view?.findViewById<TextView>(R.id.date).text.toString()

        view.findViewById<Button>(R.id.saveEditsButton).setOnClickListener {
            val tempTranslation = PreviousTranslation(date, original_text, translated_text, note)
            viewModel.editTranslation(tempTranslation)
        }



//        toolbar?.setNavigationIcon(R.drawable.ic_launcher_foreground)
//        toolbar?.setNavigationOnClickListener (Navigation.createNavigateOnClickListener(R.id.action_saveEditTranslationFragment_to_homeFragment))
    }

//    fun tempFill() {
//        val original_text = view?.findViewById<EditText>(R.id.originalTextEdit)
//        val translated_text = view?.findViewById<TextView>(R.id.translatedText)
//        val location = view?.findViewById<EditText>(R.id.locationEdit)
//        val note = view?.findViewById<EditText>(R.id.noteEdit)
//        val date = view?.findViewById<TextView>(R.id.date)
//
//
//        if (viewModel.selectedIndex.value != null && viewModel.selectedIndex.value != -1) {
//            val translation = viewModel.tempTranslationsList.value?.get(viewModel.selectedIndex.value!!)
//            if (translation != null) {
//                date?.text = translation.date
//                original_text?.setText(translation.originalText)
//                translated_text?.text = translation.translatedText
//                location?.setText("1 One Street, One Suburb, One City, 1111,  One Country")
//                note?.setText(translation.note)
//            } else {
//                // TODO: make an error message...
//                date?.text = "1/11/1111"
//                original_text?.setText("これをわざわざ翻訳しないでください")
//                translated_text?.text = "Do not bother translating this"
//                location?.setText("1 One Street, One Suburb, One City, 1111,  One Country")
//                note?.setText("This is a text note to test the note.")
//            }
//        } else {
//            // TODO: make an error message...
//            date?.text = "1/11/1111"
//            original_text?.setText("これをわざわざ翻訳しないでください")
//            translated_text?.text = "Do not bother translating this"
//            location?.setText("1 One Street, One Suburb, One City, 1111,  One Country")
//            note?.setText("This is a text note to test the note.")
//        }
//
//
//        if (!SAVE_MODE) {
//            original_text?.isEnabled = false
//        }
//    }

}