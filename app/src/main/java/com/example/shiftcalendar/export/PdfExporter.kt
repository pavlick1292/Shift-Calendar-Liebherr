package com.example.shiftcalendar.export

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.shiftcalendar.data.db.entity.Person
import com.example.shiftcalendar.domain.model.WorkHours
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PdfExporter {

    fun export(context: Context, year: Int, rows: List<Pair<Person, WorkHours>>, norm: Double): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 20f; isFakeBoldText = true }
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 12f; isFakeBoldText = true }
        val cellPaint = Paint().apply { isAntiAlias = true; textSize = 11f }

        var y = 50f
        canvas.drawText("Учёт часов за $year", 40f, y, titlePaint)
        y += 30f

        val cols = listOf(40f, 220f, 360f, 430f, 500f)
        val headers = listOf("ФИО", "Профессия", "Обычные", "Итого", "Норма")
        headers.forEachIndexed { i, h -> canvas.drawText(h, cols[i], y, headerPaint) }
        y += 6f
        canvas.drawLine(40f, y, 555f, y, cellPaint)
        y += 16f

        rows.forEach { (p, h) ->
            if (y > 800f) return@forEach
            canvas.drawText(p.fullName.take(30), cols[0], y, cellPaint)
            canvas.drawText(p.profession.titleRu.take(18), cols[1], y, cellPaint)
            canvas.drawText(fmt(h.regularHours), cols[2], y, cellPaint)
            canvas.drawText(fmt(h.total), cols[3], y, cellPaint)
            canvas.drawText(fmt(h.yearlyNorm), cols[4], y, cellPaint)
            y += 18f
        }

        y += 16f
        val total = rows.sumOf { it.second.total }
        canvas.drawText("Итого: ${fmt(total)} ч", 40f, y, headerPaint)
        y += 18f
        canvas.drawText("Норма: ${fmt(norm)} ч", 40f, y, cellPaint)
        y += 18f
        if (total > norm) canvas.drawText("Переработка: +${fmt(total - norm)} ч", 40f, y, cellPaint)

        doc.finishPage(page)
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "hours_${year}_${System.currentTimeMillis()}.pdf")
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

    private fun fmt(v: Double): String = String.format(Locale.US, "%.1f", v)
}
