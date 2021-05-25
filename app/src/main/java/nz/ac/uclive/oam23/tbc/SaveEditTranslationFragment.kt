package nz.ac.uclive.oam23.tbc

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.internal.NavigationMenu
import com.google.android.material.navigation.NavigationView
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

    private val viewModel: TranslationsViewModel by activityViewModels() {
        TranslationsViewModelFactory((activity?.application as TBCApplication).repository)
    }

    private lateinit var fragmentMode: Mode
    private lateinit var latLng: LatLng
    private var existingTranslation: Translation? = null


    private var saveMode = false
    private var translationId: Long? = null
    private var originalText: String? = null
    private var translatedText: String? = null
    private var requestQueue: RequestQueue? = null

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
            errorToast()
            requireActivity().onBackPressed()
        }
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
                viewModel.addTranslation(translation)
            }
        } else {
            view.findViewById<Button>(R.id.saveEditTranslationButton).setOnClickListener {
                updateExistingTranslation(requireView())
                existingTranslation?.let { it1 -> viewModel.editTranslation(it1) }
                (requireActivity() as MainActivity).translationSaved()
            }
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
        this.originalText = originalTextString
        this.translatedText = translatedTextString
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
            sendRequest(it.toString())
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
            errorToast()
            requireActivity().onBackPressed()
        }
    }

    fun sendRequest(text: String) {
        val url = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0&to=en"
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
                }, Response.ErrorListener { buildErrorAlert() }) {

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

    private fun buildErrorAlert() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(getString(R.string.errorOccurred))
                .setCancelable(false)
                .setPositiveButton(R.string.returnWithoutSaving) { _, _ ->
                    activity?.onBackPressed()
                }
        val alert = builder.create()
        alert.show()
    }
}
