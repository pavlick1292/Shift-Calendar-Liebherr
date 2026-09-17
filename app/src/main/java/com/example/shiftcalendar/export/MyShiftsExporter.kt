package com.example.shiftcalendar.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.shiftcalendar.data.db.entity.Person
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object MyShiftsExporter {

    private const val PAGE_W = 842
    private const val PAGE_H = 595

    private const val COLOR_TEXT = 0xFF1A1F36.toInt()
    private const val COLOR_TEXT_LIGHT = 0xFF9CA3AF.toInt()
    private const val COLOR_BORDER = 0xFFD1D5DB.toInt()
    private const val COLOR_ACCENT = 0xFF6C5CE7.toInt()

    fun exportPdf(
        context: Context,
        person: Person,
        year: Int,
        periods: List<ShiftPeriod>
    ): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()
        val page = doc.startPage(pageInfo)
        drawMyShifts(page.canvas, person, year, periods)
        doc.finishPage(page)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "my_shifts_${year}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun exportPng(
        context: Context,
        person: Person,
        year: Int,
        periods: List<ShiftPeriod>
    ): File {
        val bitmap = Bitmap.createBitmap(PAGE_W * 2, PAGE_H * 2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(2f, 2f)
        canvas.drawColor(Color.WHITE)
        drawMyShifts(canvas, person, year, periods)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "my_shifts_${year}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return file
    }

    private fun drawMyShifts(
        canvas: Canvas,
        person: Person,
        year: Int,
        periods: List<ShiftPeriod>
    ) {
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; isFakeBoldText = true; color = COLOR_TEXT }
        val subtitlePaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = COLOR_TEXT_LIGHT }
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 12f; isFakeBoldText = true; color = COLOR_TEXT }
        val cellPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = COLOR_TEXT }
        val accentPaint = Paint().apply { isAntiAlias = true; textSize = 14f; isFakeBoldText = true; color = COLOR_ACCENT }
        val linePaint = Paint().apply { isAntiAlias = true; strokeWidth = 0.5f; color = COLOR_BORDER }

        val marginX = 40f
        var y = 60f

        // Заголовок
        canvas.drawText("Мои вахты · $year", marginX, y, titlePaint)
        y += 22f
        canvas.drawText(person.fullName, marginX, y, subtitlePaint)
        y += 22f

        // Линия
        canvas.drawLine(marginX, y, PAGE_W - marginX, y, linePaint)
        y += 22f

        // Заголовки таблицы
        val colMonth = marginX
        val colDates = marginX + 200f
        val colDays = marginX + 500f

        canvas.drawText("Месяц", colMonth, y, headerPaint)
        canvas.drawText("Даты", colDates, y, headerPaint)
        canvas.drawText("Дней", colDays, y, headerPaint)
        y += 6f
        canvas.drawLine(marginX, y, PAGE_W - marginX, y, linePaint)
        y += 18f

        // Группируем вахты по месяцам
        val monthsRu = arrayOf(
            "Январь","Февраль","Март","Апрель","Май","Июнь",
            "Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"
        )

        val sorted = periods.sortedBy { it.startDate }
        var totalDays = 0

        // Группируем по месяцу начала
        val byMonth = sorted.groupBy { it.startDate.monthNumber }

        for (month in 1..12) {
            val monthPeriods = byMonth[month] ?: continue
            val firstInMonth = true
            for (p in monthPeriods) {
                val days = (p.endDate.toEpochDays() - p.startDate.toEpochDays() + 1).toInt()
                totalDays += days

                if (firstInMonth) {
                    canvas.drawText(monthsRu[month - 1], colMonth, y, cellPaint)
                }
                canvas.drawText("${p.startDate} – ${p.endDate}", colDates, y, cellPaint)
                canvas.drawText("$days", colDays, y, cellPaint)
                y += 18f
            }
        }

        y += 10f
        canvas.drawLine(marginX, y, PAGE_W - marginX, y, linePaint)
        y += 22f

        // Итого
        canvas.drawText("Итого: ${sorted.size} вахт · $totalDays дней",
            marginX, y, accentPaint)

        // Дата создания
        val now = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
            .format(java.util.Date())
        val footerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = COLOR_TEXT_LIGHT }
        canvas.drawText("Создано: $now · Приложение: Моя вахта",
            marginX, PAGE_H - 20f, footerPaint)
    }

    fun share(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context, context.packageName + ".fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Отправить график").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
