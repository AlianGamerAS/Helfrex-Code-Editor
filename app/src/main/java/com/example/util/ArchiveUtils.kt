package com.example.util

import android.content.Context
import com.example.model.CodeFile
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ArchiveUtils {

    fun getDefaultExportDirectory(context: Context): File {
        val external = context.getExternalFilesDir(null)
        val exportDir = File(external ?: context.filesDir, "HelfrexProjects")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        return exportDir
    }

    fun saveAsZip(
        exportDir: File,
        zipName: String,
        files: List<CodeFile>
    ): Pair<Boolean, String> {
        return try {
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            val cleanName = if (zipName.endsWith(".zip", ignoreCase = true)) zipName else "$zipName.zip"
            val zipFile = File(exportDir, cleanName)

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                for (file in files) {
                    val entryName = file.fullName
                    val entry = ZipEntry(entryName)
                    zos.putNextEntry(entry)
                    zos.write(file.content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
            Pair(true, zipFile.absolutePath)
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "ZIP oluşturma hatası")
        }
    }

    fun saveAsFolder(
        exportDir: File,
        folderName: String,
        files: List<CodeFile>
    ): Pair<Boolean, String> {
        return try {
            val targetFolder = File(exportDir, folderName)
            if (!targetFolder.exists()) {
                targetFolder.mkdirs()
            }

            for (file in files) {
                val outputFile = File(targetFolder, file.fullName)
                outputFile.writeText(file.content, Charsets.UTF_8)
            }
            Pair(true, targetFolder.absolutePath)
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Klasör kaydetme hatası")
        }
    }
}
