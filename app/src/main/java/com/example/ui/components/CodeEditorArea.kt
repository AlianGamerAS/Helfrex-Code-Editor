package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.EditorStrings
import com.example.model.CodeFile
import com.example.model.Language
import com.example.model.SyntaxError
import com.example.syntax.SyntaxHighlighter

@Composable
fun CodeEditorArea(
    activeFile: CodeFile?,
    syntaxErrors: List<SyntaxError>,
    strings: EditorStrings,
    isDark: Boolean,
    onContentChange: (String) -> Unit,
    onQuickNewFile: () -> Unit,
    onQuickOpenFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeFile == null) {
        EmptyEditorState(
            strings = strings,
            onNewFile = onQuickNewFile,
            onOpenFile = onQuickOpenFile,
            modifier = modifier
        )
        return
    }

    var textFieldValue by remember(activeFile.id) {
        mutableStateOf(TextFieldValue(activeFile.content))
    }

    // Keep textFieldValue in sync if active file content is updated externally
    LaunchedEffect(activeFile.content) {
        if (textFieldValue.text != activeFile.content) {
            textFieldValue = textFieldValue.copy(text = activeFile.content)
        }
    }

    val language = remember(activeFile.extension) {
        Language.fromExtension(activeFile.extension)
    }

    val errorLines = remember(syntaxErrors) {
        syntaxErrors.map { it.line }.toSet()
    }

    val lines = remember(textFieldValue.text) {
        textFieldValue.text.split("\n")
    }
    val totalLines = lines.size.coerceAtLeast(1)

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // Quick symbol insertion
    val onInsertSymbol: (String) -> Unit = { symbol ->
        val currentText = textFieldValue.text
        val selection = textFieldValue.selection
        val newText = buildString {
            append(currentText.substring(0, selection.start))
            append(symbol)
            append(currentText.substring(selection.end))
        }
        val newCursor = selection.start + symbol.length
        textFieldValue = TextFieldValue(newText, TextRange(newCursor))
        onContentChange(newText)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main Editor Canvas (Gutter + Text Field)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
            ) {
                // Gutter / Line Numbers
                Column(
                    modifier = Modifier
                        .background(
                            if (isDark) Color(0xFF181818) else Color(0xFFF0F0F0)
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..totalLines) {
                        val hasError = errorLines.contains(i)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(20.dp)
                        ) {
                            if (hasError) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFF44747))
                                    )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "$i",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (hasError) Color(0xFFF44747) else if (isDark) Color(0xFF888888) else Color(0xFF666666)
                            )
                        }
                    }
                }

                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(if (isDark) Color(0xFF2E2E2E) else Color(0xFFDDDDDD))
                )

                // Editable Code Field with horizontal scroll
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScrollState)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    val syntaxTransformation = remember(language, isDark, errorLines) {
                        VisualTransformation { text ->
                            val annotated = SyntaxHighlighter.highlight(
                                code = text.text,
                                language = language,
                                isDark = isDark,
                                errorLines = errorLines
                            )
                            TransformedText(annotated, OffsetMapping.Identity)
                        }
                    }

                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue
                            onContentChange(newValue.text)
                        },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        cursorBrush = SolidColor(if (isDark) Color(0xFFE4EDE6) else Color(0xFF1B261E)),
                        visualTransformation = syntaxTransformation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("code_editor_text_field")
                    )
                }
            }
        }

        // Error message banner if there are errors
        if (syntaxErrors.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF5A1D1D))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${syntaxErrors.size} ${strings.syntaxErrorsFound} (Satır ${syntaxErrors.first().line}: ${syntaxErrors.first().message})",
                    color = Color.White,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun QuickSymbolToolbar(
    onInsert: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    isDark: Boolean
) {
    val symbols = listOf(
        "{", "}", "(", ")", "[", "]", "<", ">", "/", ";", "=", ":", "\"", "'", "$", "#", "    ", "->", "=>"
    )

    Surface(
        color = if (isDark) Color(0xFF252526) else Color(0xFFE5E5E5),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().height(42.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onUndo,
                modifier = Modifier.size(34.dp).testTag("quick_undo_button")
            ) {
                Icon(Icons.Default.Undo, contentDescription = "Undo", modifier = Modifier.size(18.dp))
            }

            IconButton(
                onClick = onRedo,
                modifier = Modifier.size(34.dp).testTag("quick_redo_button")
            ) {
                Icon(Icons.Default.Redo, contentDescription = "Redo", modifier = Modifier.size(18.dp))
            }

            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(symbols) { sym ->
                    val display = if (sym == "    ") "Tab" else sym
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) Color(0xFF333333) else Color(0xFFFFFFFF))
                            .clickable { onInsert(sym) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("symbol_button_$display"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = display,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyEditorState(
    strings: EditorStrings,
    onNewFile: () -> Unit,
    onOpenFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF333333)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "</>",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = strings.noOpenFiles,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = strings.noOpenFilesSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNewFile,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("empty_state_new_file_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.quickStartNew)
                    }

                    OutlinedButton(
                        onClick = onOpenFile,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("empty_state_open_file_button")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.quickStartOpen)
                    }
                }
            }
        }
    }
}
