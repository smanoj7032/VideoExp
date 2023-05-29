package com.example.videoexp.utils

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object PermissionUtils {
    fun isPermissionGranted(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    fun requestPermissions(
        requestCode: Int,
        activity: AppCompatActivity,
        permissions: Array<String>
    ) {
        val permissionsToRequest = mutableListOf<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            showPermissionRationaleDialog(
                activity,
                permissionsToRequest.toTypedArray(),
                requestCode
            )
        }
    }

    private fun showPermissionRationaleDialog(
        activity: AppCompatActivity,
        permissions: Array<String>,
        requestCode: Int
    ) {
        // Show a dialog explaining why the permission is needed
        // You can customize this dialog to fit your needs

        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder.setTitle("Permission Required")
        dialogBuilder.setMessage("This app requires certain permissions to function properly.")
        dialogBuilder.setPositiveButton("OK") { _, _ ->
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                requestCode
            )
        }
        dialogBuilder.setCancelable(false) // Disable cancel button

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    fun checkPermission(context: Context): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result =
                ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE)
            val result1 =
                ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }

}


