package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {

    private fun sanitizePrefix(prefix: String): String {
        return prefix.replace(Regex("[^a-zA-Z0-9_]"), "_").trim('_').ifBlank { "pet_photo" }
    }

    /**
     * Creates a temporary file and its content Uri for camera capture via FileProvider
     */
    fun createTempCameraImageUri(context: Context, prefix: String = "pet_photo"): Pair<File, Uri>? {
        return try {
            val directory = File(context.cacheDir, "camera_photos")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val safePrefix = sanitizePrefix(prefix)
            val file = File.createTempFile("${safePrefix}_", ".jpg", directory)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Pair(file, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copies a temporary camera capture file into permanent internal app storage
     */
    fun saveCapturedFileToInternalStorage(context: Context, sourceFile: File, prefix: String = "pet_photo"): String? {
        return try {
            val directory = File(context.filesDir, "pet_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val safePrefix = sanitizePrefix(prefix)
            val fileName = "${safePrefix}_${System.currentTimeMillis()}.jpg"
            val targetFile = File(directory, fileName)
            sourceFile.copyTo(targetFile, overwrite = true)
            try { sourceFile.delete() } catch (_: Exception) {}
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves a Bitmap from camera capture into internal app storage and returns its file path
     */
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, prefix: String = "pet_photo"): String? {
        return try {
            val directory = File(context.filesDir, "pet_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val safePrefix = sanitizePrefix(prefix)
            val fileName = "${safePrefix}_${System.currentTimeMillis()}.jpg"
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
            val safePrefix = sanitizePrefix(prefix)
            val fileName = "${safePrefix}_${System.currentTimeMillis()}.jpg"
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
