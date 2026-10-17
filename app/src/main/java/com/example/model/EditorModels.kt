package com.example.model

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

enum class AppLanguage {
    AZERBAIJANI,
    TURKISH,
    RUSSIAN,
    ENGLISH
}

enum class BuildTarget(val extension: String, val targetPlatform: String) {
    APK(".apk", "Android OS"),
    EXE(".exe", "Windows x64 / x86")
}

enum class LogLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

data class BuildLog(
    val message: String,
    val level: LogLevel = LogLevel.INFO,
    val timestamp: Long = System.currentTimeMillis()
)

data class SyntaxError(
    val line: Int,
    val message: String,
    val column: Int = 1
)
