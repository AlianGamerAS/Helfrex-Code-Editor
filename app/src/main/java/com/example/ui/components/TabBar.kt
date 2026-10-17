package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.CodeFile
import kotlin.math.roundToInt

@Composable
fun TabBar(
    openTabs: List<CodeFile>,
    activeFileId: String?,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTabClick: () -> Unit,
    onMoveTab: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(42.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(openTabs, key = { _, file -> file.id }) { index, file ->
                val isActive = file.id == activeFileId
                val bgColor = if (isActive) {
                    MaterialTheme.colorScheme.background
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
                val textColor = if (isActive) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                var dragOffsetX by remember { mutableFloatStateOf(0f) }
                val isDragging = dragOffsetX != 0f

                val hasDuplicateName = remember(openTabs, file.fullName) {
                    openTabs.count { it.fullName.equals(file.fullName, ignoreCase = true) } > 1
                }
                val tabDisplayTitle = remember(file.fullName, file.path, hasDuplicateName) {
                    if (hasDuplicateName) {
                        val folderName = file.path.trim('/').substringAfterLast('/')
                        val initial = if (folderName.isNotEmpty()) folderName.first().lowercaseChar() else 'r'
                        "($initial) ${file.fullName}"
                    } else {
                        file.fullName
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .zIndex(if (isDragging) 10f else 1f)
                        .offset { IntOffset(dragOffsetX.roundToInt(), 0) }
                        .then(
                            if (isDragging) Modifier.shadow(4.dp, RoundedCornerShape(4.dp)) else Modifier
                        )
                        .background(bgColor)
                        .pointerInput(file.id, openTabs.size) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { dragOffsetX = 0f },
                                onDragEnd = {
                                    val threshold = 60.dp.toPx()
                                    if (dragOffsetX > threshold && index < openTabs.size - 1) {
                                        onMoveTab(index, index + 1)
                                    } else if (dragOffsetX < -threshold && index > 0) {
                                        onMoveTab(index, index - 1)
                                    }
                                    dragOffsetX = 0f
                                },
                                onDragCancel = { dragOffsetX = 0f },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetX += dragAmount.x
                                }
                            )
                        }
                        .clickable { onSelectTab(file.id) }
                        .padding(horizontal = 12.dp)
                        .testTag("tab_${file.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dot indicator if modified
                    if (file.isModified) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = tabDisplayTitle,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable { onCloseTab(file.id) }
                            .testTag("close_tab_${file.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close ${file.fullName}",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onNewTabClick,
            modifier = Modifier
                .size(36.dp)
                .padding(end = 4.dp)
                .testTag("new_tab_shortcut_button")
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "New Tab",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
