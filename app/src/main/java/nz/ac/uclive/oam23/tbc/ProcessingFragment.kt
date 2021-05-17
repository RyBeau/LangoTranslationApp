package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import java.io.File
import java.io.IOException


class ProcessingFragment : Fragment() {
    val recogniser = TextRecognition.getClient()
    lateinit var currentAction: TextView
    lateinit var path: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        savedInstanceState?.get("photoPath")?.let { Log.d("Test", it.toString()) }
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
        arguments?.getString("photoPath")?.let { detectText(it) } ?: run {
            Toast.makeText(
                requireActivity(),
                getString(R.string.no_image),
                Toast.LENGTH_LONG
            ).show()
            requireActivity().onBackPressed()
        }

    }

    override fun onStop() {
        Log.d("Text", path)
        super.onStop()
        val file = File(path)
        file.delete()
    }

    private fun detectText(imagePath: String) {
        path = imagePath
        val image: InputImage
        try {
            image = InputImage.fromFilePath(requireContext(),  ("file://$imagePath").toUri())
            val result = recogniser.process(image)
                .addOnSuccessListener { visionText -> run {
                    processVisionText(visionText)
                }
                }
                .addOnFailureListener { e ->
                    run {
                        Toast.makeText(
                            requireActivity(),
                            getString(R.string.text_recognition_fail),
                            Toast.LENGTH_LONG
                        ).show()
                        e.printStackTrace()
                        requireActivity().onBackPressed()
                    }
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun processVisionText(text: Text){
        val blocks = text.textBlocks
        Log.d("Text", blocks.toString())
        if(blocks.size < 1){
            Toast.makeText(requireActivity(), getString(R.string.no_text_found), Toast.LENGTH_LONG).show()
            requireActivity().onBackPressed()
        } else {
            var allText = ""
            for (block in blocks){
                Log.d("Text", block.text)
                allText = "${block.text} "
            }
            translateText(allText)
        }
    }

    private fun translateText (text: String){
        currentAction.text = getString(R.string.translating_text)
        Log.d("Text", text)
    }

}