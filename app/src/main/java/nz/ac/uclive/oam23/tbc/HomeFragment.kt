package nz.ac.uclive.oam23.tbc

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
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
import kotlin.jvm.Throws

class HomeFragment : Fragment() {

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
                }
            }
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
                        Toast.makeText(requireActivity(), "Error occurred while creating the file.", Toast.LENGTH_LONG).show()
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
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        //Set up users location.
        try {
            googleMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            getMapPermissions()
        }
    }

    /**
     * Requests for the users map permissions.
     */
    fun requestMapPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FINE_LOCATION)
    }

    /**
     * Creates an AlertDialog telling the user the benefits of enabling permissions.
     * On confirm, they will be requested to enable their map permissions.
     */
    fun buildMapAlert() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(R.string.map_permission_dialog)
                .setPositiveButton(R.string.dialog_confirm,
                        DialogInterface.OnClickListener { _, _ ->
                            requestMapPermission()
                        })
                .setNegativeButton(R.string.dialog_cancel,
                        DialogInterface.OnClickListener { _, _ ->
                        })
        // Create the AlertDialog object and return it
        builder.create()
        val dialog: AlertDialog? = builder.create()
        dialog?.show()
    }

    /**
     * Calls appropriate permission request dialogs.
     */
    fun getMapPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {

            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                buildMapAlert()
            }
            else -> {
                // You can directly ask for the permission.
                requestMapPermission()
            }

        }

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
        (activity as MainActivity).checkPermissions()
    }
}