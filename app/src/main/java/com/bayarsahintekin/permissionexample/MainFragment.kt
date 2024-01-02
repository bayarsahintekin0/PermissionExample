package com.bayarsahintekin.permissionexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bayarsahintekin.permission.RequestPermissionLauncher

/**
 * Created by sahintekin on 24.11.2023.
 */
class MainFragment : Fragment() {
    private val requestPermissionLauncher = RequestPermissionLauncher.from(this)

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)

        root.findViewById<Button>(R.id.btnFragmentPermission).setOnClickListener {
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
                        description = "You need to open setttings and give permission to go on"
                    )
                },
                onResult = { permissionsCurrentState ->
                    // We can see permissions list states on each state.
                }
            )
        }

        return root
    }

    private fun showPermissionRationaleDialog(title: String, description: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
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
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
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
        val packageName = activity?.packageName // or replace with your target package name

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}