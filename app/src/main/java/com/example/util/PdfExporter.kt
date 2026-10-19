package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun exportCodeToPdf(
        context: Context,
        fileName: String,
        content: String
    ): File {
        val document = PdfDocument()
        val pageWidth = 595 // Standard A4 width in PostScript points
        val pageHeight = 842 // Standard A4 height in PostScript points
        val marginHorizontal = 36f
        val marginTop = 48f
        val marginBottom = 40f
        val lineHeight = 14f

        val headerPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 14f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val subHeaderPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val lineNumPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 10f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val codePaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val dividerPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 1f
        }

        val lines = content.lines()
        val availableHeight = pageHeight - marginTop - marginBottom - 50f
        val linesPerPage = (availableHeight / lineHeight).toInt().coerceAtLeast(1)

        val totalPages = ((lines.size + linesPerPage - 1) / linesPerPage).coerceAtLeast(1)
        val timeStamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

        for (pageIdx in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIdx + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Draw Header
            canvas.drawText("Falambons Code Studio — $fileName", marginHorizontal, marginTop, headerPaint)
            canvas.drawText("Credit by : falambons developer | Exported: $timeStamp | Page ${pageIdx + 1}/$totalPages",
                marginHorizontal, marginTop + 16f, subHeaderPaint)

            canvas.drawLine(marginHorizontal, marginTop + 24f, pageWidth - marginHorizontal, marginTop + 24f, dividerPaint)

            // Draw Lines
            val startLineIndex = pageIdx * linesPerPage
            val endLineIndex = (startLineIndex + linesPerPage).coerceAtMost(lines.size)

            var y = marginTop + 44f
            for (i in startLineIndex until endLineIndex) {
                val lineNumStr = String.format(Locale.US, "%4d | ", i + 1)
                canvas.drawText(lineNumStr, marginHorizontal, y, lineNumPaint)

                val lineText = lines[i]
                // Truncate overly long single lines if exceeding page boundary
                val maxChars = 85
                val displayLine = if (lineText.length > maxChars) lineText.substring(0, maxChars) + "…" else lineText
                canvas.drawText(displayLine, marginHorizontal + 48f, y, codePaint)
                y += lineHeight
            }

            // Draw Footer
            canvas.drawLine(marginHorizontal, pageHeight - marginBottom + 8f, pageWidth - marginHorizontal, pageHeight - marginBottom + 8f, dividerPaint)
            canvas.drawText("Generated offline with Falambons Code Studio", marginHorizontal, pageHeight - marginBottom + 20f, subHeaderPaint)

            document.finishPage(page)
        }

        val outputDir = File(context.cacheDir, "shared_pdfs").apply { mkdirs() }
        val cleanName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val outputFile = File(outputDir, "${cleanName}.pdf")

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }
}
