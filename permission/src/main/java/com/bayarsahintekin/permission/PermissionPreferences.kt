package com.bayarsahintekin.permission

import android.content.Context
import android.content.SharedPreferences

object PermissionsPreferences {
    private var sharedPreferences: SharedPreferences? = null
    private const val PREF_NAME = "permissions_pref"

    fun initPermissionSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun updatePermissionStatus(key: String, value: Boolean) {
        val editor = sharedPreferences?.edit()
        editor?.putBoolean(key, value)
        editor?.apply()
    }

    fun getPermissionStatus(key: String): Boolean {
        return sharedPreferences?.getBoolean(key, false) ?: false
    }
}