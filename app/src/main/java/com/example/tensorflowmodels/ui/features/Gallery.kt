package com.example.tensorflowmodels.ui.features

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.tensorflowmodels.R
import com.example.tensorflowmodels.ui.custom.SettingButton
import com.example.tensorflowmodels.util.Permissions
import com.example.tensorflowmodels.util.createTempPictureUri
import com.example.tensorflowmodels.util.hasCameraPermission
import com.example.tensorflowmodels.util.hasMediaPermission
import com.example.tensorflowmodels.util.rememberPermissionsState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GalleryBottomSheet(
    onDismissSheet: (Uri) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val gallerySheetState = rememberModalBottomSheetState()
    var uri by remember { mutableStateOf(Uri.EMPTY) }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            println("$success ${uri.path}")
            if (success) {
                onDismissSheet.invoke(uri)
                //uri = context.createTempPictureUri()
            }
        }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.e("cameraPermissionLauncher","$isGranted")
        if (isGranted) {
            //uri = context.createTempPictureUri()
            cameraLauncher.launch(uri)
        }
    }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { result ->
            Log.e("galleryLauncher","$result")
            result?.let { noNullUri ->
                onDismissSheet.invoke(noNullUri)
            }
        }

    val mediaPermission = rememberPermissionsState(Permissions.MediaPermissions)

    ModalBottomSheet(
        modifier = Modifier.padding(8.dp),
        onDismissRequest = onDismissRequest,
        sheetState = gallerySheetState
    ) {
        SettingButton(
            icon = ImageVector.vectorResource(R.drawable.ic_gallery),
            buttonText = stringResource(R.string.gallery),
        ) {
            context.hasMediaPermission(
                onGranted = {
                    galleryLauncher.launch("image/*")
                },
                onRefused = {
                    mediaPermission.launchPermissionRequestsAndAction()
                }
            )
            //gallerySheetState.isVisible
            //onDismissRequest()
        }
        SettingButton(
            icon = ImageVector.vectorResource(R.drawable.ic_camera),
            buttonText = stringResource(R.string.camera),
        ) {
            context.hasCameraPermission(
                onGranted = {
                    uri = context.createTempPictureUri()
                    cameraLauncher.launch(uri)
                },
                onRefused = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
            )
            //onDismissRequest()
        }
    }
}