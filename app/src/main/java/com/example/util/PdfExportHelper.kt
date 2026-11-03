package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.local.entity.MedicalRecord
import com.example.data.local.entity.Medication
import com.example.data.local.entity.Pet
import java.io.ByteArrayOutputStream

object PdfExportHelper {

    /**
     * Converts a Bitmap to base64 Data URI for embedding into HTML print template
     */
    fun bitmapToDataUri(bitmap: Bitmap?): String {
        if (bitmap == null) return ""
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Builds and prints the Pet Health Passport & Medical Records as a PDF
     */
    fun printPetMedicalRecord(
        context: Context,
        pet: Pet,
        latestWeight: Double,
        records: List<MedicalRecord>,
        medications: List<Medication>,
        qrCodeBitmap: Bitmap? = null
    ) {
        val qrDataUri = bitmapToDataUri(qrCodeBitmap)

        val htmlContent = buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>Zdravotná karta - ${pet.name}</title>
                    <style>
                        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 24px; color: #1e293b; }
                        .header { display: flex; justify-content: space-between; border-bottom: 2px solid #0d9488; padding-bottom: 16px; margin-bottom: 20px; }
                        .title { font-size: 24px; font-weight: bold; color: #0d9488; margin: 0; }
                        .subtitle { font-size: 13px; color: #64748b; margin-top: 4px; }
                        .section-title { font-size: 16px; font-weight: bold; color: #0f172a; border-left: 4px solid #0d9488; padding-left: 8px; margin: 20px 0 10px 0; }
                        table { width: 100%; border-collapse: collapse; margin-top: 8px; font-size: 12px; }
                        th { background-color: #f1f5f9; color: #334155; text-align: left; padding: 8px; border: 1px solid #cbd5e1; }
                        td { padding: 8px; border: 1px solid #e2e8f0; }
                        tr:nth-child(even) { background-color: #f8fafc; }
                        .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 13px; margin-bottom: 16px; }
                        .info-item { padding: 6px 10px; background: #f8fafc; border-radius: 6px; border: 1px solid #e2e8f0; }
                        .qr-box { text-align: right; }
                        .qr-box img { width: 110px; height: 110px; border: 1px solid #cbd5e1; border-radius: 6px; }
                        .footer { margin-top: 30px; font-size: 11px; color: #94a3b8; text-align: center; border-top: 1px solid #e2e8f0; padding-top: 12px; }
                        .badge { display: inline-block; padding: 2px 8px; font-size: 11px; font-weight: bold; border-radius: 4px; background: #e0f2fe; color: #0369a1; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <div>
                            <h1 class="title">🐾 VETERINÁRNA ZDRAVOTNÁ KARTA</h1>
                            <div class="subtitle">Aplikácia AnimaCura_SK Zdravotný Denník &bull; Vygenerované: ${java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}</div>
                        </div>
            """)

            if (qrDataUri.isNotEmpty()) {
                append("""
                        <div class="qr-box">
                            <img src="$qrDataUri" alt="QR Kód zvieratka" />
                            <div style="font-size: 9px; color: #64748b; margin-top: 2px;">Naskenovať v aplikácii</div>
                        </div>
                """)
            }

            append("""
                    </div>

                    <div class="section-title">IDENTIFIKAČNÉ ÚDAJE ZVIERATKA</div>
                    <div class="info-grid">
                        <div class="info-item"><strong>Meno:</strong> ${pet.name} (${pet.species})</div>
                        <div class="info-item"><strong>Plemeno:</strong> ${pet.breed.ifBlank { "Kríženec / Nešpecifikované" }}</div>
                        <div class="info-item"><strong>Pohlavie:</strong> ${pet.gender}</div>
                        <div class="info-item"><strong>Dátum narodenia:</strong> ${pet.birthDate.ifBlank { "Neznámy" }}</div>
                        <div class="info-item"><strong>Číslo mikročipu:</strong> ${pet.chipNumber.ifBlank { "Nečipovaný" }}</div>
                        <div class="info-item"><strong>Pas zvieratka:</strong> ${pet.passportNumber.ifBlank { "Bez petpasu" }}</div>
                        <div class="info-item"><strong>Posledná hmotnosť:</strong> ${if (latestWeight > 0.0) "$latestWeight kg" else "Neevidovaná"}</div>
                        <div class="info-item"><strong>Cieľová hmotnosť:</strong> ${if (pet.targetWeightKg > 0.0) "${pet.targetWeightKg} kg" else "Nenastavená"}</div>
                        <div class="info-item"><strong>Ošetrujúci veterinár:</strong> ${pet.vetName.ifBlank { "Nezadaný" }} (${pet.vetClinic.ifBlank { "Klinika nezadaná" }})</div>
                        <div class="info-item"><strong>Kontakt na veta:</strong> ${pet.vetPhone.ifBlank { "–" }}</div>
                        <div class="info-item"><strong>Osvojiteľ / Majiteľ:</strong> ${pet.adoptedBy.ifBlank { "Nezadaný" }}</div>
                        <div class="info-item"><strong>Pôvod / Dátum adopcie:</strong> ${pet.adoptedFrom.ifBlank { "–" }} ${if (pet.adoptionDate.isNotBlank()) "(${pet.adoptionDate})" else ""}</div>
                    </div>

                    <div class="section-title">AKTUÁLNE UŽÍVANÉ LIEKY A DOPLNKY (${medications.filter { it.isActive }.size})</div>
            """)

            val activeMeds = medications.filter { it.isActive }
            if (activeMeds.isEmpty()) {
                append("<p style='font-size: 12px; color: #64748b;'>Žiadne aktívne lieky nie sú evidované.</p>")
            } else {
                append("""
                    <table>
                        <thead>
                            <tr>
                                <th>Názov lieku</th>
                                <th>Forma</th>
                                <th>Dávkovanie</th>
                                <th>Frekvencia & Pokyny</th>
                                <th>Zahájenie liečby</th>
                            </tr>
                        </thead>
                        <tbody>
                """)
                for (med in activeMeds) {
                    append("""
                        <tr>
                            <td><strong>${med.name}</strong></td>
                            <td>${med.form}</td>
                            <td>${med.dosage}</td>
                            <td>${med.frequency} &bull; ${med.timingNotes}</td>
                            <td>${med.startDate}</td>
                        </tr>
                    """)
                }
                append("</tbody></table>")
            }

            append("""
                    <div class="section-title">ZDRAVOTNÉ ZÁZNAMY A OČKOVANIA (${records.size})</div>
            """)

            if (records.isEmpty()) {
                append("<p style='font-size: 12px; color: #64748b;'>Žiadne minulé zdravotné záznamy.</p>")
            } else {
                append("""
                    <table>
                        <thead>
                            <tr>
                                <th>Dátum</th>
                                <th>Typ zákroku</th>
                                <th>Názov / Vakcína</th>
                                <th>Diagnóza / Klinika</th>
                                <th>Ďalší termín</th>
                            </tr>
                        </thead>
                        <tbody>
                """)
                for (rec in records) {
                    append("""
                        <tr>
                            <td><strong>${rec.date}</strong></td>
                            <td><span class="badge">${rec.type}</span></td>
                            <td>${rec.title}</td>
                            <td>${rec.diagnosis.ifBlank { "–" }} ${if (rec.vetClinic.isNotBlank()) "(${rec.vetClinic})" else ""}</td>
                            <td>${rec.nextDueDate.ifBlank { "–" }}</td>
                        </tr>
                    """)
                }
                append("</tbody></table>")
            }

            append("""
                    <div class="footer">
                        Dokument slúži ako oficiálny prehľad zdravotného stavu zvieratka pri návšteve veterinárneho lekára alebo cestovaní so zvieraťom.
                    </div>
                </body>
                </html>
            """)
        }

        // Use Android WebView printing mechanism
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
                val jobName = "AnimaCura_SK_Karta_${pet.name}_${System.currentTimeMillis()}"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
    }
}
