package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import android.util.Log.d
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import java.io.IOException
import androidx.fragment.app.activityViewModels
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject


private const val SAVE_MODE = true

class SaveEditTranslationFragment : Fragment() {

    private val viewModel: TranslationsViewModel by activityViewModels() {
        TranslationsViewModelFactory((activity?.application as TBCApplication).repository)
    }

    val REQUEST_TAG = "translation"
    val API_KEY = "d4c58350bbc547b8a7d98270627274e5"
    var requestQueue: RequestQueue? = null


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

        autoFill()

        requestQueue = Volley.newRequestQueue(context)

        val jsonObject = JSONObject()
        jsonObject.put("Text", "Hello, what is your name?")
        val jsonArray = JSONArray()
        jsonArray.put(jsonObject)

        view.findViewById<Button>(R.id.saveEditTranslationButton).setOnClickListener {
            val original_text = view?.findViewById<EditText>(R.id.originalTextEdit).text.toString()
            val translated_text = view?.findViewById<TextView>(R.id.translatedText).text.toString()
            val location = view?.findViewById<EditText>(R.id.locationEdit).text.toString()
            val note = view?.findViewById<EditText>(R.id.noteEdit).text.toString()
            val date = view?.findViewById<TextView>(R.id.date).text.toString()

            val tempTranslation = PreviousTranslation(date, original_text, translated_text, note)
            viewModel.editTranslation(tempTranslation)
        }

        val translateButton = view.findViewById<Button>(R.id.tempTranslateButton)
        translateButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                sendRequest()
            }
        })




//        toolbar?.setNavigationIcon(R.drawable.ic_launcher_foreground)
//        toolbar?.setNavigationOnClickListener (Navigation.createNavigateOnClickListener(R.id.action_saveEditTranslationFragment_to_homeFragment))
    }

    override fun onStop() {
        super.onStop()
        if (requestQueue != null) {
            requestQueue?.cancelAll(REQUEST_TAG)
        }
    }

    fun sendRequest() {

        val translatedTextBox = view?.findViewById<TextView>(R.id.translatedText)
        val original_text = view?.findViewById<EditText>(R.id.originalTextEdit)?.text.toString()
        val url = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0&to=de"
        val request: StringRequest =
            object : StringRequest(Method.POST, url, object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    if (response != null) {
                        d("req", "Your Array Response " + response)

                        val parser = JsonParser()
                        val json = parser.parse(response).asJsonArray

                        translatedTextBox?.text = json.get(0).asJsonObject.get("translations").asJsonArray.get(0).asJsonObject.get("text").asString
                            .toString()
                    } else {
                        d("req", "Your Array Response "+ "Data Null")
                        translatedTextBox?.text = "No translation received"
                    }
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    d("req", "error is " + error)
                    translatedTextBox?.text = "error is " + error.message
                }
            }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/json; charset=UTF-8"
                    params["Ocp-Apim-Subscription-Key"] = API_KEY
                    return params
                }

                override fun getBody(): ByteArray {
                    val jsonObject = JSONObject()
                    jsonObject.put("Text", original_text)
                    val jsonArray = JSONArray()
                    jsonArray.put(jsonObject)
                    return jsonArray.toString().toByteArray()
                }
            }
        requestQueue?.add(request)
    }

    fun autoFill() {
        val original_text = view?.findViewById<EditText>(R.id.originalTextEdit)
        val translated_text = view?.findViewById<TextView>(R.id.translatedText)
        val location = view?.findViewById<EditText>(R.id.locationEdit)
        val note = view?.findViewById<EditText>(R.id.noteEdit)
        val date = view?.findViewById<TextView>(R.id.date)

        d("Test", "Auto-filling now")


        if (viewModel.selectedIndex.value != null && viewModel.selectedIndex.value != -1) {
            val translation = viewModel.translationsList.value?.get(viewModel.selectedIndex.value!!)
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
        } else {
            // TODO: make an error message...
            date?.text = "1/11/1111"
            original_text?.setText("これをわざわざ翻訳しないでください")
            translated_text?.text = "Do not bother translating this"
            location?.setText("1 One Street, One Suburb, One City, 1111,  One Country")
            note?.setText("This is a text note to test the note.")
        }


        if (!SAVE_MODE) {
            original_text?.isEnabled = false
        }
    }
}
