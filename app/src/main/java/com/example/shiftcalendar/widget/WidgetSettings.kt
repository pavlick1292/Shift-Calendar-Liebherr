package com.example.shiftcalendar.widget

import android.content.Context

enum class WidgetBg(
    val titleRu: String,
    val topColor: Long,
    val bottomColor: Long,
    val textColor: Long,
    val accentColor: Long
) {
    DARK(
        titleRu = "Тёмный",
        topColor = 0xFF1A1F36,
        bottomColor = 0xFF2A1F4A,
        textColor = 0xFFFFFFFF,
        accentColor = 0xFFFF9F43
    ),
    LIGHT(
        titleRu = "Светлый",
        topColor = 0xFFFFFFFF,
        bottomColor = 0xFFF0F0F5,
        textColor = 0xFF1A1F36,
        accentColor = 0xFF6C5CE7
    ),
    TRANSPARENT(
        titleRu = "Прозрачный",
        topColor = 0x00000000,
        bottomColor = 0x00000000,
        textColor = 0xFFFFFFFF,
        accentColor = 0xFFFF9F43
    );

    companion object {
        fun fromName(name: String): WidgetBg =
            entries.firstOrNull { it.name == name } ?: DARK
    }
}

object WidgetSettings {
    private const val PREFS = "widget_prefs"
    private const val KEY_BG = "widget_bg"

    fun getBackground(context: Context): WidgetBg {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_BG, WidgetBg.DARK.name) ?: WidgetBg.DARK.name
        return WidgetBg.fromName(name)
    }

    fun setBackground(context: Context, bg: WidgetBg) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BG, bg.name).apply()
    }
}
