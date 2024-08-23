package com.example.tensorflowmodels.util


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

private const val FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
private const val COARSE_LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
private const val BACKGROUND_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION"
private const val PERMISSIONS_CLICK_DELAY_MS = 200

private const val lastPermissionRequestLaunchedAt = 0L

fun Context.checkTiramisuPermissionMedia(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            (
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                    )
}

fun Context.checkUpsideDownCakePermissionMedia(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
}

fun Context.checkPermissionReadStorage(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}

fun Context.checkPermissionCamera(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.isFineLocationPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.isCoarseLocationPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.areLocationPermissionsGranted(): Boolean {
    return (isCoarseLocationPermissionGranted() || isFineLocationPermissionGranted())
}

fun Context.hasCameraPermission(onGranted: () -> Unit, onRefused: () -> Unit) {
    checkPermissionCamera().let { isPermissionGranted ->
        if (isPermissionGranted) {
            onGranted()
        } else {
            onRefused()
        }
    }
}

fun Context.hasMediaPermission(onGranted: () -> Unit, onRefused: () -> Unit) {
    // Permission request logic
    if (checkTiramisuPermissionMedia()) {
        // Full access on Android 13 (API level 33) or higher
        onGranted()
    } else if (checkUpsideDownCakePermissionMedia()) {
        // Partial access on Android 14 (API level 34) or higher
        onGranted()
    }  else if (checkPermissionReadStorage()) {
        // Full access up to Android 12 (API level 32)
        onGranted()
    } else {
        onRefused()
    }
}

@Composable
fun Context.hasGPSPermission(onGranted: @Composable () -> Unit, onRefused: @Composable () -> Unit) {
    areLocationPermissionsGranted().let { isPermissionGranted ->
        if (isPermissionGranted) {
            onGranted()
        } else {
            onRefused()
        }
    }
}

fun ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>.launchGPSPermission(context: Context) {
    val packageInfo = context.packageManager.getPackageInfo(
        context.packageName, PackageManager.GET_PERMISSIONS
    )
    packageInfo.requestedPermissions?.let { requestedPermissions ->
        val permissionList = listOf(*requestedPermissions)
        val fineLocPermission = permissionList.contains(FINE_LOCATION_PERMISSION)
        val coarseLocPermission = permissionList.contains(COARSE_LOCATION_PERMISSION)
        val backgroundLocPermission = permissionList.contains(BACKGROUND_LOCATION_PERMISSION)

        // Request location permissions
        if (fineLocPermission) {
            requestLocationPermissions(true, backgroundLocPermission, this)
        } else if (coarseLocPermission) {
            requestLocationPermissions(false, backgroundLocPermission, this)
        } else {
            Log.w("Permissions", "Location permissions are missing")
        }
    }
}

private fun requestLocationPermissions(
    requestFineLocation: Boolean,
    requestBackgroundLocation: Boolean,
    launchPermission: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    val permissions: MutableList<String> = ArrayList()
    if (requestFineLocation) {
        permissions.add(FINE_LOCATION_PERMISSION)
    } else {
        permissions.add(COARSE_LOCATION_PERMISSION)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && requestBackgroundLocation) {
        permissions.add(BACKGROUND_LOCATION_PERMISSION)
    }
    launchPermission.launch(permissions.toTypedArray<String>())
}

@Composable
fun rememberPermissionsState(
    permissions: List<String>,
    onGrantedAction: () -> Unit = {},
    onDeniedAction: (List<String>) -> Unit = {},
    onPermanentlyDeniedAction: (List<String>) -> Unit = {}
): MultiplePermissionsState {
    // Create mutable permissions that can be requested individually
    val mutablePermissions = rememberMutablePermissionsState(permissions)

    // Refresh permissions when the lifecycle is resumed.
    PermissionsLifecycleCheckerEffect(mutablePermissions)

    val multiplePermissionsState = remember(permissions) {
        MultiplePermissionsState(mutablePermissions)
    }

    // Remember RequestMultiplePermissions launcher and assign it to multiplePermissionsState
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        multiplePermissionsState.updatePermissionsStatus(permissionsResult)

        if (!permissionsResult.containsValue(false)) {
            onGrantedAction()
        } else if (System.currentTimeMillis() - PERMISSIONS_CLICK_DELAY_MS
            < lastPermissionRequestLaunchedAt
        ) {
            onPermanentlyDeniedAction(permissionsResult.filter { !it.value }.keys.toList())
        } else {
            onDeniedAction(permissionsResult.filter { !it.value }.keys.toList())
        }
    }
    DisposableEffect(multiplePermissionsState, launcher) {
        multiplePermissionsState.launcher = launcher
        onDispose {
            multiplePermissionsState.launcher = null
        }
    }

    return multiplePermissionsState
}

@Composable
private fun PermissionsLifecycleCheckerEffect(
    permissions: List<PermissionState>,
    lifecycleEvent: Lifecycle.Event = Lifecycle.Event.ON_RESUME
) {
    // Check if the permission was granted when the lifecycle is resumed.
    // The user might've gone to the Settings screen and granted the permission.
    val permissionsCheckerObserver = remember(permissions) {
        LifecycleEventObserver { _, event ->
            if (event == lifecycleEvent) {
                for (permission in permissions) {
                    // If the permission is revoked, check again. We don't check if the permission
                    // was denied as that triggers a process restart.
                    if (permission.status != PermissionStatus.Granted) {
                        permission.refreshPermissionStatus()
                    }
                }
            }
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, permissionsCheckerObserver) {
        lifecycle.addObserver(permissionsCheckerObserver)
        onDispose { lifecycle.removeObserver(permissionsCheckerObserver) }
    }
}

@Composable
private fun rememberMutablePermissionsState(
    permissions: List<String>
): List<PermissionState> {
    val context = LocalContext.current
    val activity = context.findActivity()

    val mutablePermissions: List<PermissionState> = remember(permissions) {
        return@remember permissions.map { PermissionState(it, context, activity) }
    }
    return mutablePermissions
}

private fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

object Permissions {
    val GPSPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val MediaPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) listOf(
        Manifest.permission.READ_MEDIA_IMAGES
    ) else listOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )
}