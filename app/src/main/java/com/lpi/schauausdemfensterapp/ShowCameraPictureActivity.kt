package com.lpi.schauausdemfensterapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Toast
import com.log4k.*
import com.log4k.android.AndroidAppender
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ShowCameraPictureActivity : AppCompatActivity () {

  private val SELECT_IMAGE_INTENT_CODE: Int = 900
  private val PERMISSION_REQUEST_CODE: Int = 901

  private var currentPhotoUri: Uri? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_show_camera_picture)

    Log4k.add(Level.Debug, ".*", AndroidAppender())

    // Ask for permissions that are not yet granted
    val notGrantedPermissions = arrayOf(Manifest.permission.CAMERA,
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.READ_EXTERNAL_STORAGE)
      .filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }.toTypedArray()

    if (notGrantedPermissions.isNotEmpty()) {
      requestPermissions(notGrantedPermissions, PERMISSION_REQUEST_CODE)
    } else {
      // take picture
      dispatchTakePictureIntent()
    }
  }


  /**
   * Take picture with camera
   */
  private fun dispatchTakePictureIntent() {
    try {
      // return takePictureAndSaveIt()
      return takePictureOrChooseFromGalleryAndSaveIt()
    } catch (ex: Exception) {
      e("Error while dispatching the Take Picture Intent", ex)
    }
  }

  /**
   * Take picture with camera
   */
  private fun takePictureOrChooseFromGalleryAndSaveIt() {
    d("Choosing or Taking Picture...")

    // File where the image goes to
    val photoFile: File? = try {
      createTempFile()
    } catch (ex: IOException) {
      // Error occurred while creating the File
      e("Error occurred while creating the File", ex)
      null
    }
    // Continue only if the File was successfully created
    photoFile?.also { file ->
      val photoURI: Uri = FileProvider.getUriForFile(
        this,
        "com.example.android.fileprovider",
        file
      )
      currentPhotoUri = photoURI
      val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
      galleryIntent.type = "image/*"
      galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
      val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
      cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

      val chooser = Intent.createChooser(galleryIntent, "Choose an Image")
      chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
      chooser.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
      startActivityForResult(chooser, SELECT_IMAGE_INTENT_CODE)
    }
  }


  /**
   * Handle Image capture Result
   */
  private fun handleImageCapturedResult(requestCode: Int, resultCode: Int, data: Intent?) {
    try {
      if (resultCode == AppCompatActivity.RESULT_OK) {
        if (data == null || data.data == null) {
          // Happens when photo was taken by camera
          insertIntoImageView(currentPhotoUri!!)
        } else {
          // when was chosen from gallery
          insertIntoImageView(data.data as Uri)
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
        val doAnalyze = preferences.getBoolean("pref_analyze_image", false)
        i("Analyse Preference is set to $doAnalyze")

      } else {
        showToast(R.string.error_handle_image_result)
        e("Failed to handle the image result: $resultCode")
      }
    } catch (ex: Exception) {
      e("Error while handling the captured Image", ex)
    }
  }

  private fun insertIntoImageView(uri: Uri) {
    d("Start inserting uri ($uri) into Image View")
    val imageView = this.findViewById(R.id.image_view) as ImageView

    imageView.setImageURI(uri)
    d("Done inserting uri Photo ($uri) into Image View")
  }

  /**
   * Handle activity results
   */
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      SELECT_IMAGE_INTENT_CODE -> handleImageCapturedResult(requestCode, resultCode, data)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  /**
   * Handle result of the permission requests
   */
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    when (requestCode) {
      PERMISSION_REQUEST_CODE -> {
        showDebugToast("Permissions requested: $permissions with answer: $grantResults")
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
          dispatchTakePictureIntent()
        } else {
          showToast(R.string.error_cant_continue_no_permissions)
          // go to main screen
          startActivity(Intent(this, MainScreenActivity::class.java))
        }
      }
      else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  private fun showToast(id: Int) {
    val s = getString(id)
    Toast.makeText(this, s, Toast.LENGTH_LONG).show()
  }

  /**
   * Only shows the Toast in BuildConfig.DEBUG <br>
   * Always logs to DEBUG
   */
  private fun showDebugToast(string: String) {
    d("Debug Toast: $string")
    if (BuildConfig.DEBUG) {
      Toast.makeText(this, string, Toast.LENGTH_LONG).show()
    }
  }

  @Throws(IOException::class)
  private fun createTempFile(): File {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
      "Image_${timeStamp}_", /* prefix */
      null, /* suffix */
      storageDir /* directory */
    )
  }
}
