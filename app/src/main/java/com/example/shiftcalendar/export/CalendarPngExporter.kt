package com.example.shiftcalendar.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.FileProvider
import com.example.shiftcalendar.data.calendar.ProductionCalendar
import com.example.shiftcalendar.data.db.entity.CrewIcon
import com.example.shiftcalendar.data.repository.CrewWithPeriods
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.Locale

object CalendarPngExporter {

    private const val CELL_W = 70
    private const val CELL_H = 80
    private const val MONTH_COLS = 3
    private const val MONTH_PADDING = 40
    private const val HEADER_H = 80

    fun export(
        context: Context,
        year: Int,
        crews: List<CrewWithPeriods>,
        calendar: ProductionCalendar
    ): File {
        val monthW = CELL_W * 7 + 20
        val monthH = CELL_H * 7 + 40
        val totalW = monthW * MONTH_COLS + MONTH_PADDING * (MONTH_COLS + 1)
        val totalH = HEADER_H + monthH * 4 + MONTH_PADDING * 5

        val bitmap = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 36f
            color = Color.parseColor("#1A1F36")
            isFakeBoldText = true
        }
        canvas.drawText("График вахт $year", MONTH_PADDING.toFloat(), 50f, titlePaint)

        val monthNamePaint = Paint().apply {
            isAntiAlias = true
            textSize = 22f
            color = Color.parseColor("#1A1F36")
            isFakeBoldText = true
        }
        val dayNumPaint = Paint().apply {
            isAntiAlias = true
            textSize = 16f
            color = Color.parseColor("#1A1F36")
            textAlign = Paint.Align.CENTER
        }
        val dayNumRedPaint = Paint().apply {
            isAntiAlias = true
            textSize = 16f
            color = Color.parseColor("#EF4444")
            textAlign = Paint.Align.CENTER
        }
        val weekendColumnPaint = Paint().apply {
            isAntiAlias = true
            textSize = 14f
            color = Color.parseColor("#EF4444")
            textAlign = Paint.Align.CENTER
        }
        val weekdayColumnPaint = Paint().apply {
            isAntiAlias = true
            textSize = 14f
            color = Color.parseColor("#9CA3AF")
            textAlign = Paint.Align.CENTER
        }
        val holidayBgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#FEE2E2")
        }
        val crewIconPaint = Paint().apply {
            isAntiAlias = true
            textSize = 16f
            textAlign = Paint.Align.CENTER
        }

        for (month in 1..12) {
            val col = (month - 1) % MONTH_COLS
            val row = (month - 1) / MONTH_COLS
            val baseX = MONTH_PADDING + col * (monthW + MONTH_PADDING)
            val baseY = HEADER_H + MONTH_PADDING + row * (monthH + MONTH_PADDING)

            // Название месяца
            val monthName = java.time.Month.of(month)
                .getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, Locale("ru"))
                .replaceFirstChar { it.uppercase() }
            canvas.drawText(monthName, baseX.toFloat(), (baseY + 20).toFloat(), monthNamePaint)

            val gridStartY = baseY + 30

            // Дни недели
            val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            weekDays.forEachIndexed { i, d ->
                val cx = baseX + 10 + i * CELL_W + CELL_W / 2
                val cy = gridStartY + 15
                canvas.drawText(
                    d,
                    cx.toFloat(),
                    cy.toFloat(),
                    if (i >= 5) weekendColumnPaint else weekdayColumnPaint
                )
            }

            // Дни месяца
            val firstDate = LocalDate.of(year, month, 1)
            val daysInMonth = firstDate.lengthOfMonth()
            val firstDayOfWeek = firstDate.dayOfWeek.value
            val leading = firstDayOfWeek - 1

            for (day in 1..daysInMonth) {
                val index = leading + day - 1
                val cellCol = index % 7
                val cellRow = index / 7

                val cx = baseX + 10 + cellCol * CELL_W
                val cy = gridStartY + 30 + cellRow * CELL_H

                val date = LocalDate.of(year, month, day)
                val isHoliday = calendar.isHoliday(date.toKotlinx())
                val isWeekend = calendar.isWeekend(date.toKotlinx())
                val isRed = isHoliday || isWeekend

                // Фон для выходных/праздников
                if (isRed) {
                    canvas.drawRoundRect(
                        RectF(cx.toFloat() + 2, cy.toFloat() + 2, cx.toFloat() + CELL_W - 2, cy.toFloat() + CELL_H - 2),
                        6f, 6f, holidayBgPaint
                    )
                }

                // Число
                canvas.drawText(
                    day.toString(),
                    (cx + CELL_W / 2).toFloat(),
                    (cy + 22).toFloat(),
                    if (isRed) dayNumRedPaint else dayNumPaint
                )

                // Иконки вахт
                var iconX = cx + 8
                val iconY = cy + 50
                for (cwp in crews) {
                    val isActive = cwp.periods.any { p ->
                        date.toKotlinx() >= p.startDate && date.toKotlinx() <= p.endDate
                    }
                    if (isActive) {
                        val icon = CrewIcon.fromName(cwp.crew.iconType)
                        crewIconPaint.color = try {
                            Color.parseColor(cwp.crew.colorHex)
                        } catch (e: Exception) {
                            Color.parseColor("#3B82F6")
                        }
                        canvas.drawText(
                            icon.symbol,
                            (iconX + 7).toFloat(),
                            iconY.toFloat(),
                            crewIconPaint
                        )
                        iconX += 16
                        if (iconX + 16 > cx + CELL_W - 4) break
                    }
                }
            }
        }

        // Легенда внизу
        val legendY = totalH - 30
        var legendX = MONTH_PADDING
        val legendPaint = Paint().apply {
            isAntiAlias = true
            textSize = 18f
            color = Color.parseColor("#1A1F36")
        }
        for (cwp in crews) {
            val icon = CrewIcon.fromName(cwp.crew.iconType)
            crewIconPaint.color = try {
                Color.parseColor(cwp.crew.colorHex)
            } catch (e: Exception) {
                Color.parseColor("#3B82F6")
            }
            canvas.drawText(icon.symbol, legendX.toFloat(), legendY.toFloat(), crewIconPaint)
            legendX += 20
            canvas.drawText(cwp.crew.name, legendX.toFloat(), legendY.toFloat(), legendPaint)
            legendX += cwp.crew.name.length * 10 + 30
        }

        // Сохраняем
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
val file = File(dir, "calendar_${year}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
putExtra(Intent.EXTRA_TITLE, file.name)
putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
val chooser = Intent.createChooser(intent, "Отправить календарь").apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
context.startActivity(chooser)    }

    private fun java.time.LocalDate.toKotlinx() =
        kotlinx.datetime.LocalDate(year, monthValue, dayOfMonth)
}
