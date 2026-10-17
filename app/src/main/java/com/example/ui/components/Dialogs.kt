package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.EditorStrings
import com.example.model.AppLanguage
import com.example.model.BuildLog
import com.example.model.BuildTarget
import com.example.model.CodeFile
import com.example.model.LogLevel
import com.example.model.ThemeMode
import com.example.util.ArchiveUtils
import java.io.File

@Composable
fun NewFileDialog(
    strings: EditorStrings,
    existingFiles: List<CodeFile>,
    availableFolders: List<String>,
    onDismiss: () -> Unit,
    onCreate: (name: String, ext: String, path: String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var fileExt by remember { mutableStateOf("html") }
    var filePath by remember { mutableStateOf("/src") }

    val commonExtensions = listOf("html", "css", "js", "ts", "py", "kt", "java", "json", "xml", "cpp")
    val projectFolders = remember(availableFolders) {
        (availableFolders + listOf("/src", "/assets", "/")).distinct()
    }

    val isDuplicate = remember(fileName, fileExt, filePath, existingFiles) {
        val cleanName = fileName.trim().removeSuffix(".$fileExt")
        val cleanExt = fileExt.trim().removePrefix(".")
        existingFiles.any {
            it.path == filePath &&
            it.name.equals(cleanName, ignoreCase = true) &&
            it.extension.equals(cleanExt, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.createNewFile, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text(strings.fileName) },
                    singleLine = true,
                    placeholder = { Text("index") },
                    isError = isDuplicate,
                    supportingText = if (isDuplicate) {
                        { Text(strings.fileAlreadyExists, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth().testTag("new_file_name_input")
                )

                OutlinedTextField(
                    value = fileExt,
                    onValueChange = { fileExt = it.removePrefix(".") },
                    label = { Text(strings.fileExtension) },
                    singleLine = true,
                    placeholder = { Text("html") },
                    modifier = Modifier.fillMaxWidth().testTag("new_file_ext_input")
                )

                Text("Hızlı Uzantı Seçimi:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(commonExtensions) { ext ->
                        FilterChip(
                            selected = fileExt.equals(ext, ignoreCase = true),
                            onClick = { fileExt = ext },
                            label = { Text(".$ext") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Text("Kayıt Klasörünü Seçin:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(projectFolders) { folder ->
                        FilterChip(
                            selected = filePath == folder,
                            onClick = { filePath = folder },
                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            label = { Text(folder) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotBlank() && !isDuplicate) {
                        onCreate(fileName.trim(), fileExt.trim(), filePath.trim())
                    }
                },
                enabled = fileName.isNotBlank() && !isDuplicate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.testTag("confirm_create_file_button")
            ) {
                Text(strings.create)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_create_file_button")) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun NewFolderDialog(
    strings: EditorStrings,
    existingFolders: List<String>,
    onDismiss: () -> Unit,
    onCreate: (parentPath: String, folderName: String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var parentPath by remember { mutableStateOf("/") }

    val candidateFolders = remember(existingFolders) {
        existingFolders.ifEmpty { listOf("/", "/src", "/assets") }
    }

    val isDuplicate = remember(folderName, parentPath, candidateFolders) {
        val cleanName = folderName.trim().removePrefix("/").removeSuffix("/")
        if (cleanName.isBlank()) false
        else {
            val cleanParent = parentPath.trim().removeSuffix("/").ifEmpty { "" }
            val fullPath = if (cleanParent.isEmpty()) "/$cleanName" else "$cleanParent/$cleanName"
            candidateFolders.any { it.equals(fullPath, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.createNewFolder, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text(strings.folderName) },
                    singleLine = true,
                    placeholder = { Text("components") },
                    isError = isDuplicate,
                    supportingText = if (isDuplicate) {
                        { Text(strings.folderAlreadyExists, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth().testTag("new_folder_name_input")
                )

                Text("Ana Dizini Seçin:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(candidateFolders) { folder ->
                        FilterChip(
                            selected = parentPath == folder,
                            onClick = { parentPath = folder },
                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            label = { Text(folder) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.isNotBlank() && !isDuplicate) {
                        onCreate(parentPath.trim(), folderName.trim())
                    }
                },
                enabled = folderName.isNotBlank() && !isDuplicate,
                modifier = Modifier.testTag("confirm_create_folder_button")
            ) {
                Text(strings.create)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_create_folder_button")) {
                Text(strings.cancel)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesManagerDialog(
    files: List<CodeFile>,
    folders: List<String>,
    strings: EditorStrings,
    onDismiss: () -> Unit,
    onApply: (Map<String, Boolean>) -> Unit,
    onMoveFile: (fileId: String, targetFolder: String) -> Unit
) {
    // Initial state: Reflect actual open/closed state of each file
    val visibilityMap = remember(files) {
        mutableStateMapOf<String, Boolean>().apply {
            files.forEach { file ->
                put(file.id, file.isOpen)
            }
        }
    }

    // State for target folder selection dropdown for moving
    var activeMoveFileId by remember { mutableStateOf<String?>(null) }

    // Combine distinct folder paths from defined folders and current files
    val allDirectories = remember(folders, files) {
        (folders + files.map { it.path.ifEmpty { "/" } }).distinct().sorted()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = strings.fileDirectory,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${files.size} dosya • ${allDirectories.size} dizin",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_files_manager")) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = strings.close,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        tonalElevation = 4.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val openCount = visibilityMap.values.count { it }
                            val closedCount = visibilityMap.values.count { !it }

                            Text(
                                text = "$openCount açık • $closedCount kapalı",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.testTag("cancel_files_manager_button")
                                ) {
                                    Text(strings.cancel)
                                }

                                Button(
                                    onClick = {
                                        onApply(visibilityMap.toMap())
                                    },
                                    modifier = Modifier.testTag("apply_files_visibility_button")
                                ) {
                                    Text(strings.apply)
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                if (files.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            strings.noOpenFiles,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Group files by their directory
                        allDirectories.forEach { folderPath ->
                            val folderFiles = files.filter { (it.path.ifEmpty { "/" }) == folderPath }

                            item(key = "folder_$folderPath") {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        // Folder Header - NO eye icon on folders as requested
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = folderPath,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${folderFiles.size} dosya)",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (folderFiles.isEmpty()) {
                                            Text(
                                                text = "(Bu klasörde dosya yok)",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.padding(start = 28.dp, top = 6.dp)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Files within this folder
                                            folderFiles.forEach { file ->
                                                val isOpen = visibilityMap[file.id] ?: false

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(
                                                            if (isOpen) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                            else Color.Transparent
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    // File Info
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Code,
                                                            contentDescription = null,
                                                            tint = if (isOpen) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = file.fullName,
                                                            fontSize = 13.sp,
                                                            fontWeight = if (isOpen) FontWeight.SemiBold else FontWeight.Normal,
                                                            color = if (isOpen) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }

                                                    // Actions: Move to folder + Eye Visibility Toggle
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        // Move To Folder Button
                                                        Box {
                                                            IconButton(
                                                                onClick = { activeMoveFileId = file.id },
                                                                modifier = Modifier.size(32.dp).testTag("move_file_${file.id}")
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.DriveFileMove,
                                                                    contentDescription = strings.moveToFolder,
                                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }

                                                            DropdownMenu(
                                                                expanded = activeMoveFileId == file.id,
                                                                onDismissRequest = { activeMoveFileId = null }
                                                            ) {
                                                                Text(
                                                                    text = "Hedef Klasör:",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                                )
                                                                allDirectories.forEach { targetFolder ->
                                                                    if (targetFolder != folderPath) {
                                                                        DropdownMenuItem(
                                                                            text = { Text(targetFolder, fontSize = 13.sp) },
                                                                            leadingIcon = {
                                                                                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                                                                            },
                                                                            onClick = {
                                                                                activeMoveFileId = null
                                                                                onMoveFile(file.id, targetFolder)
                                                                            }
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        // Eye Icon ONLY on files (Visibility / VisibilityOff)
                                                        IconButton(
                                                            onClick = {
                                                                visibilityMap[file.id] = !isOpen
                                                            },
                                                            modifier = Modifier.size(36.dp).testTag("toggle_visibility_${file.id}")
                                                        ) {
                                                            if (isOpen) {
                                                                // Open eye (Visible in tabs)
                                                                Icon(
                                                                    Icons.Default.Visibility,
                                                                    contentDescription = strings.eyeOpenHint,
                                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                            } else {
                                                                // Crossed eye (Hidden from tabs)
                                                                Icon(
                                                                    Icons.Default.VisibilityOff,
                                                                    contentDescription = strings.eyeClosedHint,
                                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaveProjectDialog(
    strings: EditorStrings,
    filesCount: Int,
    onDismiss: () -> Unit,
    onSave: (saveZip: Boolean, saveFolder: Boolean, name: String, targetDir: File) -> Unit
) {
    val context = LocalContext.current
    var saveZip by remember { mutableStateOf(true) }
    var saveFolder by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("ProjectExport") }
    val defaultDir = remember { ArchiveUtils.getDefaultExportDirectory(context) }
    var exportPath by remember { mutableStateOf(defaultDir.absolutePath) }

    val projectFolders = remember {
        listOf(
            defaultDir.absolutePath,
            "${context.filesDir.absolutePath}/Projects",
            context.cacheDir.absolutePath
        )
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            exportPath = uri.path ?: defaultDir.absolutePath
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(strings.save, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Toplam $filesCount dosya (açık ve kapalı tümü) seçilen biçimde kaydedilecek.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Checkboxes: Zip and Klasör (Folder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = saveZip,
                            onCheckedChange = { saveZip = it },
                            modifier = Modifier.testTag("checkbox_save_zip")
                        )
                        Text(strings.saveAsZip, fontWeight = FontWeight.Medium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = saveFolder,
                            onCheckedChange = { saveFolder = it },
                            modifier = Modifier.testTag("checkbox_save_folder")
                        )
                        Text(strings.saveAsFolder, fontWeight = FontWeight.Medium)
                    }
                }

                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Proje / Arşiv Adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("save_project_name_input")
                )

                Text("Kayıt Klasörü:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(projectFolders) { folder ->
                        val shortName = folder.substringAfterLast("/")
                        FilterChip(
                            selected = exportPath == folder,
                            onClick = { exportPath = folder },
                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            label = { Text(shortName.ifEmpty { "/" }) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = { folderPicker.launch(null) },
                    modifier = Modifier.fillMaxWidth().testTag("browse_save_folder_button")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cihazdan Klasör Seç: ${exportPath.substringAfterLast("/")}", fontSize = 12.sp, maxLines = 1)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (saveZip || saveFolder) {
                        val targetDir = File(exportPath.trim())
                        onSave(saveZip, saveFolder, projectName.trim(), targetDir)
                    }
                },
                enabled = (saveZip || saveFolder) && projectName.isNotBlank(),
                modifier = Modifier.testTag("confirm_save_project_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_save_project_button")) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun BuildProjectDialog(
    strings: EditorStrings,
    logs: List<BuildLog>,
    isBuilding: Boolean,
    onDismiss: () -> Unit,
    onStartBuild: (target: BuildTarget, outputName: String, targetDir: File) -> Unit
) {
    val context = LocalContext.current
    var selectedTarget by remember { mutableStateOf(BuildTarget.APK) }
    var outputName by remember { mutableStateOf("HelfrexApp") }
    val defaultDir = remember { ArchiveUtils.getDefaultExportDirectory(context) }
    var outputPath by remember { mutableStateOf(defaultDir.absolutePath) }

    AlertDialog(
        onDismissRequest = { if (!isBuilding) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.build, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    strings.selectTargetPlatform,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { if (!isBuilding) selectedTarget = BuildTarget.APK }
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = selectedTarget == BuildTarget.APK,
                            onClick = { if (!isBuilding) selectedTarget = BuildTarget.APK },
                            modifier = Modifier.testTag("radio_build_apk")
                        )
                        Text(".apk", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { if (!isBuilding) selectedTarget = BuildTarget.EXE }
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = selectedTarget == BuildTarget.EXE,
                            onClick = { if (!isBuilding) selectedTarget = BuildTarget.EXE },
                            modifier = Modifier.testTag("radio_build_exe")
                        )
                        Text(".exe", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = outputName,
                    onValueChange = { outputName = it },
                    label = { Text(strings.outputName) },
                    enabled = !isBuilding,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("build_output_name_input")
                )

                OutlinedTextField(
                    value = outputPath,
                    onValueChange = { outputPath = it },
                    label = { Text(strings.outputPath) },
                    enabled = !isBuilding,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("build_output_path_input")
                )

                // Build Log Console
                Text(
                    strings.buildLogs,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    if (logs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.Center) {
                            Text(
                                "Derlemeyi başlatmak için butona tıklayın.",
                                fontSize = 11.sp,
                                color = Color(0xFF9E9E9E),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(logs) { log ->
                                val color = when (log.level) {
                                    LogLevel.INFO -> Color(0xFFE2E8F0)
                                    LogLevel.SUCCESS -> Color(0xFF00FF66)
                                    LogLevel.WARNING -> Color(0xFFFFB300)
                                    LogLevel.ERROR -> Color(0xFFFF4444)
                                }
                                Text(
                                    log.message,
                                    color = color,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetDir = File(outputPath.trim())
                    onStartBuild(selectedTarget, outputName.trim(), targetDir)
                },
                enabled = !isBuilding && outputName.isNotBlank(),
                modifier = Modifier.testTag("start_build_button")
            ) {
                if (isBuilding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.building)
                } else {
                    Text(strings.startBuild)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isBuilding,
                modifier = Modifier.testTag("close_build_button")
            ) {
                Text(strings.close)
            }
        }
    )
}

@Composable
fun SettingsDialog(
    strings: EditorStrings,
    currentTheme: ThemeMode,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(strings.settings, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 1. Sistem Teması
                Text(
                    strings.systemTheme,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onThemeSelected(ThemeMode.SYSTEM) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = currentTheme == ThemeMode.SYSTEM,
                            onClick = { onThemeSelected(ThemeMode.SYSTEM) },
                            modifier = Modifier.testTag("theme_radio_system")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.systemRecommended, fontSize = 14.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onThemeSelected(ThemeMode.DARK) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = currentTheme == ThemeMode.DARK,
                            onClick = { onThemeSelected(ThemeMode.DARK) },
                            modifier = Modifier.testTag("theme_radio_dark")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.darkMode, fontSize = 14.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onThemeSelected(ThemeMode.LIGHT) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = currentTheme == ThemeMode.LIGHT,
                            onClick = { onThemeSelected(ThemeMode.LIGHT) },
                            modifier = Modifier.testTag("theme_radio_light")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.lightTheme, fontSize = 14.sp)
                    }
                }

                // 2. Dili Değiştir
                Text(
                    strings.changeLanguage,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val languages = listOf(
                    Pair(AppLanguage.AZERBAIJANI, "Azərbaycanca"),
                    Pair(AppLanguage.TURKISH, "Türkçe"),
                    Pair(AppLanguage.RUSSIAN, "Русский"),
                    Pair(AppLanguage.ENGLISH, "English")
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    languages.forEach { (lang, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onLanguageSelected(lang) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = currentLanguage == lang,
                                onClick = { onLanguageSelected(lang) },
                                modifier = Modifier.testTag("lang_radio_${lang.name.lowercase()}")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss, 
                modifier = Modifier.testTag("close_settings_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(strings.close)
            }
        }
    )
}
