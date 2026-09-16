package com.example.shiftcalendar.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.shiftcalendar.data.db.entity.Person
import com.example.shiftcalendar.domain.model.WorkHours
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object CsvExporter {

    fun export(context: Context, year: Int, rows: List<Pair<Person, WorkHours>>): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "hours_$year.csv")

        FileOutputStream(file).bufferedWriter(Charsets.UTF_8).use { w ->
            w.write("\uFEFF")
            w.write("ФИО;Профессия;Проживание;Обычные;Ночные;Дорога;Ручные;Итого;Норма;Переработка\n")
            rows.forEach { (p, h) ->
                w.write(listOf(
                    p.fullName, p.profession.titleRu, p.residence.titleRu,
                    fmt(h.regularHours), fmt(h.nightHours), fmt(h.roadHours),
                    fmt(h.manualAdjustment), fmt(h.total), fmt(h.yearlyNorm), fmt(h.overtime)
                ).joinToString(";"))
                w.write("\n")
            }
        }
        return file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Отправить CSV"))
    }

    private fun fmt(v: Double) = String.format(Locale.US, "%.1f", v)
}
