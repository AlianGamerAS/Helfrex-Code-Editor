package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.EditorStrings

@Composable
fun AppDrawerContent(
    strings: EditorStrings,
    onNewFileClick: () -> Unit,
    onNewFolderClick: () -> Unit,
    onSelectFileClick: () -> Unit,
    onSelectFolderClick: () -> Unit,
    onFilesManagerClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBuildClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isNewMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section
            Column {
                // App Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp, top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF26332A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "</>",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Helfrex Studio",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // 1. "Yeni" + icon expandable menu
                DrawerExpandableItem(
                    title = strings.newAction,
                    icon = Icons.Default.Add,
                    isExpanded = isNewMenuExpanded,
                    onHeaderClick = { isNewMenuExpanded = !isNewMenuExpanded },
                    testTag = "drawer_new_menu_button"
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 4.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Option 1: Yeni Dosya Oluştur
                        DrawerSubItem(
                            title = strings.createNewFile,
                            icon = Icons.Default.InsertDriveFile,
                            onClick = onNewFileClick,
                            testTag = "drawer_create_new_file"
                        )
                        // Option 2: Yeni Klasör Oluştur
                        DrawerSubItem(
                            title = strings.createNewFolder,
                            icon = Icons.Default.CreateNewFolder,
                            onClick = onNewFolderClick,
                            testTag = "drawer_create_new_folder"
                        )
                        // Option 3: Dosya Seç
                        DrawerSubItem(
                            title = strings.selectFile,
                            icon = Icons.Default.FolderOpen,
                            onClick = onSelectFileClick,
                            testTag = "drawer_select_file"
                        )
                        // Option 4: Klasör Seç
                        DrawerSubItem(
                            title = strings.selectFolder,
                            icon = Icons.Default.CreateNewFolder,
                            onClick = onSelectFolderClick,
                            testTag = "drawer_select_folder"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 2. "Dosyalar"
                DrawerActionItem(
                    title = strings.files,
                    icon = Icons.Default.Source,
                    onClick = onFilesManagerClick,
                    testTag = "drawer_files_manager_button"
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 3. "Kaydet" (Zip ve Klasör)
                DrawerActionItem(
                    title = strings.save,
                    icon = Icons.Default.Save,
                    onClick = onSaveClick,
                    testTag = "drawer_save_project_button"
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 4. "Derle" (.apk ve .exe)
                DrawerActionItem(
                    title = strings.build,
                    icon = Icons.Default.Build,
                    onClick = onBuildClick,
                    testTag = "drawer_build_project_button"
                )
            }

            // Bottom Section: "Ayarlar" in bottom-left corner
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                DrawerActionItem(
                    title = strings.settings,
                    icon = Icons.Default.Settings,
                    onClick = onSettingsClick,
                    testTag = "drawer_settings_button"
                )
            }
        }
    }
}

@Composable
private fun DrawerActionItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DrawerExpandableItem(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onHeaderClick)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag(testTag),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            content()
        }
    }
}

@Composable
private fun DrawerSubItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
