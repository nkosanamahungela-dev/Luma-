package com.kaii.photos.compose.app_bars.album_view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kyant.backdrop.Backdrop
import com.kaii.photos.LocalNavController
import com.kaii.photos.PhotosApplication
import com.kaii.photos.R
import com.kaii.photos.compose.app_bars.SelectionGlassChip
import com.kaii.photos.compose.app_bars.SelectionGlassTagButton
import com.kaii.photos.compose.app_bars.main_bars.LiquidGlassIconButton
import com.kaii.photos.compose.app_bars.main_bars.LumaTopBar
import com.kaii.photos.compose.dialogs.user_action.AlbumPathsDialog
import com.kaii.photos.database.sync.CloudSyncWorker
import com.kaii.photos.datastore.AlbumType
import com.kaii.photos.helpers.Screens
import com.kaii.photos.helpers.grid_management.SelectionManager
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun SingleAlbumViewTopBar(
    albumInfo: () -> AlbumType,
    selectionManager: SelectionManager,
    showTagDialog: () -> Boolean,
    isMediaPicker: Boolean = false,
    showDialog: () -> Unit,
    setShowTagDialog: (show: Boolean) -> Unit,
    backdrop: Backdrop,
    topBarOffsetPx: () -> Float = { 0f }
) {
    val show by selectionManager.enabled.collectAsStateWithLifecycle(initialValue = false)
    val navController = LocalNavController.current

    LaunchedEffect(show) {
        if (!show) setShowTagDialog(false)
    }

    LumaTopBar(
        backdrop = backdrop,
        titleRowOffsetPx = topBarOffsetPx,
        trailing = {},
        leading = {
            AnimatedContent(
                targetState = show,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { 24 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -24 })
                    } else {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { 24 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -24 })
                    }
                },
                label = "folder title selection swap"
            ) { selecting ->
                if (!selecting) {
                    Text(
                        text = albumInfo().name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 72.dp, end = 8.dp)
                    )
                }
            }
        },
        floatingLeading = {
            AnimatedContent(
                targetState = show,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { 24 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -24 })
                    } else {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { 24 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -24 })
                    }
                },
                label = "folder back selection swap"
            ) { selecting ->
                if (selecting) {
                    SelectionGlassChip(backdrop, selectionManager)
                } else {
                    LiquidGlassIconButton(
                        backdrop = backdrop,
                        icon = painterResource(R.drawable.back_arrow),
                        contentDescription = stringResource(R.string.return_to_previous_page),
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        floatingTrailing = {
            AnimatedContent(
                targetState = show,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { 24 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -24 })
                    } else {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { 24 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -24 })
                    }
                },
                label = "folder actions selection swap"
            ) { selecting ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selecting) {
                        if (albumInfo() !is AlbumType.SAFFolder) {
                            SelectionGlassTagButton(
                                backdrop = backdrop,
                                showTagDialog = showTagDialog,
                                setShowTagDialog = setShowTagDialog,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    } else {
                        val info = albumInfo()
                        if (info.immichId != null && info !is AlbumType.Cloud) {
                            val context = LocalContext.current
                            val coroutineScope = rememberCoroutineScope()
                            var id by retain { mutableStateOf<UUID?>(null) }
                            var loading by retain { mutableStateOf(false) }

                            LaunchedEffect(id) {
                                if (id != null) {
                                    WorkManager.getInstance(context).getWorkInfoByIdFlow(id!!).collect {
                                        loading = it?.state != WorkInfo.State.SUCCEEDED
                                    }
                                }
                            }

                            LiquidGlassIconButton(
                                backdrop = backdrop,
                                icon = painterResource(R.drawable.cloud_sync),
                                contentDescription = stringResource(R.string.refresh),
                                onClick = {
                                    coroutineScope.launch {
                                        loading = true
                                        id = CloudSyncWorker.immediateEnqueue(context = context, albumId = albumInfo().id)
                                    }
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        if (!isMediaPicker && info is AlbumType.Folder && !info.wasCloud) {
                            var showPathsDialog by remember { mutableStateOf(false) }
                            if (showPathsDialog) {
                                AlbumPathsDialog(
                                    albumInfo = info,
                                    onConfirm = { selectedPaths ->
                                        val newInfo = info.copy(id = info.id, paths = selectedPaths)
                                        PhotosApplication.appModule.settings.albums.edit(
                                            id = info.id,
                                            newInfo = newInfo
                                        )
                                        navController.popBackStack()
                                        navController.navigate(route = Screens.Album.GridView(album = newInfo))
                                    },
                                    onDismiss = { showPathsDialog = false }
                                )
                            }
                            LiquidGlassIconButton(
                                backdrop = backdrop,
                                icon = painterResource(R.drawable.add),
                                contentDescription = "Add folder paths",
                                onClick = { showPathsDialog = true },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        LiquidGlassIconButton(
                            backdrop = backdrop,
                            icon = painterResource(R.drawable.settings),
                            contentDescription = "Album settings",
                            onClick = showDialog,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }
        }
    )
}
