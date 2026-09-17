package com.example.shiftcalendar.export

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShiftPeriodPdfExporter {

    data class CrewData(
        val name: String,
        val periods: List<ShiftPeriod>
    )

    fun export(context: Context, crews: List<CrewData>): File {
        val doc = PdfDocument()
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 20f; isFakeBoldText = true }
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 12f; isFakeBoldText = true }
        val cellPaint = Paint().apply { isAntiAlias = true; textSize = 12f }
        val sectionPaint = Paint().apply { isAntiAlias = true; textSize = 14f; isFakeBoldText = true }

        var pageNum = 1
        for (crew in crews) {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            var y = 50f
            canvas.drawText("Состав: ${crew.name}", 40f, y, titlePaint)
            y += 20f
            canvas.drawText("Список вахт", 40f, y, sectionPaint)
            y += 30f

            canvas.drawText("№", 40f, y, headerPaint)
            canvas.drawText("Даты", 100f, y, headerPaint)
            canvas.drawText("Дней", 380f, y, headerPaint)
            y += 6f
            canvas.drawLine(40f, y, 555f, y, cellPaint)
            y += 20f

            var totalDays = 0
            val sorted = crew.periods.sortedBy { it.startDate }

            sorted.forEachIndexed { idx, p ->
                if (y > 780f) return@forEachIndexed
                val days = (p.endDate.toEpochDays() - p.startDate.toEpochDays() + 1).toInt()
                totalDays += days

                canvas.drawText("${idx + 1}", 40f, y, cellPaint)
                canvas.drawText("${p.startDate} – ${p.endDate}", 100f, y, cellPaint)
                canvas.drawText("$days", 380f, y, cellPaint)
                y += 22f
            }

            y += 10f
            canvas.drawLine(40f, y, 555f, y, cellPaint)
            y += 20f
            canvas.drawText("Итого: ${sorted.size} вахт · $totalDays дней", 40f, y, headerPaint)

            val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru")).format(Date())
            canvas.drawText("Создано: $now", 40f, 800f, cellPaint)
            canvas.drawText("Приложение: Моя вахта", 40f, 820f, cellPaint)

            doc.finishPage(page)
            pageNum++
        }

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "shifts_all_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context, context.packageName + ".fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, file.name)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Отправить PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
