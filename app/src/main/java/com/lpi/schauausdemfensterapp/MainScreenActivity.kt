package com.lpi.schauausdemfensterapp

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast

class MainScreenActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main_screen)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.menu, menu)
    return true
  }

  /**
   * actions on click menu items
   */
  override fun onOptionsItemSelected(item: MenuItem) =
    when (item.itemId) {
      R.id.menu_action_settings -> {
        openSettings()
        true
      }
      else -> {
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        super.onOptionsItemSelected(item)
      }
  }


  /**
   * Open the settings Activity
   */
  fun openSettings() {
    // Create an Intent to start the settings
    val settingsIntent = Intent(this, SettingsActivity::class.java)

    // Start the new activity.
    startActivity(settingsIntent)
  }

  fun openCameraActivity(view: View) {
    // Create an Intent to start the settings
    val cameraIntent = Intent(this, ShowCameraPictureActivity::class.java)

    // Start the new activity.
    startActivity(cameraIntent)
  }

  fun displaySettings(view: View){
    val preferences = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)

    showToast("""pref_analyze_image: ${preferences.getBoolean("pref_analyze_image", false)}
      |notifications_new_message: ${preferences.getBoolean("notifications_new_message", false)}
      |notifications_new_message_ringtone: ${preferences.getString("notifications_new_message_ringtone", "<unset>")}
      |example_text: ${preferences.getString("example_text", "<unset>")}
      |example_list: ${preferences.getString("example_list", "<unset>")}
      |sync_frequency: ${preferences.getString("sync_frequency", "<unset>")}
    """.trimMargin())
  }

  private fun showToast(string: String) {
    Toast.makeText(this, string, Toast.LENGTH_LONG).show()
  }

}
