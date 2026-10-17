package com.example.model

data class CodeFile(
    val id: String,
    val name: String,
    val extension: String,
    val content: String,
    val path: String = "",
    val isOpen: Boolean = true,
    val isModified: Boolean = false
) {
    val fullName: String
        get() = if (name.endsWith(".$extension", ignoreCase = true)) name else "$name.$extension"
}
