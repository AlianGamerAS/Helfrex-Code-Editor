package com.example.model

data class CodeFolder(
    val id: String,
    val name: String,
    val parentPath: String = "/"
) {
    val fullPath: String
        get() = if (parentPath == "/" || parentPath.isEmpty()) "/$name" else "$parentPath/$name"
}
