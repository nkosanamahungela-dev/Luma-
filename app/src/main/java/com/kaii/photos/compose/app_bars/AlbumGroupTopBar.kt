package com.kaii.photos.compose.app_bars

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.kyant.backdrop.Backdrop
import com.kaii.photos.compose.app_bars.main_bars.LiquidGlassIconButton
import com.kaii.photos.compose.app_bars.main_bars.LumaTopBar
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kaii.photos.R
import com.kaii.photos.compose.widgets.popup_album_chooser.PopUpAlbumChooser
import com.kaii.photos.datastore.AlbumGroup
import com.kaii.photos.helpers.TextStylingConstants
import com.kaii.photos.widgets.popup_chooser_state.AlbumGroupPopupChooserState
import com.kaii.photos.widgets.popup_chooser_state.rememberAlbumGroupPopUpAlbumChooserState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumGroupTopBar(
    group: () -> AlbumGroup?,
    groups: () -> List<AlbumGroup>,
    navController: NavController,
    showInfoDialog: () -> Unit,
    setAlbums: (ids: List<String>) -> Unit,
    backdrop: Backdrop,
    topBarOffsetPx: () -> Float = { 0f }
) {
    val coroutineScope = rememberCoroutineScope()
    var showAddAlbumsDialog by remember { mutableStateOf(false) }

    val popupChooserState = rememberAlbumGroupPopUpAlbumChooserState { album, query, filter ->
        val nameMatch = album.name.contains(query, true)

        val filterMatch = when (filter) {
            AlbumGroupPopupChooserState.Filters.InThisGroup -> group()?.albumIds?.contains(album.id) == true
            AlbumGroupPopupChooserState.Filters.NotInAGroup -> groups().none { it.albumIds.contains(album.id) }
            AlbumGroupPopupChooserState.Filters.InOtherGroups -> groups().any { it.albumIds.contains(album.id) && it.id != group()?.id }
            AlbumGroupPopupChooserState.Filters.None -> true
        }

        nameMatch && filterMatch
    }

    if (showAddAlbumsDialog) {
        val selectedAlbums by popupChooserState.selectedAlbums.collectAsStateWithLifecycle()
        LaunchedEffect(group()) {
            if (group()?.albumIds != null) popupChooserState.addAll(group()!!.albumIds)
        }

        PopUpAlbumChooser(
            state = popupChooserState,
            key = {
                it.id
            },
            extraContent = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            state = rememberScrollState()
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.Start
                    )
                ) {
                    val activeFilter by popupChooserState.filter.collectAsStateWithLifecycle()

                    AlbumGroupPopupChooserState.Filters.entries.forEach { filter ->
                        ElevatedFilterChip(
                            selected = activeFilter == filter,
                            onClick = {
                                popupChooserState.applyFilter(filter)
                            },
                            label = {
                                Text(
                                    text = stringResource(id = filter.label),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            shapes = FilterChipDefaults.shapes()
                        )
                    }
                }
            },
            onDismiss = {
                setAlbums(selectedAlbums)
                showAddAlbumsDialog = false
            }
        )
    }

    LumaTopBar(
        backdrop = backdrop,
        titleRowOffsetPx = topBarOffsetPx,
        trailing = {},
        leading = {
            Text(
                text = group()?.name ?: stringResource(id = R.string.album_group),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 72.dp, end = 8.dp)
            )
        },
        floatingLeading = {
            LiquidGlassIconButton(
                backdrop = backdrop,
                icon = painterResource(id = R.drawable.back_arrow),
                contentDescription = "Go back to previous page",
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        floatingTrailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiquidGlassIconButton(
                    backdrop = backdrop,
                    icon = painterResource(id = R.drawable.add),
                    contentDescription = stringResource(id = R.string.album_group_show_add_dialog),
                    onClick = {
                        coroutineScope.launch { showAddAlbumsDialog = true }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
                LiquidGlassIconButton(
                    backdrop = backdrop,
                    icon = painterResource(id = R.drawable.settings),
                    contentDescription = stringResource(id = R.string.album_group_info),
                    onClick = showInfoDialog,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    )}