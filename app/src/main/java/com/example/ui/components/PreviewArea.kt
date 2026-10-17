package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.localization.EditorStrings
import com.example.model.CodeFile

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PreviewArea(
    activeFile: CodeFile?,
    allFiles: List<CodeFile>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    strings: EditorStrings,
    modifier: Modifier = Modifier
) {
    var webViewKey by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Mode Selector: Web vs Android UI
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { onTabSelected(0) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.webPreview, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.testTag("tab_web_preview")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { onTabSelected(1) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.androidPreview, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.testTag("tab_android_preview")
            )
        }

        if (selectedTabIndex == 0) {
            // Web Preview
            val htmlContent = remember(activeFile, allFiles, webViewKey) {
                resolveHtmlContent(activeFile, allFiles)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            settings.allowContentAccess = true
                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()
                            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        }
                    },
                    update = { view ->
                        view.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier.fillMaxSize().testTag("web_preview_view")
                )

                // Refresh Floating Action
                IconButton(
                    onClick = { webViewKey++ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .size(44.dp)
                        .testTag("refresh_web_preview_button")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            }
        } else {
            // Android UI Simulation
            AndroidUiSimulationArea(activeFile = activeFile, strings = strings)
        }
    }
}

@Composable
fun AndroidUiSimulationArea(
    activeFile: CodeFile?,
    strings: EditorStrings
) {
    var sampleCounter by remember { mutableIntStateOf(0) }
    var sampleText by remember { mutableStateOf("Helfrex Mobile App") }
    var sampleSwitch by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Device Mockup Container
        Card(
            modifier = Modifier
                .width(320.dp)
                .fillMaxSize()
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Device Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("12:00", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("5G  100%", fontSize = 11.sp)
                }

                // App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Android, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        activeFile?.name ?: "Android Preview",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Body content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Active Code: ${activeFile?.fullName ?: "None"}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Lines: ${activeFile?.content?.lines()?.size ?: 0} | Extension: .${activeFile?.extension ?: ""}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = sampleText,
                        onValueChange = { sampleText = it },
                        label = { Text("Input Field Simulation") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active State Feature", fontSize = 13.sp)
                        Switch(checked = sampleSwitch, onCheckedChange = { sampleSwitch = it })
                    }

                    Button(
                        onClick = { sampleCounter++ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Interactive Tap Counter: $sampleCounter")
                    }

                    Text(
                        "Code snippet preview:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF14171F))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = activeFile?.content?.take(300) ?: "// No code available",
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

private fun resolveHtmlContent(activeFile: CodeFile?, allFiles: List<CodeFile>): String {
    if (activeFile != null && (activeFile.extension.equals("html", true) || activeFile.extension.equals("htm", true))) {
        var content = activeFile.content

        // Inject any CSS files from project
        val cssFiles = allFiles.filter { it.extension.equals("css", true) }
        if (cssFiles.isNotEmpty()) {
            val combinedCss = cssFiles.joinToString("\n") { it.content }
            content = content.replace("</head>", "<style>$combinedCss</style></head>")
        }

        // Inject any JS files from project
        val jsFiles = allFiles.filter { it.extension.equals("js", true) }
        if (jsFiles.isNotEmpty()) {
            val combinedJs = jsFiles.joinToString("\n") { it.content }
            content = content.replace("</body>", "<script>$combinedJs</script></body>")
        }

        return content
    }

    // If CSS is active
    if (activeFile != null && activeFile.extension.equals("css", true)) {
        return """<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>${activeFile.content}</style>
</head>
<body>
  <div style="padding: 20px;">
    <h1>CSS Preview</h1>
    <p>Viewing styled output for ${activeFile.fullName}</p>
    <button>Sample Button</button>
  </div>
</body>
</html>"""
    }

    // If JS is active
    if (activeFile != null && activeFile.extension.equals("js", true)) {
        return """<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    body { font-family: sans-serif; background: #0f172a; color: white; padding: 20px; }
    #log { background: #1e293b; padding: 15px; border-radius: 8px; font-family: monospace; }
  </style>
</head>
<body>
  <h2>JavaScript Console Output</h2>
  <div id="log"></div>
  <script>
    const logDiv = document.getElementById('log');
    console.log = function(...args) {
      logDiv.innerHTML += args.join(' ') + '<br>';
    };
    try {
      ${activeFile.content}
    } catch(err) {
      logDiv.innerHTML += '<span style="color:#ef4444">Error: ' + err.message + '</span>';
    }
  </script>
</body>
</html>"""
    }

    // Generic code or no file
    val anyHtml = allFiles.firstOrNull { it.extension.equals("html", true) }
    if (anyHtml != null) {
        return resolveHtmlContent(anyHtml, allFiles)
    }

    return """<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    body { font-family: system-ui, sans-serif; background: #0f172a; color: #94a3b8; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; text-align: center; }
    .card { background: #1e293b; padding: 30px; border-radius: 12px; }
  </style>
</head>
<body>
  <div class="card">
    <h2 style="color: #38bdf8;">Web Preview</h2>
    <p>HTML, CSS veya JS dosyası oluşturup canlı çıktısını burada görün.</p>
  </div>
</body>
</html>"""
}
