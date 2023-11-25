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
import com.bayarsahintekin.permission.PreferencesHandler

import java.lang.ref.WeakReference

class RequestPermissionLauncher private constructor(private val lifecycleOwner: WeakReference<LifecycleOwner>) :
    DefaultLifecycleObserver, ActivityResultCallback<Map<String, Boolean>> {

    private lateinit var permissionCheck: ActivityResultLauncher<Array<String>>
    private var activity: Activity? = null
    private var denied: List<SisalPermission> = arrayListOf()
    private lateinit var preferences: SharedPreferences

    init {
        lifecycleOwner.get()?.lifecycle?.addObserver(this)
    }
    companion object {
        fun from(lifecycleOwner: LifecycleOwner) = RequestPermissionLauncher(WeakReference(lifecycleOwner))
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
        for (perm in permissions) if (ContextCompat.checkSelfPermission(
                activity!!,
                perm
            ) != PackageManager.PERMISSION_GRANTED
        ) return false
        return true
    }

    private fun shouldShowRequestPermissionRationale(vararg permissions: String): Boolean {
        for (perm in permissions)
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, perm)) return true
        return false
    }

    fun requestPermission() = permissionCheck.launch(denied.map { it.permission }.toTypedArray())

    fun launch(permissions: ArrayList<String>,
               onShowRational: () -> Unit,
               onPermanentlyDenied: () -> Unit,
               onResult: (permissions: ArrayList<SisalPermission>) -> Unit) {

        val result = arrayListOf<SisalPermission>()
        permissions.forEach {
            if (checkSelfPermission(it)) {
                result.add(SisalPermission(it, PermissionResult.GRANTED))
                preferences[it + activity!!::class.java.simpleName] = true
            }else if (shouldShowRequestPermissionRationale(it)) {
                preferences[it + activity!!::class.java.simpleName] = false
                result.add(SisalPermission(it, PermissionResult.DENIED))
            }
            else {
                if (preferences[it + activity!!::class.java.simpleName]){
                    result.add(SisalPermission(it, PermissionResult.PERMANENTLY_DENIED))
                }else
                    permissionCheck.launch( permissions.map { it }.toTypedArray())

            }
        }

        denied = result.filter { it.state == PermissionResult.DENIED }
        val permanentlyDenied = result.filter { it.state == PermissionResult.PERMANENTLY_DENIED }

        if (denied.isNotEmpty())
            onShowRational.invoke()
        if (permanentlyDenied.isNotEmpty())
            onPermanentlyDenied.invoke()
        onResult.invoke(result)
    }
}

enum class PermissionResult {
    GRANTED, DENIED, PERMANENTLY_DENIED
}

data class SisalPermission(var permission: String, var state: PermissionResult)
