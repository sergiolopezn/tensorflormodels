package com.example.tensorflowmodels.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.tensorflowmodels.BuildConfig
import java.io.File
import java.util.Date

fun Context.createTempPictureUri(
): Uri {
    val provider = "${BuildConfig.APPLICATION_ID}.provider"
    val fileExtension = ".jpg"
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val fileName = "JPEG_${timeStamp}_"
    val tempFile = File.createTempFile(
        fileName, fileExtension, cacheDir
    ).apply {
        createNewFile()
    }

    return FileProvider.getUriForFile(applicationContext, provider, tempFile)
}

fun Context.dialPhoneNumber(phoneNumber: String?) {
    phoneNumber?.let { phone ->
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        startActivity(intent)
    }
}

fun Context.toBitmap(image: Uri): Bitmap {
    return contentResolver.openInputStream(image).let {
        BitmapFactory.decodeStream(it)
    }
}