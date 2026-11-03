package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONArray
import org.json.JSONObject

data class PetSyncExport(
    val petName: String,
    val species: String,
    val breed: String,
    val birthDate: String,
    val gender: String,
    val chipNumber: String,
    val passportNumber: String,
    val latestWeight: Double,
    val activeMedications: List<String>,
    val recentVaccinations: List<String>,
    val exportTimestamp: Long = System.currentTimeMillis()
)

object QrCodeUtils {

    /**
     * Generates a QR Code Bitmap from given string content
     */
    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decodes a Barcode or QR Code from a Bitmap
     */
    fun decodeBarcodeFromBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            val hints = mapOf(
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.POSSIBLE_FORMATS to listOf(
                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.CODE_39
                )
            )
            val result = reader.decode(binaryBitmap, hints)
            result.text
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converts PetSyncExport into a JSON string formatted for QR encoding
     */
    fun toJson(export: PetSyncExport): String {
        val json = JSONObject()
        json.put("app", "AnimaCura_SK")
        json.put("name", export.petName)
        json.put("species", export.species)
        json.put("breed", export.breed)
        json.put("birth", export.birthDate)
        json.put("gender", export.gender)
        json.put("chip", export.chipNumber)
        json.put("pass", export.passportNumber)
        json.put("weight", export.latestWeight)
        json.put("meds", JSONArray(export.activeMedications))
        json.put("vacs", JSONArray(export.recentVaccinations))
        json.put("time", export.exportTimestamp)
        return json.toString()
    }

    /**
     * Parses a JSON string from a QR code into PetSyncExport
     */
    fun fromJson(jsonStr: String): PetSyncExport? {
        return try {
            val json = JSONObject(jsonStr)
            val medsArray = json.optJSONArray("meds")
            val medsList = mutableListOf<String>()
            if (medsArray != null) {
                for (i in 0 until medsArray.length()) {
                    medsList.add(medsArray.getString(i))
                }
            }
            val vacsArray = json.optJSONArray("vacs")
            val vacsList = mutableListOf<String>()
            if (vacsArray != null) {
                for (i in 0 until vacsArray.length()) {
                    vacsList.add(vacsArray.getString(i))
                }
            }

            PetSyncExport(
                petName = json.optString("name", "Neznáme zviera"),
                species = json.optString("species", "Pes"),
                breed = json.optString("breed", ""),
                birthDate = json.optString("birth", ""),
                gender = json.optString("gender", ""),
                chipNumber = json.optString("chip", ""),
                passportNumber = json.optString("pass", ""),
                latestWeight = json.optDouble("weight", 0.0),
                activeMedications = medsList,
                recentVaccinations = vacsList,
                exportTimestamp = json.optLong("time", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            null
        }
    }
}
