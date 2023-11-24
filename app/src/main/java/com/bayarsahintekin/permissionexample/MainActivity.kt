package com.bayarsahintekin.permissionexample

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bayarsahintekin.permission.RequestPermissionLauncher
import com.bayarsahintekin.permissionexample.R

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = RequestPermissionLauncher.from(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {

            requestPermissionLauncher.launch(
                arrayListOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                onShowRational = {
                    showPermissionRationaleDialog(
                        title = "Rationale",
                        description = "You need to give permission to go on"
                    )
                }, onPermanentlyDenied = {
                    showPermanentlyDeniedPermissionDialog(
                        title = "Permanently Denied",
                        description = "You need to open settings and give permission to go on"
                    )
                },
                onResult = {

                }
            )

        }
    }

    private fun showPermissionRationaleDialog(title: String, description: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setMessage(description)
            .setTitle(title)
            .setPositiveButton("Give permission"){ dialog, which ->
                requestPermissionLauncher.requestPermission()
            }
            .setNegativeButton("Cancel"){ dialog, which ->
                dialog.dismiss()
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showPermanentlyDeniedPermissionDialog(title: String, description: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setMessage(description)
            .setTitle(title)
            .setPositiveButton("Go to settings"){ dialog, which ->
                openSettings()
            }
            .setNegativeButton("Cancel"){ dialog, which ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun openSettings() {
        val packageName = packageName
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}