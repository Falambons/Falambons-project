package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream

object QrCodeGenerator {

    fun generateCustomQr(
        content: String,
        size: Int = 600,
        foregroundColor: Int = Color.rgb(0, 229, 255), // Cyan neon default
        backgroundColor: Int = Color.rgb(10, 10, 16),  // Dark background
        centerLogoText: String = "FLB", // Falambons brand tag
        showCenterLogo: Boolean = true
    ): Bitmap {
        val hints = HashMap<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H) // High error correction to allow center logo
            put(EncodeHintType.MARGIN, 2)
        }

        // Limit content length for QR scanning feasibility (QR max ~2.9KB)
        val safeContent = if (content.length > 1800) content.substring(0, 1800) else content
        val bitMatrix = QRCodeWriter().encode(
            safeContent.ifEmpty { "Falambons Code Studio" },
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) foregroundColor else backgroundColor)
            }
        }

        // Draw custom center logo/badge if enabled
        if (showCenterLogo) {
            val canvas = Canvas(bitmap)
            val logoSize = (size * 0.22f)
            val left = (size - logoSize) / 2f
            val top = (size - logoSize) / 2f
            val rect = RectF(left, top, left + logoSize, top + logoSize)

            // Badge Background
            val badgeBgPaint = Paint().apply {
                color = backgroundColor
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(rect, 16f, 16f, badgeBgPaint)

            // Badge Border
            val borderPaint = Paint().apply {
                color = foregroundColor
                strokeWidth = 4f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            canvas.drawRoundRect(rect, 16f, 16f, borderPaint)

            // Badge Text
            val textPaint = Paint().apply {
                color = foregroundColor
                textSize = logoSize * 0.35f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            val textY = top + (logoSize / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(centerLogoText.take(4).uppercase(), size / 2f, textY, textPaint)
        }

        return bitmap
    }

    fun saveQrToFile(context: Context, bitmap: Bitmap, fileName: String = "falambons_qr"): File {
        val outputDir = File(context.cacheDir, "shared_qr").apply { mkdirs() }
        val file = File(outputDir, "${fileName}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}
