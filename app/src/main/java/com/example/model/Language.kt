package com.example.model

enum class Language(
    val displayName: String,
    val extensions: List<String>,
    val defaultExtension: String
) {
    HTML("HTML", listOf("html", "htm"), "html"),
    CSS("CSS", listOf("css"), "css"),
    JAVASCRIPT("JavaScript", listOf("js", "mjs", "cjs"), "js"),
    TYPESCRIPT("TypeScript", listOf("ts", "tsx"), "ts"),
    PYTHON("Python", listOf("py", "pyw"), "py"),
    KOTLIN("Kotlin", listOf("kt", "kts"), "kt"),
    JAVA("Java", listOf("java"), "java"),
    CPP("C++", listOf("cpp", "cc", "cxx", "hpp", "h"), "cpp"),
    C("C", listOf("c", "h"), "c"),
    CSHARP("C#", listOf("cs"), "cs"),
    JSON("JSON", listOf("json"), "json"),
    XML("XML", listOf("xml", "svg"), "xml"),
    SQL("SQL", listOf("sql"), "sql"),
    MARKDOWN("Markdown", listOf("md", "markdown"), "md"),
    SHELL("Shell Script", listOf("sh", "bash", "zsh"), "sh"),
    PHP("PHP", listOf("php"), "php"),
    RUST("Rust", listOf("rs"), "rs"),
    GO("Go", listOf("go"), "go"),
    PLAIN_TEXT("Plain Text", listOf("txt"), "txt");

    companion object {
        fun fromExtension(ext: String): Language {
            val normalized = ext.trim().lowercase().removePrefix(".")
            return entries.firstOrNull { it.extensions.contains(normalized) } ?: PLAIN_TEXT
        }

        fun fromFileName(fileName: String): Language {
            val ext = fileName.substringAfterLast(".", "")
            return fromExtension(ext)
        }
    }
}
