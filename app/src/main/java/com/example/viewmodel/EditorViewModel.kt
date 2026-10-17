package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppLanguage
import com.example.model.BuildLog
import com.example.model.BuildTarget
import com.example.model.CodeFile
import com.example.model.Language
import com.example.model.LogLevel
import com.example.model.SyntaxError
import com.example.model.ThemeMode
import com.example.syntax.SyntaxErrorDetector
import com.example.util.ArchiveUtils
import com.example.util.CompilerEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class ActiveDialog {
    NEW_FILE,
    NEW_FOLDER,
    FILES_MANAGER,
    SAVE_PROJECT,
    BUILD_PROJECT,
    SETTINGS
}

data class EditorUiState(
    val files: List<CodeFile> = emptyList(),
    val folders: List<String> = listOf("/", "/src", "/assets", "/scripts"),
    val activeFileId: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.TURKISH,
    val isPreviewMode: Boolean = false,
    val previewTypeIndex: Int = 0, // 0: Web, 1: Android UI
    val activeDialog: ActiveDialog? = null,
    val buildLogs: List<BuildLog> = emptyList(),
    val isBuilding: Boolean = false,
    val toastMessage: String? = null
)

class EditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    // Undo / Redo history for active file
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private var lastRecordedContent: String = ""

    private var buildJob: Job? = null

    // Get active file
    val activeFile: StateFlow<CodeFile?> = _uiState.combine(_uiState) { state, _ ->
        state.files.firstOrNull { it.id == state.activeFileId }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Current syntax errors of active file
    val activeSyntaxErrors: StateFlow<List<SyntaxError>> = activeFile.combine(_uiState) { file, _ ->
        if (file == null) emptyList()
        else {
            val lang = Language.fromExtension(file.extension)
            SyntaxErrorDetector.checkErrors(file.content, lang)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Tabs that are open
    val openTabs: StateFlow<List<CodeFile>> = _uiState.combine(_uiState) { state, _ ->
        state.files.filter { it.isOpen }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun openDialog(dialog: ActiveDialog) {
        _uiState.value = _uiState.value.copy(activeDialog = dialog)
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(activeDialog = null)
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun setAppLanguage(lang: AppLanguage) {
        _uiState.value = _uiState.value.copy(language = lang)
    }

    fun setPreviewMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isPreviewMode = enabled)
    }

    fun setPreviewTypeIndex(index: Int) {
        _uiState.value = _uiState.value.copy(previewTypeIndex = index)
    }

    fun getAllFolders(): List<String> {
        val custom = _uiState.value.folders
        val fromFiles = _uiState.value.files.map { it.path.ifEmpty { "/" } }
        return (custom + fromFiles).distinct().sorted()
    }

    fun createFolder(parentPath: String, folderName: String): Boolean {
        val cleanName = folderName.trim().removePrefix("/").removeSuffix("/")
        if (cleanName.isBlank()) {
            _uiState.value = _uiState.value.copy(toastMessage = "Klasör adı boş olamaz")
            return false
        }
        val cleanParent = parentPath.trim().removeSuffix("/").ifEmpty { "" }
        val fullPath = if (cleanParent.isEmpty()) "/$cleanName" else "$cleanParent/$cleanName"

        val allFolders = getAllFolders()
        if (allFolders.any { it.equals(fullPath, ignoreCase = true) }) {
            _uiState.value = _uiState.value.copy(toastMessage = "Bu dizinde aynı isimde klasör zaten var!")
            return false
        }

        val updatedFolders = (allFolders + fullPath).distinct()
        _uiState.value = _uiState.value.copy(
            folders = updatedFolders,
            activeDialog = null,
            toastMessage = "$cleanName klasörü oluşturuldu"
        )
        return true
    }

    fun moveFileToFolder(fileId: String, targetFolder: String): Boolean {
        val currentFiles = _uiState.value.files
        val targetFile = currentFiles.firstOrNull { it.id == fileId } ?: return false
        val cleanTarget = targetFolder.trim().ifEmpty { "/" }

        // Duplicate check in destination directory
        val duplicate = currentFiles.any {
            it.id != fileId &&
            it.path == cleanTarget &&
            it.name.equals(targetFile.name, ignoreCase = true) &&
            it.extension.equals(targetFile.extension, ignoreCase = true)
        }
        if (duplicate) {
            _uiState.value = _uiState.value.copy(toastMessage = "Bu dizinde aynı isim ve uzantıda dosya zaten var!")
            return false
        }

        val updated = currentFiles.map {
            if (it.id == fileId) it.copy(path = cleanTarget) else it
        }
        _uiState.value = _uiState.value.copy(
            files = updated,
            toastMessage = "${targetFile.fullName} -> $cleanTarget klasörüne taşındı"
        )
        return true
    }

    fun createFile(name: String, extension: String, path: String, initialContent: String = ""): Boolean {
        val cleanName = name.trim().removeSuffix(".$extension")
        val cleanExt = extension.trim().removePrefix(".").ifEmpty { "txt" }
        val cleanPath = path.trim().ifEmpty { "/src" }

        // Rule: In the same directory, no two files can have the same name and extension!
        val exists = _uiState.value.files.any {
            it.path == cleanPath &&
            it.name.equals(cleanName, ignoreCase = true) &&
            it.extension.equals(cleanExt, ignoreCase = true)
        }
        if (exists) {
            _uiState.value = _uiState.value.copy(toastMessage = "Bu dizinde aynı isim ve uzantıda dosya zaten var!")
            return false
        }

        val newId = UUID.randomUUID().toString()
        val sampleContent = if (initialContent.isNotBlank()) initialContent else getBoilerplateFor(cleanExt, cleanName)

        val newFile = CodeFile(
            id = newId,
            name = cleanName,
            extension = cleanExt,
            content = sampleContent,
            path = cleanPath,
            isOpen = true,
            isModified = false
        )

        val updatedFolders = (_uiState.value.folders + cleanPath).distinct()
        val updatedFiles = _uiState.value.files + newFile
        _uiState.value = _uiState.value.copy(
            files = updatedFiles,
            folders = updatedFolders,
            activeFileId = newId,
            activeDialog = null,
            toastMessage = "${newFile.fullName} oluşturuldu"
        )
        resetHistory(sampleContent)
        return true
    }

    fun addPickedFile(name: String, extension: String, content: String, path: String) {
        val newId = UUID.randomUUID().toString()
        val cleanName = name.substringBeforeLast(".")
        val cleanExt = if (extension.isNotBlank()) extension else name.substringAfterLast(".", "txt")

        val newFile = CodeFile(
            id = newId,
            name = cleanName,
            extension = cleanExt,
            content = content,
            path = path,
            isOpen = true,
            isModified = false
        )

        val updatedFiles = _uiState.value.files + newFile
        _uiState.value = _uiState.value.copy(
            files = updatedFiles,
            activeFileId = newId,
            activeDialog = null,
            toastMessage = "${newFile.fullName} açıldı"
        )
        resetHistory(content)
    }

    fun loadFolder(folderName: String, files: List<CodeFile>) {
        val current = _uiState.value.files.toMutableList()
        current.addAll(files)
        val firstOpened = files.firstOrNull()?.id ?: _uiState.value.activeFileId

        _uiState.value = _uiState.value.copy(
            files = current,
            activeFileId = firstOpened,
            activeDialog = null,
            toastMessage = "$folderName klasöründen ${files.size} dosya yüklendi"
        )
    }

    fun selectFile(fileId: String) {
        val file = _uiState.value.files.firstOrNull { it.id == fileId }
        if (file != null) {
            val updated = _uiState.value.files.map {
                if (it.id == fileId) it.copy(isOpen = true) else it
            }
            _uiState.value = _uiState.value.copy(files = updated, activeFileId = fileId)
            resetHistory(file.content)
        }
    }

    fun closeTab(fileId: String) {
        val current = _uiState.value.files
        val updated = current.map {
            if (it.id == fileId) it.copy(isOpen = false) else it
        }
        val remainingOpen = updated.filter { it.isOpen }
        val nextActive = if (_uiState.value.activeFileId == fileId) {
            remainingOpen.lastOrNull()?.id
        } else {
            _uiState.value.activeFileId
        }

        _uiState.value = _uiState.value.copy(files = updated, activeFileId = nextActive)
    }

    fun updateActiveFileContent(newContent: String) {
        val activeId = _uiState.value.activeFileId ?: return
        val currentFiles = _uiState.value.files
        val target = currentFiles.firstOrNull { it.id == activeId } ?: return

        if (target.content != newContent) {
            if (lastRecordedContent.isNotEmpty()) {
                undoStack.add(lastRecordedContent)
                redoStack.clear()
            }
            lastRecordedContent = newContent

            val updated = currentFiles.map {
                if (it.id == activeId) it.copy(content = newContent, isModified = true) else it
            }
            _uiState.value = _uiState.value.copy(files = updated)
        }
    }

    fun undo() {
        val activeId = _uiState.value.activeFileId ?: return
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(lastRecordedContent)
            lastRecordedContent = previous

            val updated = _uiState.value.files.map {
                if (it.id == activeId) it.copy(content = previous, isModified = true) else it
            }
            _uiState.value = _uiState.value.copy(files = updated)
        }
    }

    fun redo() {
        val activeId = _uiState.value.activeFileId ?: return
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(lastRecordedContent)
            lastRecordedContent = next

            val updated = _uiState.value.files.map {
                if (it.id == activeId) it.copy(content = next, isModified = true) else it
            }
            _uiState.value = _uiState.value.copy(files = updated)
        }
    }

    fun saveActiveFileDirectly(context: Context) {
        val activeId = _uiState.value.activeFileId ?: run {
            _uiState.value = _uiState.value.copy(toastMessage = "Kaydedilecek açık dosya yok")
            return
        }
        val currentFiles = _uiState.value.files
        val target = currentFiles.firstOrNull { it.id == activeId } ?: return

        try {
            val exportDir = ArchiveUtils.getDefaultExportDirectory(context)
            if (!exportDir.exists()) exportDir.mkdirs()
            val targetFile = File(exportDir, target.fullName)
            targetFile.writeText(target.content, Charsets.UTF_8)

            val updated = currentFiles.map {
                if (it.id == activeId) it.copy(isModified = false) else it
            }
            _uiState.value = _uiState.value.copy(
                files = updated,
                toastMessage = "${target.fullName} kaydedildi"
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                toastMessage = "Kaydetme hatası: ${e.message}"
            )
        }
    }

    fun moveOpenTab(fromIndex: Int, toIndex: Int) {
        val currentFiles = _uiState.value.files.toMutableList()
        val openIndices = currentFiles.mapIndexedNotNull { idx, file -> if (file.isOpen) idx else null }
        if (fromIndex in openIndices.indices && toIndex in openIndices.indices && fromIndex != toIndex) {
            val realFrom = openIndices[fromIndex]
            val realTo = openIndices[toIndex]
            val item = currentFiles.removeAt(realFrom)
            currentFiles.add(realTo, item)
            _uiState.value = _uiState.value.copy(files = currentFiles)
        }
    }

    fun applyFileVisibilities(visibilityMap: Map<String, Boolean>) {
        val updated = _uiState.value.files.map { file ->
            val isNowOpen = visibilityMap[file.id] ?: file.isOpen
            file.copy(isOpen = isNowOpen)
        }
        val openFiles = updated.filter { it.isOpen }
        val newActive = if (openFiles.none { it.id == _uiState.value.activeFileId }) {
            openFiles.firstOrNull()?.id
        } else {
            _uiState.value.activeFileId
        }

        _uiState.value = _uiState.value.copy(
            files = updated,
            activeFileId = newActive,
            activeDialog = null,
            toastMessage = "Dosya görünürlükleri güncellendi"
        )
    }

    fun saveProject(
        saveZip: Boolean,
        saveFolder: Boolean,
        name: String,
        targetDir: File
    ) {
        val allFiles = _uiState.value.files
        if (allFiles.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                activeDialog = null,
                toastMessage = "Kaydedilecek dosya bulunamadı"
            )
            return
        }

        val results = mutableListOf<String>()

        if (saveZip) {
            val (ok, res) = ArchiveUtils.saveAsZip(targetDir, name, allFiles)
            if (ok) results.add("ZIP: $res") else results.add("ZIP hatası: $res")
        }

        if (saveFolder) {
            val (ok, res) = ArchiveUtils.saveAsFolder(targetDir, name, allFiles)
            if (ok) results.add("Klasör: $res") else results.add("Klasör hatası: $res")
        }

        val markedSaved = allFiles.map { it.copy(isModified = false) }

        _uiState.value = _uiState.value.copy(
            files = markedSaved,
            activeDialog = null,
            toastMessage = results.joinToString(" | ")
        )
    }

    fun startCompilation(
        target: BuildTarget,
        outputName: String,
        targetDir: File
    ) {
        buildJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isBuilding = true,
            buildLogs = emptyList()
        )

        buildJob = viewModelScope.launch {
            CompilerEngine.executeBuild(
                target = target,
                outputName = outputName,
                destinationDir = targetDir,
                files = _uiState.value.files
            ).collect { log ->
                val currentLogs = _uiState.value.buildLogs + log
                val stillRunning = log.level != LogLevel.SUCCESS && log.level != LogLevel.ERROR
                _uiState.value = _uiState.value.copy(
                    buildLogs = currentLogs,
                    isBuilding = stillRunning,
                    toastMessage = if (log.level == LogLevel.SUCCESS) "Derleme başarıyla tamamlandı!" else null
                )
            }
        }
    }

    private fun resetHistory(content: String) {
        undoStack.clear()
        redoStack.clear()
        lastRecordedContent = content
    }

    private fun getBoilerplateFor(ext: String, name: String): String {
        return when (ext.lowercase()) {
            "html" -> """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>$name</title>
  <style>
    body {
      font-family: system-ui, sans-serif;
      background: #0f172a;
      color: #f8fafc;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      margin: 0;
    }
    .card {
      background: #1e293b;
      padding: 2rem;
      border-radius: 12px;
      box-shadow: 0 10px 25px rgba(0,0,0,0.5);
      text-align: center;
      border: 1px solid #334155;
    }
    button {
      background: #38bdf8;
      color: #0f172a;
      border: none;
      padding: 0.75rem 1.5rem;
      font-weight: bold;
      border-radius: 8px;
      cursor: pointer;
      margin-top: 1rem;
    }
  </style>
</head>
<body>
  <div class="card">
    <h1>$name</h1>
    <p>Created in Helfrex Code</p>
    <button onclick="alert('Hello from Helfrex Code!')">Click Me</button>
  </div>
</body>
</html>"""
            "css" -> """/* Style for $name */
:root {
  --primary-color: #38bdf8;
  --bg-color: #0f172a;
}

body {
  margin: 0;
  padding: 16px;
  background-color: var(--bg-color);
  color: #ffffff;
}"""
            "js" -> """// $name.js
function main() {
  console.log("Welcome to Helfrex Code!");
}

main();"""
            "py" -> """# $name.py
def main():
    print("Welcome to Helfrex Code!")

if __name__ == "__main__":
    main()"""
            "kt" -> """// $name.kt
package com.example

fun main() {
    println("Welcome to Helfrex Code!")
}"""
            "java" -> """// $name.java
public class $name {
    public static void main(String[] args) {
        System.out.println("Welcome to Helfrex Code!");
    }
}"""
            "json" -> """{
  "name": "$name",
  "version": "1.0.0",
  "description": "Created with Helfrex Code",
  "author": "Developer"
}"""
            else -> """// $name.$ext
"""
        }
    }
}
