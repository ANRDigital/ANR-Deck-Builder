package com.shuneault.netrunnerdeckbuilder.ui

import android.content.Context

class ThemeHelper {
    companion object {
        fun getTheme(factionCode: String, context: Context): Int {
            val themeName = "Theme.Netrunner_" + factionCode.replace("-", "")
            return context.resources.getIdentifier(themeName, "style", context.packageName)
        }
    }
}