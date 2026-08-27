package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageStorageHelper {

    /**
     * Saves a Bitmap from camera capture into internal app storage and returns its file path
     */
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, prefix: String = "pet_photo"): String? {
        return try {
            val directory = File(context.filesDir, "pet_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
            val file = File(directory, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copies an image selected from the gallery (Uri) into internal app storage and returns its file path
     */
    fun saveUriToInternalStorage(context: Context, uri: Uri, prefix: String = "pet_photo"): String? {
        return try {
            val directory = File(context.filesDir, "pet_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
            val file = File(directory, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
