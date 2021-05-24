package nz.ac.uclive.oam23.tbc

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.*


class SaveEditTranslationFragment : Fragment() {

    private val viewModel: TranslationsViewModel by activityViewModels() {
        TranslationsViewModelFactory((activity?.application as TBCApplication).repository)
    }

    private var saveMode = false
    private var translationId: Long? = null
    private var originalText: String? = null
    private var translatedText: String? = null
    private var requestQueue: RequestQueue? = null

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
        bundle: Bundle?
    ): View? {
        val mainActivity = activity as MainActivity
        mainActivity.setLocation(MainActivity.Location.SAVE_EDIT_TRANSLATION)

        originalText = arguments?.getString("original_text")
        translatedText = arguments?.getString("translated_text")

        if (originalText != null || translatedText != null) {
            saveMode = true
            requestQueue = Volley.newRequestQueue(context)
        }

        return inflater.inflate(R.layout.fragment_save_edit_translation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autoFill()

        val jsonObject = JSONObject()
        jsonObject.put("Text", "Hello, what is your name?")
        val jsonArray = JSONArray()
        jsonArray.put(jsonObject)

        if (saveMode) {
            view.findViewById<Button>(R.id.saveEditTranslationButton).setOnClickListener {
                val original_text =
                    view?.findViewById<EditText>(R.id.originalTextEdit).text.toString()
                val translated_text =
                    view?.findViewById<TextView>(R.id.translatedText).text.toString()
                val date = view?.findViewById<TextView>(R.id.date).text.toString()
//                val location = view?.findViewById<EditText>(R.id.locationEdit).text.toString()
                val note = view?.findViewById<EditText>(R.id.noteEdit).text.toString()

                val location = LatLng(0.0, 0.0)     // TODO implement actual location storing

                val tempTranslation = Translation(
                    original_text, translated_text, LocalDate.parse(
                        date
                    ), location, note
                )
                viewModel.addTranslation(tempTranslation)
                Toast.makeText(context, "Translation saved sucessfully", Toast.LENGTH_SHORT)
            }
        } else {
            view.findViewById<Button>(R.id.saveEditTranslationButton).setOnClickListener {
                val original_text =
                    view?.findViewById<EditText>(R.id.originalTextEdit).text.toString()
                val translated_text =
                    view?.findViewById<TextView>(R.id.translatedText).text.toString()
//                val location = view?.findViewById<EditText>(R.id.locationEdit).text.toString()
                val note = view?.findViewById<EditText>(R.id.noteEdit).text.toString()
                val date = view?.findViewById<TextView>(R.id.date).text.toString()

                val location = LatLng(0.0, 0.0)

                val tempTranslation = Translation(
                    original_text, translated_text, LocalDate.parse(
                        date
                    ), location, note
                )
                viewModel.editTranslation(tempTranslation)
            }
        }

        view?.findViewById<EditText>(R.id.originalTextEdit).addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable) {
                sendRequest(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

//        val originalTextEditText = view?.findViewById<EditText>(R.id.originalTextEdit)
//        originalTextEditText.setOnFocusChangeListener { view, hasFocus ->
//            if (!hasFocus) {
//                sendRequest(originalTextEditText.text.toString())
//            }
//        }
    }

    override fun onStop() {
        super.onStop()
        if (requestQueue != null) {
            requestQueue?.cancelAll(viewModel.REQUEST_TAG)
        }
    }

    fun autoFill() {
        val original_text = view?.findViewById<EditText>(R.id.originalTextEdit)
        val translated_text = view?.findViewById<TextView>(R.id.translatedText)
        val location = view?.findViewById<EditText>(R.id.locationEdit)
        val note = view?.findViewById<EditText>(R.id.noteEdit)
        val date = view?.findViewById<TextView>(R.id.date)

        if (saveMode) {
            date?.text = LocalDate.now().toString()
            original_text?.setText(originalText)
            translated_text?.text = translatedText
            location?.setText("temp location")
            note?.setText("")
        } else {
            original_text?.isEnabled = false
            if (translationId != null) {
                viewModel.viewModelScope.launch {
                    val translation = viewModel.getTranslation(translationId!!)
                    fillTranslation(translation)
                }
            } else {
                fillTranslation(null)
            }
        }
    }

    private fun fillTranslation(translation: Translation?) {
        val original_text = view?.findViewById<EditText>(R.id.originalTextEdit)
        val translated_text = view?.findViewById<TextView>(R.id.translatedText)
        val location = view?.findViewById<EditText>(R.id.locationEdit)
        val note = view?.findViewById<EditText>(R.id.noteEdit)
        val date = view?.findViewById<TextView>(R.id.date)

        if (translation != null) {
            date?.text = translation.date.toString()
            original_text?.setText(translation.originalText)
            translated_text?.text = translation.translatedText
            location?.setText(translation.location.toString())
            note?.setText(translation.note)
        } else {
            // TODO: make an error message...
            date?.text = "1/11/1111"
            original_text?.setText("これをわざわざ翻訳しないでください")
            translated_text?.text = "Do not bother translating this"
            location?.setText("1 One Street, One Suburb, One City, 1111,  One Country")
            note?.setText("This is a text note to test the note.")
        }
    }

    fun sendRequest(text: String) {
        // TODO: Update to work with the language the user selects :)
        val url = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0&to=de"
        val request: StringRequest =
            object : StringRequest(Method.POST, url, object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    var translationResponse = ""
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
                        view?.findViewById<TextView>(R.id.translatedText)?.setText(
                            translationResponse
                        )
                    } else {
                        translationResponse = "No translation available"
                    }
                    view?.findViewById<TextView>(R.id.translatedText)?.setText(translationResponse)
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    buildErrorAlert()
                }
            }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/json; charset=UTF-8"
                    params["Ocp-Apim-Subscription-Key"] = viewModel.API_KEY
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
