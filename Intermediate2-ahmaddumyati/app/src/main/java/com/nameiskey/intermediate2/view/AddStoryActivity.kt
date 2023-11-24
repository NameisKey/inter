package com.nameiskey.intermediate2.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.nameiskey.intermediate2.R
import com.nameiskey.intermediate2.databinding.ActivityAddStoryBinding
import com.nameiskey.intermediate2.util.HttpResponseCode
import com.nameiskey.intermediate2.util.Preferences
import com.nameiskey.intermediate2.util.createTempImgFile
import com.nameiskey.intermediate2.util.rotateFile
import com.nameiskey.intermediate2.util.uriToFile
import com.nameiskey.intermediate2.viewmodel.AddStoryViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding

    private lateinit var addStoryViewModel: AddStoryViewModel

    private lateinit var mUserPreference: Preferences

    private var getFile: File? = null

    private lateinit var currentPhotoPath: String

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.text_add_story)

        addStoryViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        )[AddStoryViewModel::class.java]

        addStoryViewModel.story.observe(this) {
            successAddStory()
        }

        addStoryViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        addStoryViewModel.responseType.observe(this) { response ->
            if (response != HttpResponseCode.SUCCESS) newStoryFailed(response)
        }

        addStoryViewModel.file.observe(this) { file ->
            getFile = file
            Glide.with(binding.ivDetailPhoto).load(BitmapFactory.decodeFile(file.path))
                .into(binding.ivDetailPhoto)
        }

        addStoryViewModel.currentPhotoPath.observe(this) { photoPath ->
            currentPhotoPath = photoPath
        }

        addStoryViewModel.location.observe(this) { location ->
            currentLocation = location
        }

        mUserPreference = Preferences(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getMyLastLocation()

        binding.btnCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.resolveActivity(packageManager)
            createTempImgFile(application).also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this@AddStoryActivity,
                    PACKAGE_NAME,
                    it
                )
                addStoryViewModel.setPhotoPath(it.absolutePath)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                launcherIntentCamera.launch(intent)
            }
        }

        binding.btnGallery.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = GALLERY_INTENT_TYPE
            val chooser =
                Intent.createChooser(intent, resources.getString(R.string.text_choose_picture))
            launcherIntentGallery.launch(chooser)
        }

        binding.buttonAdd.setOnClickListener {
            uploadImage(binding.edAddDescription.text.toString(), currentLocation)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)

            myFile.let { file ->
                rotateFile(file)
                addStoryViewModel.setFile(file)
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val selectedImg = it.data?.data as Uri
            selectedImg.let { uri ->
                addStoryViewModel.setFile(uriToFile(uri, this@AddStoryActivity))
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                getMyLastLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                getMyLastLocation()
            }
            else -> {
                binding.switchLocation.isEnabled = false
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    addStoryViewModel.setLocation(location)
                } else {
                    binding.switchLocation.isEnabled = false
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)

        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > STREAM_LENGTH)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    private fun uploadImage(storyDescription: String, location: Location?) {
        if (getFile != null) {
            val file = reduceFileImage(getFile as File)

            val description =
                storyDescription.toRequestBody(UPLOAD_DESCRIPTION_REQBODY.toMediaType())
            val requestImageFile = file.asRequestBody(UPLOAD_IMAGE_REQBODY.toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                PHOTO,
                file.name,
                requestImageFile
            )
            val latitude = location?.latitude?.toString()
                ?.toRequestBody(UPLOAD_DESCRIPTION_REQBODY.toMediaType())
            val longitude = location?.longitude?.toString()
                ?.toRequestBody(UPLOAD_DESCRIPTION_REQBODY.toMediaType())

            val userToken = mUserPreference.getUser().token

            addStoryViewModel.setAuth(userToken, description, imageMultipart, latitude, longitude)
        } else {
            Toast.makeText(
                this@AddStoryActivity,
                resources.getString(R.string.error_insert_file),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.apply {
                edAddDescription.isEnabled = false
                btnGallery.isEnabled = false
                btnCamera.isEnabled = false
                buttonAdd.isEnabled = false
                progressBar.visibility = View.VISIBLE
            }

        } else {
            binding.apply {
                edAddDescription.isEnabled = true
                btnGallery.isEnabled = true
                btnCamera.isEnabled = true
                buttonAdd.isEnabled = true
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun newStoryFailed(statusCode: Int?) {
        val message = when (statusCode) {
            HttpResponseCode.FAILED -> resources.getString(R.string.error_upload_failed)
            else -> resources.getString(R.string.error_server)
        }
        Toast.makeText(this@AddStoryActivity, "$statusCode: $message", Toast.LENGTH_SHORT).show()
    }

    private fun successAddStory() {
        Toast.makeText(
            this@AddStoryActivity,
            resources.getString(R.string.success_add_story),
            Toast.LENGTH_SHORT
        ).show()

        val intentToMain = Intent()
        setResult(MainActivity.ADD_STORY_RESULT, intentToMain)
        finish()
    }

    companion object {
        private const val PACKAGE_NAME = "com.nameiskey.intermediate2"

        private const val PHOTO = "photo"

        private const val STREAM_LENGTH = 1000000

        private const val GALLERY_INTENT_TYPE = "image/*"

        private const val UPLOAD_DESCRIPTION_REQBODY = "text/plain"

        private const val UPLOAD_IMAGE_REQBODY = "image/jpeg"
    }
}