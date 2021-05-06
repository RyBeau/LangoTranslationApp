package nz.ac.uclive.oam23.tbc

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val REQUEST_IMAGE_CAPTURE = 1


    lateinit var currentPhotoPath: String

    /**
     * Creates a unique filename using the current date.
     * Currently saves files to EXTERNAL storage.
     */
    @Throws(IOException::class)
    private fun createImageFileName(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    /**
     * If a photo is successfully taken and saved, show a toast to the user.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // It came from our call
            if (resultCode == Activity.RESULT_OK) {
                // The result was successful
                Toast.makeText(activity?.applicationContext, "Photo successfully taken!", Toast.LENGTH_LONG).show()
            }
        }
    }


    /**
     * Requests to take a photo using intents.
     * If a photo is sucessfully taken, saves it into
     */
    private fun takeImage() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            context?.packageManager?.let {
                takePictureIntent.resolveActivity(it)?.also {
                    // Create the File where the photo should go
                    try {
                        val photoFile: File? = createImageFileName()
                        photoFile.also { file ->
                            if (file !== null) {
                                val photoURI: Uri? = activity?.applicationContext?.let { context ->
                                    FileProvider.getUriForFile(
                                            context,
                                            "nz.ac.uclive.oam23.tbc.android.fileprovider",
                                            file
                                    )
                                }
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                            }
                        }
                    } catch (e: IOException) {
                        // Error occurred while creating the File
                        Toast.makeText(activity?.applicationContext, "Error occurred while creating the file.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    /**
     * Function to change the map once it is created.
     * Add markers/move camera/set zoom here as required.
     */
    private val callback = OnMapReadyCallback { googleMap ->

        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    /**
     * Creates the map, calls the OnMapReadyCallback() function.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        // Inflate the layout for this fragment

        //Set up image capturing
        val cameraButton = view.findViewById<FloatingActionButton>(R.id.cameraButton)
        cameraButton.setOnClickListener {
            try {
                createImageFileName()
                takeImage()
            } catch (e: SecurityException) {
                Toast.makeText(context, "Error: Please ensure you have appropriate camera permissions in your phone settings", Toast.LENGTH_LONG).show()
            }
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                HomeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}