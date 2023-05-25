package com.smartestidea.a2fac.core

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.google.android.material.snackbar.Snackbar
import com.smartestidea.a2fac.R

@RequiresApi(Build.VERSION_CODES.M)
fun requestAndroid11Permission(p:String, activity:ComponentActivity, coordinator:CoordinatorLayout?, function: ()->Unit = {}) {
    val permissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        shouldShowRequestPermissionRationale(activity,p)
        if(isGranted) function()
        if (!isGranted && coordinator!=null) Snackbar.make(
            coordinator,
            activity.resources.getString(R.string.grant_permission),
            Snackbar.LENGTH_SHORT
        ).show()
    }
    permissionLauncher.launch(p)
}