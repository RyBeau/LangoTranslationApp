package nz.ac.uclive.oam23.tbc

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


class SaveEditTranslationFragment : NoNavFragment() {

    enum class Mode {
        EDIT_MODE,
        NEW_MODE
    }

    private val viewModel: TranslationsViewModel by activityViewModels {
        TranslationsViewModelFactory((activity?.application as LangoApplication).repository)
    }

    private lateinit var fragmentMode: Mode
    private lateinit var latLng: LatLng
    private var existingTranslation: Translation? = null
    private var currentSavedInstanceState: Bundle? = null
    private var usingCurrentLocation: Boolean = true

    private var originalText: String? = null
    private var translatedText: String? = null
    private var requestQueue: RequestQueue? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val note = requireView().findViewById<EditText>(R.id.noteEdit).text.toString()
        val location = requireView().findViewById<EditText>(R.id.locationEdit).text.toString()
        if (existingTranslation?.note != note){
            outState.putString("editedNote", note)
        }
        if (existingTranslation?.locationString != location){
            outState.putString("editedLocation", location)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState?.containsKey("editedNote") == true){
            requireView().findViewById<EditText>(R.id.noteEdit).setText(
                savedInstanceState.getString(
                    "editedNote"
                )
            )
        }
        if (savedInstanceState?.containsKey("editedLocation") == true){
            requireView().findViewById<EditText>(R.id.locationEdit).setText(
                savedInstanceState.getString(
                    "editedLocation"
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentSavedInstanceState = savedInstanceState
        val mainActivity = activity as MainActivity
        mainActivity.setLocation(MainActivity.Location.SAVE_EDIT_TRANSLATION)
        mainActivity.supportActionBar!!.show()
        return inflater.inflate(R.layout.fragment_save_edit_translation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val key: Long
        if (requireArguments().getLong("translationKey") != (-1).toLong()) {
            key = requireArguments().getLong("translationKey")
            fragmentMode = Mode.EDIT_MODE
            (requireActivity() as AppCompatActivity).supportActionBar!!.title = getString(R.string.title_edit_translation)
            viewModel.getTranslation(key).observe(viewLifecycleOwner, { dbTranslation ->
                existingTranslation = dbTranslation
                fillFromExisting()
            })
        } else if (requireArguments().getString("untranslatedText") != null &&
                requireArguments().getString("translatedText") != null) {
            fragmentMode = Mode.NEW_MODE
            requestQueue = Volley.newRequestQueue(context)
            requireArguments().getString("untranslatedText")?.let {
                requireArguments().getString("translatedText")?.let { it1 -> fillNew(it, it1) }
                (requireActivity() as AppCompatActivity).supportActionBar!!.title = getString(R.string.title_save_translation)
            }
        } else {
            createLongToast(getString(R.string.error))
            requireActivity().onBackPressed()
        }
        setFocusChangeCallbacks(view)
        setButtonCallbacks(view)
    }

    override fun onStop() {
        super.onStop()
        if (requestQueue != null) {
            requestQueue?.cancelAll(getString(R.string.TRANSLATION_API_REQUEST_TAG))
        }
    }

    private fun setButtonCallbacks(view: View){
        view.findViewById<Button>(R.id.cancelEditTranslationButton).setOnClickListener {
            requireActivity().onBackPressed()
        }

        if (fragmentMode == Mode.NEW_MODE) {
            view.findViewById<Button>(R.id.deleteTranslationButton).isVisible = false
            view.findViewById<Button>(R.id.saveEditTranslationButton).setOnClickListener {
                val originalText = view.findViewById<EditText>(R.id.originalTextEdit).text.toString()
                val translatedText = view.findViewById<TextView>(R.id.translatedText).text.toString()
                val locationString = view.findViewById<EditText>(R.id.locationEdit).text.toString()
                val note = view.findViewById<EditText>(R.id.noteEdit).text.toString()

                val translation = Translation(
                    originalText,
                    translatedText,
                    LocalDate.now(),
                    locationString,
                    latLng,
                    note
                )
                if (validateTranslation(translation.originalText, translation.locationString, translation.locationLatLng)){
                    viewModel.addTranslation(translation)
                            findNavController().navigate(R.id.action_navigation_saveEdit_to_navigation_home)
                } else {
                    createLongToast(getString(R.string.invalid_entries))
                }
            }
        } else {
            view.findViewById<Button>(R.id.deleteTranslationButton).isVisible = true
            view.findViewById<Button>(R.id.deleteTranslationButton).setOnClickListener{
                confirmDelete()
            }
            view.findViewById<Button>(R.id.saveEditTranslationButton).setOnClickListener {
                updateExistingTranslation(requireView())
                findNavController().navigate(R.id.action_navigation_saveEdit_to_navigation_home)
            }
        }
    }

    private fun setFocusChangeCallbacks(view: View){
        val locationEdit =  view.findViewById<EditText>(R.id.locationEdit)
        locationEdit.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus){
                val newLatLng = convertLocationToLatLng(locationEdit.text.toString())
                usingCurrentLocation = false
                if (newLatLng != null) {
                    latLng = newLatLng
                }
                setFocusableButtons(false)
            } else {
                setFocusableButtons(true)
            }
        }
        val saveButton =  view.findViewById<Button>(R.id.saveEditTranslationButton)
        val deleteButton =  view.findViewById<Button>(R.id.deleteTranslationButton)
        val cancelButton =  view.findViewById<Button>(R.id.cancelEditTranslationButton)
        setButtonToCloseKeyboard(saveButton)
        setButtonToCloseKeyboard(deleteButton)
        setButtonToCloseKeyboard(cancelButton)
    }

    private fun setButtonToCloseKeyboard(button: Button){
        button.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) run {
                val inputManager = (requireActivity().getSystemService(android.app.Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                inputManager.hideSoftInputFromWindow(view?.windowToken, 0)
            }
        }
    }

    private fun setFocusableButtons(bool: Boolean){
        requireView().findViewById<Button>(R.id.saveEditTranslationButton).isFocusableInTouchMode = bool
        requireView().findViewById<Button>(R.id.deleteTranslationButton).isFocusableInTouchMode = bool
        requireView().findViewById<Button>(R.id.cancelEditTranslationButton).isFocusableInTouchMode = bool
    }

    private fun validateTranslation(originalTextString: String, locationString: String, translationLatLng: LatLng): Boolean {
        return originalTextString.isNotEmpty() && validateLocation(locationString, translationLatLng)
    }

    private fun validateLocation(locationString: String, translationLatLng: LatLng): Boolean{
        return convertLocationToLatLng(locationString) == translationLatLng || usingCurrentLocation
    }

    private fun convertLocationToLatLng(locationString: String): LatLng? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return if (locationString.isNotEmpty()){
            val addresses = geocoder.getFromLocationName(locationString, 1)
            if (addresses.isNotEmpty() && addresses[0] != null) {
                LatLng(addresses[0].latitude, addresses[0].longitude)
            } else {
                resetLocationAlert()
                null
            }
        } else {
            resetLocationAlert()
            null
        }
    }

