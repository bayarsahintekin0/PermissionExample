package com.bayarsahintekin.permission

import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bayarsahintekin.permission.PreferencesHandler.get
import com.bayarsahintekin.permission.PreferencesHandler.set

import java.lang.ref.WeakReference



class RequestPermissionLauncher private constructor(private val lifecycleOwner: WeakReference<LifecycleOwner>) :
    DefaultLifecycleObserver, ActivityResultCallback<Map<String, Boolean>> {

    private lateinit var permissionCheck: ActivityResultLauncher<Array<String>>
    private var activity: Activity? = null
    private var denied: List<PermissionData> = arrayListOf()
    private lateinit var preferences: SharedPreferences

    private var activityName:String = "Unknown Activity"

    init {
        lifecycleOwner.get()?.lifecycle?.addObserver(this)
        activity?.let {
            activityName =  it::class.java.simpleName
        }
    }

    companion object {
        fun from(lifecycleOwner: LifecycleOwner) =
            RequestPermissionLauncher(WeakReference(lifecycleOwner))
    }

    override fun onCreate(owner: LifecycleOwner) {
        permissionCheck = when (owner) {
            is AppCompatActivity -> {
                owner.registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                    this
                )
            }

            is ComponentActivity -> {
                owner.registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                    this
                )
            }

            else -> {
                (owner as Fragment).registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                    this
                )
            }
        }
        activity = when (lifecycleOwner.get()) {
            is Fragment -> {
                (lifecycleOwner.get() as? Fragment)?.context?.scanForActivity()
            }

            is ComponentActivity -> {
                lifecycleOwner.get() as ComponentActivity
            }

            else -> {
                lifecycleOwner.get() as? AppCompatActivity
            }
        }
        activity?.applicationContext?.let {
            preferences = PreferencesHandler.build(it)
        }
        super.onCreate(owner)
    }


    override fun onActivityResult(result: Map<String, Boolean>) {}

    private fun checkSelfPermission(vararg permissions: String): Boolean {
        for (perm in permissions) if (activity?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    perm
                )
            } != PackageManager.PERMISSION_GRANTED
        ) return false
        return true
    }

    private fun shouldShowRequestPermissionRationale(vararg permissions: String): Boolean {
        for (perm in permissions)
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, perm)) return true
        return false
    }

    fun requestPermission() = permissionCheck.launch(denied.map { it.permission }.toTypedArray())

    fun clearAllPermanentlyDeniedData(){
        preferences.edit().clear().apply()
    }

    fun launch(
        permissions: ArrayList<String>,
        onShowRational: (deniedPermissions: ArrayList<PermissionData>) -> Unit,
        onPermanentlyDenied: (permanentlyDeniedPermissions: ArrayList<PermissionData>) -> Unit,
        onResult: (permissions: ArrayList<PermissionData>) -> Unit
    ) {

        val result = arrayListOf<PermissionData>()
        permissions.forEach {
            if (checkSelfPermission(it)) {
                result.add(PermissionData(it, PermissionResult.GRANTED))
                preferences[it + activityName] = true
            } else if (shouldShowRequestPermissionRationale(it)) {
                preferences[it + activityName] = false
                result.add(PermissionData(it, PermissionResult.DENIED))
            } else {
                if (preferences[it + activityName]) {
                    result.add(PermissionData(it, PermissionResult.PERMANENTLY_DENIED))
                } else
                    permissionCheck.launch(permissions.map { it }.toTypedArray())

            }
        }

        // We kept denied permissions inside denied list to decide that;
        // will we show rationale or not
        denied = result.filter { it.state == PermissionResult.DENIED }
        val permanentlyDenied = result.filter { it.state == PermissionResult.PERMANENTLY_DENIED }

        // Here we check denied list is empty or not. If it's empty there is no need to show rationale
        // else we inform user via callback
        if (denied.isNotEmpty())
            onShowRational.invoke(denied as ArrayList<PermissionData>)
        //Here we check permanentlyDenied list is empty or not.
        // If it's empty there is no need to show permanently denied dialog
        // else we inform user via callback to show dialog
        if (permanentlyDenied.isNotEmpty())
            onPermanentlyDenied.invoke(permanentlyDenied as ArrayList<PermissionData>)
        // In every step we inform user permission states in that time.
        onResult.invoke(result)
    }
}

enum class PermissionResult {
    GRANTED, DENIED, PERMANENTLY_DENIED
}

data class PermissionData(var permission: String, var state: PermissionResult)
