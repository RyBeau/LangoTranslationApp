package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import java.io.IOException

class ProcessingFragment : Fragment() {
    val recogniser = TextRecognition.getClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val mainActivity = activity as MainActivity
        mainActivity.setLocation(MainActivity.Location.PROCESSING)
        return inflater.inflate(R.layout.fragment_processing, container, false)
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