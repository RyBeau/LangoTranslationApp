package nz.ac.uclive.oam23.tbc

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.view.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class SaveEditTranslationFragment : Fragment() {

    enum class Mode {
        EDIT_MODE,
        NEW_MODE
    }

    private val viewModel: TranslationsViewModel by activityViewModels() {
        TranslationsViewModelFactory((activity?.application as TBCApplication).repository)
    }

    private lateinit var fragmentMode: Mode
    private lateinit var latLng: LatLng
    private var existingTranslation: Translation? = null



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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (fragmentMode == Mode.EDIT_MODE) {
            val note = requireView().findViewById<EditText>(R.id.noteEdit).text.toString()
            val location = requireView().findViewById<EditText>(R.id.locationEdit).text.toString()
            if (existingTranslation?.note != note){
                outState.putString("editedNote", note)
            }
            if (existingTranslation?.locationString != location){
                outState.putString("editedLocation", location)
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState?.containsKey("editedNote") == true){
            Log.d("Test", "This ran")
            requireView().findViewById<EditText>(R.id.noteEdit).setText(savedInstanceState.getString("editedNote"))
        }
        if (savedInstanceState?.containsKey("editedLocation") == true){
            requireView().findViewById<EditText>(R.id.locationEdit).setText(savedInstanceState.getString("editedLocation"))
        }
    }

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

        val key: Long
        if (requireArguments().getLong("translationKey") != (-1).toLong()) {
            key = requireArguments().getLong("translationKey")
            fragmentMode = Mode.EDIT_MODE
            viewModel.getTranslation(key).observe(viewLifecycleOwner, { dbTranslation ->
                existingTranslation = dbTranslation
                fillFromExisting()
            })
        } else if (requireArguments().getString("untranslatedText") != null &&
                requireArguments().getString("translatedText") != null) {
            fragmentMode = Mode.NEW_MODE
            requireArguments().getString("untranslatedText")?.let {
                requireArguments().getString("translatedText")?.let { it1 -> fillNew(it, it1) }
            }
        } else {
            errorToast()
            requireActivity().onBackPressed()
        }

        setButtonCallbacks(view)
//        toolbar?.setNavigationIcon(R.drawable.ic_launcher_foreground)
//        toolbar?.setNavigationOnClickListener (Navigation.createNavigateOnClickListener(R.id.action_saveEditTranslationFragment_to_homeFragment))
    }

    private fun setButtonCallbacks(view: View){
        view.findViewById<Button>(R.id.cancelEditTranslationButton).setOnClickListener {
            requireActivity().onBackPressed()
        }

        if (fragmentMode == Mode.NEW_MODE) {
            view.findViewById<Button>(R.id.saveEditTranslationButton).setOnClickListener {
                val originalText = view.findViewById<EditText>(R.id.originalTextEdit).text.toString()
                val translatedText = view.findViewById<TextView>(R.id.translatedText).text.toString()
                val locationString = view.findViewById<EditText>(R.id.locationEdit).text.toString()
                val note = view.findViewById<EditText>(R.id.noteEdit).text.toString()

                val translation = Translation(originalText, translatedText, LocalDate.now(), locationString, latLng, note)
                viewModel.addTranslation(translation)

                requireActivity().title = getString(R.string.title_save_translation)

            }
        } else {
            view.findViewById<Button>(R.id.saveEditTranslationButton).setOnClickListener {
                updateExistingTranslation(requireView())
                existingTranslation?.let { it1 -> viewModel.editTranslation(it1) }
                (requireActivity() as MainActivity).translationSaved()
            }
            requireActivity().title = getString(R.string.title_edit_translation)
        }
    }

    private fun updateExistingTranslation(view: View){
        val locationString = view.findViewById<EditText>(R.id.locationEdit).text.toString()
        val note = view.findViewById<EditText>(R.id.noteEdit).text.toString()
        existingTranslation?.locationString = locationString
        existingTranslation?.note = note
    }

    @SuppressLint("MissingPermission")
    private fun fillNew(originalTextString: String, translatedTextString: String){
        val originalText = requireView().findViewById<EditText>(R.id.originalTextEdit)
        val translatedText = requireView().findViewById<TextView>(R.id.translatedText)
        val location = requireView().findViewById<EditText>(R.id.locationEdit)
        val date = requireView().findViewById<TextView>(R.id.date)

        val locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationClient.lastLocation.addOnSuccessListener {
            latLng = LatLng(it.latitude, it.longitude)
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addressList = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            val locationString = "${addressList[0].getAddressLine(0)}, ${addressList[0].locality}, " +
                    "${addressList[0].postalCode}, ${addressList[0].postalCode}"
            location.setText(locationString)
        }
        date.text = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        originalText.setText(originalTextString)
        originalText.doAfterTextChanged {
            //TODO Retranslated and update
        }
        translatedText.text = translatedTextString

    }

    private fun errorToast(){
        Toast.makeText(
                requireActivity(),
                getString(R.string.error),
                Toast.LENGTH_LONG
        ).show()
    }

    private fun fillFromExisting() {
        val originalText = requireView().findViewById<EditText>(R.id.originalTextEdit)
        val translatedText = requireView().findViewById<TextView>(R.id.translatedText)
        val location = requireView().findViewById<EditText>(R.id.locationEdit)
        val date = requireView().findViewById<TextView>(R.id.date)
        val note = requireView().findViewById<EditText>(R.id.noteEdit)

        if (existingTranslation != null){
            originalText.setText(existingTranslation!!.originalText)
            translatedText.text = existingTranslation!!.translatedText
            if(location.text.isEmpty()) {
                location.setText(existingTranslation!!.locationString)
            }
            date.text = existingTranslation!!.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
            if(note.text.isEmpty()){
                note.setText(existingTranslation!!.note)
            }

            originalText.isEnabled = false
        } else {
            errorToast()
            requireActivity().onBackPressed()
        }
    }

}