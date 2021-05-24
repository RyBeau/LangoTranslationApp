package nz.ac.uclive.oam23.tbc

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule
import kotlin.jvm.Throws

class HomeFragment : Fragment() {

    private val viewModel: TranslationsViewModel by activityViewModels() {
        TranslationsViewModelFactory((activity?.application as TBCApplication).repository)
    }

    val PERMISSIONS_REQUEST_CODE = 10
    val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE)

    //Request codes for individual requests.
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_FINE_LOCATION = 2
    val REQUEST_CAMERA_STORAGE_PERMISSIONS = 3
    lateinit var googleMapRef: GoogleMap

    lateinit var currentPhotoPath: String

    /**
     * Handles feedback to the user after permissions are accepted.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_FINE_LOCATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        googleMapRef.isMyLocationEnabled = true
                    } catch (e: SecurityException) {
                    }
                }
            }
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(requireActivity(), R.string.on_photo_success, Toast.LENGTH_LONG).show()
                    val bundle = bundleOf("photoPath" to currentPhotoPath)
                        view?.findNavController()?.navigate(R.id.action_navigation_home_to_processingFragment, bundle)
                }
            }
        }
    }

    /**
     * Callback function for permission request
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.sum()) {
                Toast.makeText(requireActivity(), getString(R.string.permissions_granted), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireActivity(), getString(R.string.permissions_denied), Toast.LENGTH_LONG).show()
            }
        }
        enableUserLocation()
    }

    /**
     * Enables the users location to be shown on the map.
     */
    fun enableUserLocation() {
        try {
            googleMapRef.isMyLocationEnabled = true
        } catch (e: SecurityException) {
        } catch (e: UninitializedPropertyAccessException) {
        }
    }

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
     * Requests to take a photo using intents.
     * If a photo is successfully taken, saves it into EXTERNAL storage.
     */
    private fun takeImage() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            context?.packageManager?.let {
                takePictureIntent.resolveActivity(it)?.also {
                    // Create the File where the photo should go
                    try {
                        val photoFile: File? = createImageFileName()
                        if (photoFile !== null) {
                            val photoURI: Uri? = requireActivity().let { context ->
                                FileProvider.getUriForFile(
                                        context,
                                        "nz.ac.uclive.oam23.tbc.android.fileprovider",
                                        photoFile
                                )
                            }
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        }
                    } catch (e: IOException) {
                        // Error occurred while creating the File
                        Toast.makeText(requireActivity(), getString(R.string.file_error), Toast.LENGTH_LONG).show()
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
        googleMapRef = googleMap

        viewModel.translationsList.observe(viewLifecycleOwner) { newTranslations ->
            for (translation in newTranslations) {
                val marker = LatLng(translation.location.longitude, translation.location.latitude)
                googleMap.addMarker(MarkerOptions().position(marker).title(translation.note))
                googleMap.setOnMarkerClickListener { marker ->
                    Toast.makeText(requireContext(), translation.note, Toast.LENGTH_LONG).show()
//                    viewModel.setSelectedIndex(position)
//                    Navigation.findNavController(requireView()).navigate(R.id.action_navigation_previous_to_navigation_viewTranslation)
                    true
                }

            }
        }

        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        //Set up users location.
        enableUserLocation()
    }


    /**
     * Requests for the users camera permissions.
     */
    fun requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CAMERA_STORAGE_PERMISSIONS)
    }

    /**
     * Creates the map, calls the OnMapReadyCallback() function.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val mainActivity = activity as MainActivity
        mainActivity.setLocation(MainActivity.Location.HOME)

        //Set up image capturing
        val cameraButton = view.findViewById<FloatingActionButton>(R.id.cameraButton)
        cameraButton.setOnClickListener {
            try {
                createImageFileName()
                takeImage()
            } catch (e: SecurityException) {
                requestCameraPermission()
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
    }
}