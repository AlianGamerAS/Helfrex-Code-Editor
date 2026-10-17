package com.example

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.provider.DocumentsContract
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.localization.StringsProvider
import com.example.model.CodeFile
import com.example.model.ThemeMode
import com.example.viewmodel.ActiveDialog
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.BuildProjectDialog
import com.example.ui.components.CodeEditorArea
import com.example.ui.components.FilesManagerDialog
import com.example.ui.components.NewFileDialog
import com.example.ui.components.NewFolderDialog
import com.example.ui.components.PreviewArea
import com.example.ui.components.SaveProjectDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TabBar
import com.example.ui.theme.HelfrexCodeTheme
import com.example.viewmodel.EditorViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelfrexApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelfrexApp(viewModel: EditorViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val activeFile by viewModel.activeFile.collectAsState()
    val activeSyntaxErrors by viewModel.activeSyntaxErrors.collectAsState()
    val openTabs by viewModel.openTabs.collectAsState()

    val strings = remember(uiState.language) {
        StringsProvider.get(uiState.language)
    }

    val systemDark = isSystemInDarkTheme()
    val isDark = when (uiState.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Hide keyboard when left drawer opens
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            keyboardController?.hide()
        }
    }

    // Toast feedback from viewmodel
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Android SAF File Picker for "Dosya Seç"
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var fileName = "opened_file.txt"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex)
                    }
                }

                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).readText()
                } ?: ""

                val ext = fileName.substringAfterLast(".", "txt")
                viewModel.addPickedFile(fileName, ext, content, uri.path ?: "/")
            } catch (e: Exception) {
                Toast.makeText(context, "Dosya okunamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Android SAF Folder Picker for "Klasör Seç"
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        if (treeUri != null) {
            try {
                val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocId)
                val folderName = treeDocId.substringAfterLast(":", "Project")
                val files = mutableListOf<CodeFile>()

                context.contentResolver.query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE
                    ),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

                    while (cursor.moveToNext()) {
                        val docId = cursor.getString(idIdx)
                        val name = cursor.getString(nameIdx) ?: continue
                        val mime = cursor.getString(mimeIdx) ?: ""

                        if (mime != DocumentsContract.Document.MIME_TYPE_DIR) {
                            val ext = name.substringAfterLast(".", "txt")
                            val fileDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                            try {
                                val content = context.contentResolver.openInputStream(fileDocUri)?.use { stream ->
                                    BufferedReader(InputStreamReader(stream)).readText()
                                } ?: ""
                                files.add(
                                    CodeFile(
                                        id = UUID.randomUUID().toString(),
                                        name = name.substringBeforeLast("."),
                                        extension = ext,
                                        content = content,
                                        path = "/$folderName",
                                        isOpen = files.isEmpty()
                                    )
                                )
                            } catch (_: Exception) {}
                        }
                    }
                }

                if (files.isNotEmpty()) {
                    viewModel.loadFolder(folderName, files)
                } else {
                    Toast.makeText(context, "Klasörde okunabilir dosya bulunamadı", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Klasör açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    HelfrexCodeTheme(themeMode = uiState.themeMode) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    strings = strings,
                    onNewFileClick = {
                        keyboardController?.hide()
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openDialog(ActiveDialog.NEW_FILE)
                    },
                    onNewFolderClick = {
                        keyboardController?.hide()
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openDialog(ActiveDialog.NEW_FOLDER)
                    },
                    onSelectFileClick = {
                        keyboardController?.hide()
                        coroutineScope.launch { drawerState.close() }
                        filePickerLauncher.launch(arrayOf("*/*"))
                    },
                    onSelectFolderClick = {
                        keyboardController?.hide()
                        coroutineScope.launch { drawerState.close() }
                        folderPickerLauncher.launch(null)
                    },
                    onFilesManagerClick = {
                        keyboardController?.hide()
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openDialog(ActiveDialog.FILES_MANAGER)
                    },
                    onSaveClick = {
                        keyboardController?.hide()
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openDialog(ActiveDialog.SAVE_PROJECT)
                    },
                    onBuildClick = {
                        keyboardController?.hide()
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openDialog(ActiveDialog.BUILD_PROJECT)
                    },
                    onSettingsClick = {
                        keyboardController?.hide()
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openDialog(ActiveDialog.SETTINGS)
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF333333)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "</>",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = strings.appName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        navigationIcon = {
                            // Three-lines menu button
                            IconButton(
                                onClick = {
                                    keyboardController?.hide()
                                    coroutineScope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                },
                                modifier = Modifier.testTag("menu_drawer_button")
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = strings.menu,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        actions = {
                            // Undo (Geri Al)
                            IconButton(
                                onClick = { viewModel.undo() },
                                modifier = Modifier.testTag("top_bar_undo")
                            ) {
                                Icon(
                                    Icons.Default.Undo,
                                    contentDescription = "Geri Al",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Redo (İleri Al)
                            IconButton(
                                onClick = { viewModel.redo() },
                                modifier = Modifier.testTag("top_bar_redo")
                            ) {
                                Icon(
                                    Icons.Default.Redo,
                                    contentDescription = "İleri Al",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Quick Save: Saves ONLY the currently active open file directly without asking for path
                            IconButton(
                                onClick = {
                                    viewModel.saveActiveFileDirectly(context)
                                },
                                modifier = Modifier.testTag("top_bar_quick_save")
                            ) {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = strings.save,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Open Tabs bar with drag-to-reorder support
                    if (openTabs.isNotEmpty()) {
                        TabBar(
                            openTabs = openTabs,
                            activeFileId = uiState.activeFileId,
                            onSelectTab = { viewModel.selectFile(it) },
                            onCloseTab = { viewModel.closeTab(it) },
                            onNewTabClick = { viewModel.openDialog(ActiveDialog.NEW_FILE) },
                            onMoveTab = { from, to -> viewModel.moveOpenTab(from, to) }
                        )
                    }

                    // Pure Code Editor Workspace
                    CodeEditorArea(
                        activeFile = activeFile,
                        syntaxErrors = activeSyntaxErrors,
                        strings = strings,
                        isDark = isDark,
                        onContentChange = { viewModel.updateActiveFileContent(it) },
                        onQuickNewFile = { viewModel.openDialog(ActiveDialog.NEW_FILE) },
                        onQuickOpenFile = { filePickerLauncher.launch(arrayOf("*/*")) }
                    )
                }
            }
        }

        // Active Dialogs / Modals
        when (uiState.activeDialog) {
            ActiveDialog.NEW_FILE -> {
                NewFileDialog(
                    strings = strings,
                    existingFiles = uiState.files,
                    availableFolders = viewModel.getAllFolders(),
                    onDismiss = { viewModel.closeDialog() },
                    onCreate = { name, ext, path ->
                        viewModel.createFile(name, ext, path)
                    }
                )
            }
            ActiveDialog.NEW_FOLDER -> {
                NewFolderDialog(
                    strings = strings,
                    existingFolders = viewModel.getAllFolders(),
                    onDismiss = { viewModel.closeDialog() },
                    onCreate = { parentPath, folderName ->
                        viewModel.createFolder(parentPath, folderName)
                    }
                )
            }
            ActiveDialog.FILES_MANAGER -> {
                FilesManagerDialog(
                    files = uiState.files,
                    folders = viewModel.getAllFolders(),
                    strings = strings,
                    onDismiss = { viewModel.closeDialog() },
                    onApply = { visibilityMap ->
                        viewModel.applyFileVisibilities(visibilityMap)
                    },
                    onMoveFile = { fileId, targetFolder ->
                        viewModel.moveFileToFolder(fileId, targetFolder)
                    }
                )
            }
            ActiveDialog.SAVE_PROJECT -> {
                SaveProjectDialog(
                    strings = strings,
                    filesCount = uiState.files.size,
                    onDismiss = { viewModel.closeDialog() },
                    onSave = { saveZip, saveFolder, name, targetDir ->
                        viewModel.saveProject(saveZip, saveFolder, name, targetDir)
                    }
                )
            }
            ActiveDialog.BUILD_PROJECT -> {
                BuildProjectDialog(
                    strings = strings,
                    logs = uiState.buildLogs,
                    isBuilding = uiState.isBuilding,
                    onDismiss = { viewModel.closeDialog() },
                    onStartBuild = { target, outputName, targetDir ->
                        viewModel.startCompilation(target, outputName, targetDir)
                    }
                )
            }
            ActiveDialog.SETTINGS -> {
                SettingsDialog(
                    strings = strings,
                    currentTheme = uiState.themeMode,
                    currentLanguage = uiState.language,
                    onDismiss = { viewModel.closeDialog() },
                    onThemeSelected = { viewModel.setThemeMode(it) },
                    onLanguageSelected = { viewModel.setAppLanguage(it) }
                )
            }
            null -> {}
        }
    }
}
