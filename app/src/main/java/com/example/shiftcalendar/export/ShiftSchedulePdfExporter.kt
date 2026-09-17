package com.example.shiftcalendar.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.shiftcalendar.data.calendar.ProductionCalendar
import com.example.shiftcalendar.data.db.dao.PersonWithMembership
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShiftSchedulePdfExporter {

    data class CrewData(
        val name: String,
        val periods: List<ShiftPeriod>,
        val members: List<PersonWithMembership>
    )

    private const val COLOR_WORK = 0xFF3B82F6.toInt()
    private const val COLOR_OFF = 0xFFE5E7EB.toInt()
    private const val COLOR_HOLIDAY = 0xFFFEE2E2.toInt()
    private const val COLOR_TEXT = 0xFF1A1F36.toInt()
    private const val COLOR_TEXT_LIGHT = 0xFF9CA3AF.toInt()
    private const val COLOR_BORDER = 0xFFD1D5DB.toInt()

    fun export(
        context: Context,
        crews: List<CrewData>,
        year: Int,
        calendar: ProductionCalendar
    ): File {
        val doc = PdfDocument()
        var pageNum = 1

        for (crew in crews) {
            for (month in 1..12) {
                val pageInfo = PdfDocument.PageInfo.Builder(842, 595, pageNum).create()
                val page = doc.startPage(pageInfo)
                drawMonthPage(page.canvas, crew, year, month, calendar)
                doc.finishPage(page)
                pageNum++
            }
        }

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "schedule_all_${year}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    private fun drawMonthPage(
        canvas: android.graphics.Canvas,
        crew: CrewData,
        year: Int, month: Int,
        calendar: ProductionCalendar
    ) {
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 16f; isFakeBoldText = true; color = COLOR_TEXT }
        val monthPaint = Paint().apply { isAntiAlias = true; textSize = 20f; isFakeBoldText = true; color = COLOR_TEXT }
        val dayHeaderPaint = Paint().apply { isAntiAlias = true; textSize = 9f; isFakeBoldText = true; color = COLOR_TEXT; textAlign = Paint.Align.CENTER }
        val namePaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = COLOR_TEXT }
        val legendPaint = Paint().apply { isAntiAlias = true; textSize = 8f; color = COLOR_TEXT }
        val symbolPaint = Paint().apply { isAntiAlias = true; textSize = 10f; textAlign = Paint.Align.CENTER; isFakeBoldText = true; color = Color.WHITE }
        val borderPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 0.5f; color = COLOR_BORDER }
        val holidayBg = Paint().apply { isAntiAlias = true; color = COLOR_HOLIDAY }
        val circlePaint = Paint().apply { isAntiAlias = true }

        canvas.drawText(crew.name, 30f, 30f, titlePaint)
        val monthName = java.time.Month.of(month)
            .getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, Locale("ru"))
            .replaceFirstChar { it.uppercase() }
        canvas.drawText("$monthName $year", 30f, 52f, monthPaint)

        val nameColWidth = 130f
        val dayColWidth = 23f
        val rowHeight = 20f
        val headerY = 80f
        val tableX = 30f
        val tableY = 95f

        val daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth()
        val totalWidth = nameColWidth + dayColWidth * daysInMonth

        canvas.drawText("ФИО", tableX + 5f, headerY + 4f, dayHeaderPaint.also { it.textAlign = Paint.Align.LEFT })

        for (day in 1..daysInMonth) {
            val x = tableX + nameColWidth + (day - 1) * dayColWidth
            val cx = x + dayColWidth / 2
            val date = kotlinx.datetime.LocalDate(year, month, day)
            val isRed = calendar.isHoliday(date) || calendar.isWeekend(date)
            if (isRed) {
                canvas.drawRect(RectF(x, tableY, x + dayColWidth, tableY + rowHeight * (crew.members.size + 1)), holidayBg)
            }
            val dayPaint = Paint(dayHeaderPaint).apply { color = if (isRed) 0xFFEF4444.toInt() else COLOR_TEXT }
            canvas.drawText(day.toString(), cx, headerY + 4f, dayPaint)
        }

        for (day in 1..daysInMonth) {
            val x = tableX + nameColWidth + (day - 1) * dayColWidth
            val cx = x + dayColWidth / 2
            val date = java.time.LocalDate.of(year, month, day)
            val dow = when (date.dayOfWeek.value) {
                1 -> "Пн"; 2 -> "Вт"; 3 -> "Ср"; 4 -> "Чт"; 5 -> "Пт"; 6 -> "Сб"; 7 -> "Вс"; else -> ""
            }
            val isWeekend = date.dayOfWeek.value >= 6
            val dowPaint = Paint(dayHeaderPaint).apply { textSize = 7f; color = if (isWeekend) 0xFFEF4444.toInt() else COLOR_TEXT_LIGHT }
            canvas.drawText(dow, cx, headerY + 14f, dowPaint)
        }

        var y = tableY + rowHeight
        val sortedMembers = crew.members.sortedBy { it.person.fullName }

        for (m in sortedMembers) {
            val nameDisplay = if (m.person.fullName.length > 20) m.person.fullName.take(18) + ".." else m.person.fullName
            canvas.drawText(nameDisplay, tableX + 5f, y + 14f, namePaint)

            for (day in 1..daysInMonth) {
                val x = tableX + nameColWidth + (day - 1) * dayColWidth
                val cx = x + dayColWidth / 2
                val date = kotlinx.datetime.LocalDate(year, month, day)
                val isActive = crew.periods.any { date >= it.startDate && date <= it.endDate }

                if (isActive) {
                    circlePaint.color = COLOR_WORK
                    canvas.drawCircle(cx, y + rowHeight / 2, 8f, circlePaint)
                    canvas.drawText("Р", cx, y + rowHeight / 2 + 4f, symbolPaint)
                } else {
                    circlePaint.color = COLOR_OFF
                    canvas.drawCircle(cx, y + rowHeight / 2, 3f, circlePaint)
                }
            }

            canvas.drawLine(tableX, y, tableX + totalWidth, y, borderPaint)
            y += rowHeight
            if (y > 540f) break
        }

        canvas.drawRect(RectF(tableX, tableY, tableX + totalWidth, y), borderPaint)
        canvas.drawLine(tableX + nameColWidth, tableY, tableX + nameColWidth, y, borderPaint)
        for (day in 1 until daysInMonth) {
            val x = tableX + nameColWidth + day * dayColWidth
            canvas.drawLine(x, tableY, x, y, borderPaint)
        }

        val legendY = y + 20f
        var legendX = tableX
        circlePaint.color = COLOR_WORK
        canvas.drawCircle(legendX + 5f, legendY, 6f, circlePaint)
        canvas.drawText("Р — рабочий день", legendX + 15f, legendY + 3f, legendPaint)
        legendX += 120f
        circlePaint.color = COLOR_OFF
        canvas.drawCircle(legendX + 5f, legendY, 3f, circlePaint)
        canvas.drawText("· — отдых", legendX + 15f, legendY + 3f, legendPaint)
        legendX += 80f
        canvas.drawRect(RectF(legendX, legendY - 6f, legendX + 12f, legendY + 6f), holidayBg)
        canvas.drawText("— праздник/выходной", legendX + 16f, legendY + 3f, legendPaint)

        val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru")).format(Date())
        canvas.drawText("Создано: $now · Приложение: Моя вахта", tableX, 575f, legendPaint)
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, file.name)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Отправить расписание").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
