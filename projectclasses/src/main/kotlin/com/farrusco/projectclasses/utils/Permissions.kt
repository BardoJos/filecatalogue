package com.farrusco.projectclasses.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object Permissions {

    /**
     * Check if explicitly requesting camera permission is required.<br></br>
     * It is required in Android Marshmallow and above if "CAMERA" permission is requested in the
     * manifest.<br></br>
     * See [StackOverflow
     * question](http://stackoverflow.com/questions/32789027/android-m-camera-intent-permission-bug).
     */
    fun isExplicitCameraPermissionRequired(context: Context): Boolean {
        return hasCameraPermissionInManifest(context) &&
                context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    fun isExplicitCameraPermissionGranted(context: Context): Boolean {
        return hasCameraPermissionInManifest(context) &&
                context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if the app requests a specific permission in the manifest.
     *
     * [context] the context of your activity to check for permissions
     * @return true - the permission in requested in manifest, false - not.
     */
    fun hasCameraPermissionInManifest(context: Context): Boolean {
        val packageName = context.packageName
        try {
            val flags = PackageManager.GET_PERMISSIONS
            val packageInfo = when {
                Build.VERSION.SDK_INT >= 33 -> context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
                else -> context.packageManager.getPackageInfo(packageName, flags)
            }
            val declaredPermissions = packageInfo.requestedPermissions
            return declaredPermissions
                ?.any { it?.equals("android.permission.CAMERA", true) == true } == true
        } catch (e: PackageManager.NameNotFoundException) {
            // Since the package name cannot be found we return false below
            // because this means that the camera permission hasn't been declared
            // by the user for this package, so we can't show the camera app among
            // the list of apps.
            e.printStackTrace()
        }
        return false
    }

}