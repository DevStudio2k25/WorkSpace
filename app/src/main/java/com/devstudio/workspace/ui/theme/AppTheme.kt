package com.devstudio.workspace.ui.theme

enum class AppTheme {
    FOREST,
    OCEAN,
    SUNSET
}

fun AppTheme.getDisplayName(): String {
    return when (this) {
        AppTheme.FOREST -> "Forest 🌲"
        AppTheme.OCEAN -> "Ocean 🌊"
        AppTheme.SUNSET -> "Sunset 🌅"
    }
}