    private fun resetLocationAlert(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getString(R.string.location_not_found))
                .setCancelable(false)
                .setPositiveButton(R.string.yes) { _, _ ->
                    resetLocation()
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.dismiss()
                }
        val alert = builder.create()
        alert.show()
    }

    private fun resetLocation(){
        if (fragmentMode == Mode.NEW_MODE){
            setCurrentLocation()
        } else {
            val location = requireView().findViewById<EditText>(R.id.locationEdit)
            latLng = existingTranslation?.locationLatLng!!
            location.setText(existingTranslation?.locationString)
        }
    }

    private fun updateExistingTranslation(view: View) {
        val locationString = view.findViewById<EditText>(R.id.locationEdit).text.toString()
        val note = view.findViewById<EditText>(R.id.noteEdit).text.toString()
        var changeOccurred = false
        if(locationString != existingTranslation?.locationString){
            if (validateLocation(locationString, latLng)){
                existingTranslation?.locationLatLng = latLng
                existingTranslation?.locationString = locationString
                changeOccurred = true
            } else {
                return
            }
        }
        if (note != existingTranslation?.note){
            existingTranslation?.note = note
            changeOccurred = true
        }
        if (changeOccurred){
            existingTranslation?.let { it1 -> viewModel.editTranslation(it1) }
        }
        (requireActivity() as MainActivity).translationSaved()
    }

    @SuppressLint("MissingPermission")
    private fun setCurrentLocation(){
        val location = requireView().findViewById<EditText>(R.id.locationEdit)
        val locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        usingCurrentLocation = true
        locationClient.lastLocation.addOnSuccessListener {
            latLng = LatLng(it.latitude, it.longitude)
            val geocoder = Geocoder(context, Locale.getDefault())
            val addressList = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            val locationString = "${addressList[0].getAddressLine(0)}, ${addressList[0].locality}, " +
                    "${addressList[0].postalCode}, ${addressList[0].postalCode}"
            location.setText(locationString)
        }
    }

    private fun fillNew(originalTextString: String, translatedTextString: String){
        this.originalText = originalTextString
        this.translatedText = translatedTextString
        val originalText = requireView().findViewById<EditText>(R.id.originalTextEdit)
        val translatedText = requireView().findViewById<TextView>(R.id.translatedText)
        val location = requireView().findViewById<EditText>(R.id.locationEdit)
        val date = requireView().findViewById<TextView>(R.id.date)


        if (currentSavedInstanceState == null || currentSavedInstanceState?.containsKey("editedLocation") == false){
            setCurrentLocation()
        } else {
            location.setText(currentSavedInstanceState?.getString("editedLocation"))
            convertLocationToLatLng(location.text.toString())?.let { it -> latLng = it }
        }

        date.text = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        originalText.setText(originalTextString)
        translatedText.text = translatedTextString
        originalText.doAfterTextChanged {
            sendRequest(it.toString())
        }
    }

    private fun createLongToast(string: String){
        Toast.makeText(
            requireActivity(),
            string,
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
            latLng = existingTranslation?.locationLatLng!!
            originalText.setText(existingTranslation!!.originalText)
            translatedText.text = existingTranslation!!.translatedText
            if(location.text.isEmpty()) {
                location.setText(existingTranslation!!.locationString)
            } else {
                convertLocationToLatLng(location.text.toString())?.let { it -> latLng = it }
            }
            date.text = existingTranslation!!.date.format(
                DateTimeFormatter.ofLocalizedDate(
                    FormatStyle.SHORT
                )
            )
            if(note.text.isEmpty()){
                note.setText(existingTranslation!!.note)
            }

            originalText.isEnabled = false
        } else {
            createLongToast(getString(R.string.error))
            requireActivity().onBackPressed()
        }
    }

    /**
     * Creates confirmation dialog to confirm deletion.
     */
    private fun confirmDelete(){
        val builder = AlertDialog.Builder(context)
        builder.setMessage(getString(R.string.delete_confirmation))
                .setCancelable(false)
                .setPositiveButton(R.string.yes) { _, _ ->
                    existingTranslation?.let { viewModel.deleteTranslation(it) }
                    (requireActivity() as MainActivity).setLocation(MainActivity.Location.PREVIOUS_TRANSLATIONS)
                    findNavController().navigate(R.id.action_navigation_saveEdit_to_navigation_previous)
                }
                .setNegativeButton(R.string.no){ dialog, _ ->
                    dialog.dismiss()
                }
        val alert = builder.create()
        alert.show()
    }

    private fun sendRequest(text: String) {
        val url = getString(R.string.TRANSLATION_URL)
        val request: StringRequest =
                object : StringRequest(Method.POST, url, Response.Listener<String?> { response ->
                    var translationResponse: String
                    if (response != null) {
                        val parser = JsonParser()
                        val json = parser.parse(response).asJsonArray
                        try {
                            translationResponse =
                                json.get(0).asJsonObject.get("translations").asJsonArray.get(
                                    0
                                ).asJsonObject.get("text").asString
                        } catch (e: Exception) {
                            translationResponse = "No translation available"
                        }
                        view?.findViewById<TextView>(R.id.translatedText)?.text =
                            translationResponse
                    } else {
                        translationResponse = "No translation available"
                    }
                    view?.findViewById<TextView>(R.id.translatedText)?.setText(translationResponse)
                }, Response.ErrorListener {
                    createLongToast(getString(R.string.error))
                    findNavController().navigate(R.id.action_navigation_saveEdit_to_navigation_home)
                }) {

                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["Content-Type"] = "application/json; charset=UTF-8"
                        params["Ocp-Apim-Subscription-Key"] = getString(R.string.TRANSLATION_API_KEY)
                        return params
                    }

                    override fun getBody(): ByteArray {
                        val jsonObject = JSONObject()
                        jsonObject.put("Text", text)
                        val jsonArray = JSONArray()
                        jsonArray.put(jsonObject)
                        return jsonArray.toString().toByteArray()
                    }
                }
        requestQueue?.add(request)
    }
}
