package com.example.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.example.model.Language
import java.util.regex.Pattern

object SyntaxHighlighter {

    private val KEYWORDS = listOf(
        // Kotlin / Java / C++ / C#
        "package", "import", "class", "interface", "object", "val", "var", "fun", "function",
        "return", "if", "else", "for", "while", "do", "when", "switch", "case", "break",
        "continue", "try", "catch", "finally", "throw", "throws", "new", "this", "super",
        "public", "private", "protected", "internal", "abstract", "final", "open", "override",
        "enum", "sealed", "data", "struct", "namespace", "using", "typedef", "const", "let",
        // Python
        "def", "elif", "except", "is", "lambda", "pass", "raise", "with", "yield", "async", "await",
        "from", "as", "global", "nonlocal", "assert",
        // SQL
        "select", "from", "where", "insert", "into", "update", "delete", "create", "table", "drop",
        "alter", "join", "inner", "left", "right", "outer", "group", "by", "order", "having", "limit",
        // General
        "type", "module", "export", "default"
    )

    private val BUILTIN_TYPES = listOf(
        "String", "Int", "Double", "Float", "Boolean", "Long", "Short", "Byte", "Char", "Unit",
        "void", "int", "double", "float", "bool", "boolean", "char", "long", "short", "byte",
        "List", "Map", "Set", "Array", "ArrayList", "HashMap", "Any", "None", "null", "true",
        "false", "undefined", "NaN", "self", "str", "dict", "tuple"
    )

    fun highlight(
        code: String,
        language: Language,
        isDark: Boolean,
        errorLines: Set<Int> = emptySet()
    ): AnnotatedString {
        if (code.isEmpty()) return AnnotatedString("")

        val keywordColor = if (isDark) Color(0xFF569CD6) else Color(0xFF0000FF)
        val typeColor = if (isDark) Color(0xFF4EC9B0) else Color(0xFF267F99)
        val stringColor = if (isDark) Color(0xFFCE9178) else Color(0xFFA31515)
        val numberColor = if (isDark) Color(0xFFB5CEA8) else Color(0xFF098658)
        val commentColor = if (isDark) Color(0xFF6A9955) else Color(0xFF008000)
        val tagColor = if (isDark) Color(0xFF4EC9B0) else Color(0xFF800000)
        val attrColor = if (isDark) Color(0xFF9CDCFE) else Color(0xFFE50000)
        val defaultColor = if (isDark) Color(0xFFD4D4D4) else Color(0xFF1E1E1E)
        val errorUnderlineColor = Color(0xFFF44747)

        return buildAnnotatedString {
            append(code)
            addStyle(SpanStyle(color = defaultColor, fontFamily = FontFamily.Monospace), 0, code.length)

            when (language) {
                Language.HTML, Language.XML -> {
                    // Highlight HTML/XML tags
                    val tagPattern = Pattern.compile("<(/?[a-zA-Z0-9\\-_]+)([^>]*)>")
                    val matcher = tagPattern.matcher(code)
                    while (matcher.find()) {
                        val start = matcher.start(1)
                        val end = matcher.end(1)
                        if (start in 0..code.length && end in 0..code.length) {
                            addStyle(SpanStyle(color = tagColor, fontWeight = FontWeight.Bold), start, end)
                        }

                        // Attributes inside tag
                        val attrContent = matcher.group(2) ?: ""
                        val attrOffset = matcher.start(2)
                        val attrPattern = Pattern.compile("([a-zA-Z\\-_:]+)\\s*=")
                        val attrMatcher = attrPattern.matcher(attrContent)
                        while (attrMatcher.find()) {
                            val aStart = attrOffset + attrMatcher.start(1)
                            val aEnd = attrOffset + attrMatcher.end(1)
                            if (aStart in 0..code.length && aEnd in 0..code.length) {
                                addStyle(SpanStyle(color = attrColor), aStart, aEnd)
                            }
                        }
                    }

                    // Comments <!-- ... -->
                    val commentPattern = Pattern.compile("<!--[\\s\\S]*?-->")
                    val cMatcher = commentPattern.matcher(code)
                    while (cMatcher.find()) {
                        addStyle(SpanStyle(color = commentColor), cMatcher.start(), cMatcher.end())
                    }
                }
                else -> {
                    // Regular languages: Keywords, Types, Numbers, Strings, Comments

                    // Multi-line comments
                    val multiComment = Pattern.compile("/\\*[\\s\\S]*?\\*/")
                    val mcMatcher = multiComment.matcher(code)
                    while (mcMatcher.find()) {
                        addStyle(SpanStyle(color = commentColor), mcMatcher.start(), mcMatcher.end())
                    }

                    // Single-line comments // or #
                    val commentRegex = if (language == Language.PYTHON || language == Language.SHELL) {
                        Pattern.compile("#.*")
                    } else {
                        Pattern.compile("//.*")
                    }
                    val scMatcher = commentRegex.matcher(code)
                    while (scMatcher.find()) {
                        addStyle(SpanStyle(color = commentColor), scMatcher.start(), scMatcher.end())
                    }

                    // Numbers
                    val numPattern = Pattern.compile("\\b(\\d+(\\.\\d+)?|0x[0-9a-fA-F]+)[fFL]?\\b")
                    val numMatcher = numPattern.matcher(code)
                    while (numMatcher.find()) {
                        addStyle(SpanStyle(color = numberColor), numMatcher.start(), numMatcher.end())
                    }

                    // Keywords
                    val wordPattern = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
                    val wordMatcher = wordPattern.matcher(code)
                    while (wordMatcher.find()) {
                        val word = wordMatcher.group()
                        if (KEYWORDS.contains(word)) {
                            addStyle(
                                SpanStyle(color = keywordColor, fontWeight = FontWeight.SemiBold),
                                wordMatcher.start(),
                                wordMatcher.end()
                            )
                        } else if (BUILTIN_TYPES.contains(word)) {
                            addStyle(
                                SpanStyle(color = typeColor),
                                wordMatcher.start(),
                                wordMatcher.end()
                            )
                        }
                    }
                }
            }

            // Strings: "..." or '...' or `...`
            val stringPattern = Pattern.compile("\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*'|`(\\\\.|[^`\\\\])*`")
            val strMatcher = stringPattern.matcher(code)
            while (strMatcher.find()) {
                addStyle(SpanStyle(color = stringColor), strMatcher.start(), strMatcher.end())
            }

            // Error lines wavy red underline
            if (errorLines.isNotEmpty()) {
                val lines = code.split("\n")
                var currentOffset = 0
                for ((idx, line) in lines.withIndex()) {
                    val lineNum = idx + 1
                    val lineEnd = currentOffset + line.length
                    if (errorLines.contains(lineNum) && line.isNotBlank()) {
                        val trimStart = line.indexOfFirst { !it.isWhitespace() }
                        if (trimStart >= 0) {
                            addStyle(
                                SpanStyle(
                                    color = errorUnderlineColor,
                                    textDecoration = TextDecoration.Underline
                                ),
                                currentOffset + trimStart,
                                lineEnd
                            )
                        }
                    }
                    currentOffset = lineEnd + 1
                }
            }
        }
    }
}
