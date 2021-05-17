package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import java.io.IOException


class ProcessingFragment : Fragment() {
    val recogniser = TextRecognition.getClient()
    lateinit var currentAction: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val mainActivity = activity as MainActivity
        mainActivity.setLocation(MainActivity.Location.PROCESSING)
        return inflater.inflate(R.layout.fragment_processing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentAction = view.findViewById(R.id.currentAction)
        currentAction.text = getString(R.string.recognising_text)
    }

    private fun detectText(imagePath: String) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(requireContext(), imagePath.toUri())
            val result = recogniser.process(image)
                .addOnSuccessListener { visionText -> {
                    //TODO Set text box value
                }
                }
                .addOnFailureListener { e -> {
                    //TODO Handle failure
                    e.printStackTrace()
                }
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}