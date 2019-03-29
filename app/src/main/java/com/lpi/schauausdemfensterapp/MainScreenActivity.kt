package com.lpi.schauausdemfensterapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast

class MainScreenActivity : AppCompatActivity() {




  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main_screen)
  }

  fun toast(view: View) {
    // val myToast = Toast.makeText(this, message, duration);
    val myToast = Toast.makeText(this, "Hello Toast!", Toast.LENGTH_SHORT)
    myToast.show()
  }

  fun openSettings(view: View) {
    // Create an Intent to start the settings
    val settingsIntent = Intent(this, SettingsActivity::class.java)

    // Start the new activity.
    startActivity(settingsIntent)
  }

  fun openCameraActiity(view: View) {
    // Create an Intent to start the settings
    val cameraIntent = Intent(this, ShowCameraPictureActivity::class.java)

    // Start the new activity.
    startActivity(cameraIntent)
  }

}
