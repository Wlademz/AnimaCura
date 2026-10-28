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

    /**
     * Resolves the human-readable display name of a file from a content Uri
     */
    fun getFileName(context: Context, uri: Uri): String {
        var name = "dokument"
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val retrieved = it.getString(nameIndex)
                        if (!retrieved.isNullOrBlank()) {
                            name = retrieved
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return name
    }

    /**
     * Copies any document or file (PDF, image, doc) into internal medical_attachments storage
     */
    fun saveAttachmentToInternalStorage(context: Context, uri: Uri, originalName: String?): Pair<String, String>? {
        return try {
            val directory = File(context.filesDir, "medical_attachments")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val rawName = originalName ?: getFileName(context, uri)
            val safeName = rawName.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "priloha_${System.currentTimeMillis()}" }
            val targetFile = File(directory, "${System.currentTimeMillis()}_$safeName")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }
            Pair(targetFile.absolutePath, rawName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Opens an attachment (PDF, image, etc.) using the Android system viewer
     */
    fun openAttachment(context: Context, pathOrUri: String) {
        try {
            val file = File(pathOrUri)
            val contentUri: Uri = if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                Uri.parse(pathOrUri)
            }

            val mimeType = when {
                pathOrUri.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                pathOrUri.endsWith(".jpg", ignoreCase = true) || pathOrUri.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                pathOrUri.endsWith(".png", ignoreCase = true) -> "image/png"
                else -> context.contentResolver.getType(contentUri) ?: "*/*"
            }

            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, mimeType)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Otvoriť lekársku správu / prílohu"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context,
                "Pre zobrazenie tohto súboru je potrebná aplikácia na čítanie PDF alebo prehliadač.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
}

