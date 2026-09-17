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
import com.example.shiftcalendar.data.calendar.ProductionCalendar
import com.example.shiftcalendar.data.db.dao.PersonWithMembership
import com.example.shiftcalendar.data.repository.CrewWithPeriods
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

object YearCalendarExporter {

    data class CrewInfo(
        val cwp: CrewWithPeriods,
        val members: List<PersonWithMembership>
    )

    private const val PAGE_W = 842
    private const val PAGE_H = 595

    private const val COLOR_WEEKEND = 0xFFEF4444.toInt()
    private const val COLOR_TEXT = 0xFF1A1F36.toInt()
    private const val COLOR_TEXT_LIGHT = 0xFF9CA3AF.toInt()
    private const val COLOR_BORDER = 0xFFD1D5DB.toInt()

    fun exportPdf(
        context: Context,
        year: Int,
        crewsInfo: List<CrewInfo>,
        calendar: ProductionCalendar
    ): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()
        val page = doc.startPage(pageInfo)
        drawYear(page.canvas, year, crewsInfo, calendar)
        doc.finishPage(page)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "year_${year}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun exportPng(
        context: Context,
        year: Int,
        crewsInfo: List<CrewInfo>,
        calendar: ProductionCalendar
    ): File {
        val bitmap = Bitmap.createBitmap(PAGE_W * 2, PAGE_H * 2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(2f, 2f)
        canvas.drawColor(Color.WHITE)
        drawYear(canvas, year, crewsInfo, calendar)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "year_${year}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return file
    }

    private fun drawYear(
        canvas: Canvas,
        year: Int,
        crewsInfo: List<CrewInfo>,
        calendar: ProductionCalendar
    ) {
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 14f; isFakeBoldText = true; color = COLOR_TEXT }
        val monthPaint = Paint().apply { isAntiAlias = true; textSize = 8f; isFakeBoldText = true; color = COLOR_TEXT }
        val dayNumPaint = Paint().apply { isAntiAlias = true; textSize = 5f; color = COLOR_TEXT; textAlign = Paint.Align.CENTER }
        val dowPaint = Paint().apply { isAntiAlias = true; textSize = 4f; color = COLOR_TEXT_LIGHT; textAlign = Paint.Align.CENTER }
        val legendTitlePaint = Paint().apply { isAntiAlias = true; textSize = 7f; isFakeBoldText = true; color = COLOR_TEXT }
        val legendPaint = Paint().apply { isAntiAlias = true; textSize = 6f; color = COLOR_TEXT }
        val borderPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 0.3f; color = COLOR_BORDER }
        val crewPaint = Paint().apply { isAntiAlias = true }

        canvas.drawText("График вахт $year", 20f, 18f, titlePaint)

        val legendLineHeight = 10f
        val legendLines = 1 + crewsInfo.size
        val legendHeight = legendLines * legendLineHeight + 10f

        val marginLeft = 20f
        val marginTop = 26f
        val gridW = PAGE_W - marginLeft * 2
        val gridH = PAGE_H - marginTop - legendHeight - 10f

        val cols = 4
        val rows = 3
        val cellW = gridW / cols
        val cellH = gridH / rows

        val monthsRu = arrayOf(
            "Январь","Февраль","Март","Апрель","Май","Июнь",
            "Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"
        )

        for (month in 1..12) {
            val idx = month - 1
            val col = idx % cols
            val row = idx / cols
            val x = marginLeft + col * cellW
            val y = marginTop + row * cellH

            drawMonth(canvas, year, month, monthsRu[idx],
                x, y, cellW, cellH,
                crewsInfo.map { it.cwp }, calendar,
                monthPaint, dayNumPaint, dowPaint, borderPaint, crewPaint)
        }

        var legendY = marginTop + gridH + 12f
        val legendX = marginLeft

        canvas.drawText("Легенда:", legendX, legendY, legendTitlePaint)
        legendY += legendLineHeight

        crewPaint.color = COLOR_WEEKEND
        canvas.drawRect(RectF(legendX, legendY - 5f, legendX + 8f, legendY + 1f), crewPaint)
        canvas.drawText("Выходной / праздник", legendX + 11f, legendY, legendPaint)

        for (info in crewsInfo) {
            legendY += legendLineHeight
            if (legendY > PAGE_H - 5f) break

            val color = try { Color.parseColor(info.cwp.crew.colorHex) } catch (e: Exception) { Color.BLUE }
            crewPaint.color = color
            canvas.drawRect(RectF(legendX, legendY - 5f, legendX + 8f, legendY + 1f), crewPaint)

            val membersNames = info.members.joinToString(", ") { shortName(it.person.fullName) }
            val memberCount = info.members.size
            val totalDays = info.cwp.periods.sumOf { p ->
                (p.endDate.toEpochDays() - p.startDate.toEpochDays() + 1).toInt()
            }
            val text = "${info.cwp.crew.name} ($memberCount чел, $totalDays дн): $membersNames"
            canvas.drawText(text.take(160), legendX + 11f, legendY, legendPaint)
        }
    }

    private fun drawMonth(
        canvas: Canvas,
        year: Int, month: Int, monthName: String,
        x: Float, y: Float, w: Float, h: Float,
        crews: List<CrewWithPeriods>,
        calendar: ProductionCalendar,
        monthPaint: Paint, dayNumPaint: Paint,
        dowPaint: Paint, borderPaint: Paint, crewPaint: Paint
    ) {
        canvas.drawText(monthName, x + 4f, y + 10f, monthPaint)

        val headerY = y + 14f
        val cellSize = (w - 8f) / 7f
        val rowH = (h - 20f) / 7f

        val dows = arrayOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс")
        for (i in 0..6) {
            val dx = x + 4f + i * cellSize + cellSize / 2
            val color = if (i >= 5) COLOR_WEEKEND else COLOR_TEXT_LIGHT
            val p = Paint(dowPaint).apply { this.color = color }
            canvas.drawText(dows[i], dx, headerY + 4f, p)
        }

        val first = LocalDate.of(year, month, 1)
        val daysInMonth = first.lengthOfMonth()
        val firstDow = first.dayOfWeek.value - 1

        for (day in 1..daysInMonth) {
            val index = firstDow + day - 1
            val col = index % 7
            val row = index / 7

            val cx = x + 4f + col * cellSize
            val cy = headerY + 8f + row * rowH

            val kotlinDate = kotlinx.datetime.LocalDate(year, month, day)
            val isRed = calendar.isHoliday(kotlinDate) || calendar.isWeekend(kotlinDate)

            var crewColor: Int? = null
            for (cwp in crews) {
                val isActive = cwp.periods.any { kotlinDate >= it.startDate && kotlinDate <= it.endDate }
                if (isActive) {
                    crewColor = try { Color.parseColor(cwp.crew.colorHex) } catch (e: Exception) { Color.BLUE }
                    break
                }
            }

            val bg = when {
                crewColor != null -> crewColor
                isRed -> COLOR_WEEKEND
                else -> null
            }

            if (bg != null) {
                crewPaint.color = bg
                canvas.drawRect(RectF(cx, cy, cx + cellSize - 1f, cy + rowH - 1f), crewPaint)
            }

            canvas.drawRect(RectF(cx, cy, cx + cellSize - 1f, cy + rowH - 1f), borderPaint)

            val textColor = if (bg != null) Color.WHITE else COLOR_TEXT
            val p = Paint(dayNumPaint).apply { this.color = textColor }
            canvas.drawText(day.toString(), cx + cellSize / 2, cy + rowH / 2 + 2f, p)
        }
    }

    private fun shortName(fullName: String): String {
        val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> fullName
            parts.size == 1 -> parts[0]
            parts.size == 2 -> parts[0] + " " + parts[1].first() + "."
            else -> parts[0] + " " + parts[1].first() + "." + parts[2].first() + "."
        }
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
        val chooser = Intent.createChooser(intent, "Отправить календарь").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
