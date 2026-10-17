package com.example.syntax

import com.example.model.Language
import com.example.model.SyntaxError
import java.util.Stack

object SyntaxErrorDetector {

    fun checkErrors(code: String, language: Language): List<SyntaxError> {
        if (code.isBlank()) return emptyList()

        val errors = mutableListOf<SyntaxError>()
        val lines = code.split("\n")

        // 1. Bracket and quote matching
        val bracketStack = Stack<Pair<Char, Int>>() // char, lineNum
        var inSingleQuote = false
        var inDoubleQuote = false
        var quoteStartLine = 1

        for ((index, line) in lines.withIndex()) {
            val lineNum = index + 1
            var i = 0
            val len = line.length

            while (i < len) {
                val c = line[i]

                // Check escape
                if (c == '\\' && i + 1 < len) {
                    i += 2
                    continue
                }

                // Check comments
                if (!inSingleQuote && !inDoubleQuote) {
                    if (c == '/' && i + 1 < len && line[i + 1] == '/') {
                        break // rest of line is comment
                    }
                    if (c == '#' && (language == Language.PYTHON || language == Language.SHELL)) {
                        break
                    }
                }

                // String quotes
                if (c == '"' && !inSingleQuote) {
                    inDoubleQuote = !inDoubleQuote
                    if (inDoubleQuote) quoteStartLine = lineNum
                } else if (c == '\'' && !inDoubleQuote && language != Language.PLAIN_TEXT) {
                    inSingleQuote = !inSingleQuote
                    if (inSingleQuote) quoteStartLine = lineNum
                } else if (!inSingleQuote && !inDoubleQuote) {
                    when (c) {
                        '(', '{', '[' -> bracketStack.push(Pair(c, lineNum))
                        ')' -> {
                            if (bracketStack.isEmpty() || bracketStack.peek().first != '(') {
                                errors.add(SyntaxError(lineNum, "Beklenmeyen kapama parantezi ')'"))
                            } else {
                                bracketStack.pop()
                            }
                        }
                        '}' -> {
                            if (bracketStack.isEmpty() || bracketStack.peek().first != '{') {
                                errors.add(SyntaxError(lineNum, "Beklenmeyen süslü parantez '}'"))
                            } else {
                                bracketStack.pop()
                            }
                        }
                        ']' -> {
                            if (bracketStack.isEmpty() || bracketStack.peek().first != '[') {
                                errors.add(SyntaxError(lineNum, "Beklenmeyen köşeli parantez ']'"))
                            } else {
                                bracketStack.pop()
                            }
                        }
                    }
                }
                i++
            }

            // Unclosed single-line strings (except multi-line templates)
            if (inSingleQuote && !language.isMultiLineStringCapable()) {
                errors.add(SyntaxError(quoteStartLine, "Kapatılmamış tek tırnak dizesi (')"))
                inSingleQuote = false
            }
            if (inDoubleQuote && !language.isMultiLineStringCapable()) {
                errors.add(SyntaxError(quoteStartLine, "Kapatılmamış çift tırnak dizesi (\")"))
                inDoubleQuote = false
            }
        }

        while (!bracketStack.isEmpty()) {
            val unclosed = bracketStack.pop()
            val expected = when (unclosed.first) {
                '(' -> "')'"
                '{' -> "'}'"
                '[' -> "']'"
                else -> "kapanış"
            }
            errors.add(SyntaxError(unclosed.second, "Kapatılmamış parantez ${unclosed.first}, $expected bekleniyor"))
        }

        // HTML/XML tag matching check
        if (language == Language.HTML || language == Language.XML) {
            val selfClosingTags = setOf("area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr")
            val tagRegex = Regex("""<(/?[a-zA-Z0-9\-_]+)[^>]*>""")
            val tagStack = Stack<Pair<String, Int>>()

            for ((index, line) in lines.withIndex()) {
                val lineNum = index + 1
                val matches = tagRegex.findAll(line)
                for (match in matches) {
                    val raw = match.groupValues[1]
                    val isClosing = raw.startsWith("/")
                    val tagName = raw.removePrefix("/").lowercase()

                    if (selfClosingTags.contains(tagName) || match.value.endsWith("/>")) {
                        continue
                    }

                    if (isClosing) {
                        if (tagStack.isEmpty() || tagStack.peek().first != tagName) {
                            errors.add(SyntaxError(lineNum, "Eşleşmeyen kapanış etiketi: </$tagName>"))
                        } else {
                            tagStack.pop()
                        }
                    } else {
                        tagStack.push(Pair(tagName, lineNum))
                    }
                }
            }

            while (!tagStack.isEmpty()) {
                val unclosedTag = tagStack.pop()
                errors.add(SyntaxError(unclosedTag.second, "Kapatılmamış etiket: <${unclosedTag.first}>"))
            }
        }

        return errors.distinctBy { Pair(it.line, it.message) }
    }

    private fun Language.isMultiLineStringCapable(): Boolean {
        return this == Language.PYTHON || this == Language.JAVASCRIPT || this == Language.TYPESCRIPT || this == Language.KOTLIN
    }
}
