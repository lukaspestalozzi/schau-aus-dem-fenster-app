package com.lpi.schauausdemfensterapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.log4k.Level
import com.log4k.Log4k
import com.log4k.android.AndroidAppender
import com.log4k.e
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ShowCameraPictureActivity : Activity() {

  private val IMAGE_CAPTURE_REQUEST_CODE: Int = 900
  private val PERMISSION_REQUEST_CODE: Int = 901

  private var currentPhotoPath: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_show_camera_picture)

    // init logger
    if (BuildConfig.DEBUG) {
      Log4k.add(Level.Verbose, ".*", AndroidAppender())
    } else {
      Log4k.add(Level.Info, ".*", AndroidAppender())
    }

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
    return dispatchTakePictureIntent_saveFile()
  }

  /**
   * Take picture with camera
   */
  private fun dispatchTakePictureIntent_saveFile() {
    // check permission
    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
    } else {
      // if have permission, take the picture
      Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
        // Ensure that there's a camera activity to handle the intent
        takePictureIntent.resolveActivity(packageManager)?.also {
          // Create the File where the photo should go
          val photoFile: File? = try {
            createImageFile()
          } catch (ex: IOException) {
            // Error occurred while creating the File
            e("Error occurred while creating the File", ex)
            null
          }
          // Continue only if the File was successfully created
          photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
              this,
              "com.example.android.fileprovider",
              it
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST_CODE)
          }
        }
      }
    }
  }


  /**
   * Handle Image capture Result
   */
  private fun handleImageCapturedResult(requestCode: Int, resultCode: Int, data: Intent) {
    return handleImageCapturedResult_tempFile(requestCode, resultCode, data)
  }

  /**
   * Handle Image capture Result
   */
  private fun handleImageCapturedResult_tempFile(requestCode: Int, resultCode: Int, data: Intent) {
    if (resultCode == AppCompatActivity.RESULT_OK) {
      setPicture()
    } else {
      showToast("Failed to handle the image result: $resultCode")
    }
  }

  private fun setPicture() {
    val imageView = this.findViewById(R.id.image_view) as ImageView

    // Get the dimensions of the View
    val targetW: Int = imageView.width
    val targetH: Int = imageView.height

    val bmOptions = BitmapFactory.Options().apply {
      // Get the dimensions of the bitmap
      inJustDecodeBounds = true
      BitmapFactory.decodeFile(currentPhotoPath, this)
      val photoW: Int = outWidth
      val photoH: Int = outHeight

      // Decode the image file into a Bitmap sized to fill the View
      inJustDecodeBounds = false

      // Determine how much to scale down the image
      if (targetW > 0 && targetH > 0) {
        val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)
        // inSampleSize = scaleFactor
      }
    }
    BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
      imageView.setImageBitmap(rotateBitmap(currentPhotoPath!!, bitmap))
    }
  }

  private fun rotateBitmap(src: String, bitmap: Bitmap) : Bitmap {
    // rotate the bitmap
    val orientation =  ExifInterface(src).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
    val matrix = Matrix()
    when(orientation) {
      ExifInterface.ORIENTATION_NORMAL -> return bitmap
      ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
      ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
      ExifInterface.ORIENTATION_FLIP_VERTICAL -> {matrix.setRotate(180f); matrix.postScale(-1f, 1f)}
      ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.setRotate(90f); matrix.postScale(-1f, 1f)}
      ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
      ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.setRotate(-90f); matrix.postScale(-1f, 1f)}
      ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
    }

    try {
      val oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
      bitmap.recycle()
      return oriented
    } catch (e: OutOfMemoryError) {
      Log.e("rotateBitmap: ", "OutOfMemoryError while rotating the bitmap")
      return bitmap
    }
  }

  /**
   * Handle activity results
   */
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    when (requestCode) {
      IMAGE_CAPTURE_REQUEST_CODE -> handleImageCapturedResult(requestCode, resultCode, data)
    }
  }

  /**
   * Handle result of the permission requests
   */
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    when (requestCode) {
      PERMISSION_REQUEST_CODE -> {
        showToast("Permission requested with answer: $grantResults")
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
          dispatchTakePictureIntent()
        } else {
          showToast("Can't continue without those Permisisons")
          // go to main screen
          startActivity(Intent(this, MainScreenActivity::class.java))
        }
      }
    }
  }

  private fun showToast(string: String) {
    Toast.makeText(this, string, Toast.LENGTH_LONG).show()
  }

  @Throws(IOException::class)
  private fun createImageFile(): File {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
      "JPEG_${timeStamp}_", /* prefix */
      ".jpg", /* suffix */
      storageDir /* directory */
    ).apply {
      // Save a file: path for use with ACTION_VIEW intents
      currentPhotoPath = absolutePath
    }
  }
}
