package com.kaii.photos.compose.pages.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.kaii.photos.compose.ViewProperties
import com.kaii.photos.compose.grids.media.PhotoGrid
import com.kaii.photos.datastore.AlbumType
import com.kaii.photos.helpers.grid_management.SelectionManager
import com.kaii.photos.models.SearchViewModel

@Composable
fun SearchPage(
    viewModel: SearchViewModel,
    selectionManager: SelectionManager,
    isMediaPicker: Boolean,
    isMultiSelectMode: Boolean
) {
    val gridState = rememberLazyGridState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 0.dp)
        ) {
            val items = viewModel.gridMediaFlow.collectAsLazyPagingItems()

            val columnSize by viewModel.columnSize.collectAsStateWithLifecycle()
            val openVideosExternally by viewModel.openVideosExternally.collectAsStateWithLifecycle()
            val cacheThumbnails by viewModel.cacheThumbnails.collectAsStateWithLifecycle()
            val thumbnailSize by viewModel.thumbnailSize.collectAsStateWithLifecycle()
            val useRoundedCorners by viewModel.useRoundedCorners.collectAsStateWithLifecycle()
            val vibrateOnClick by viewModel.vibrateOnClick.collectAsStateWithLifecycle()

            PhotoGrid(
                pagingItems = items,
                album = { AlbumType.PlaceHolder },
                selectionManager = selectionManager,
                viewProperties = ViewProperties.SearchNotFound,
                state = gridState,
                isMainPage = true,
                isMediaPicker = isMediaPicker,
                isMultiSelectMode = isMultiSelectMode,
                columnSize = { columnSize },
                openVideosExternally = { openVideosExternally },
                cacheThumbnails = { cacheThumbnails },
                thumbnailSize = { thumbnailSize },
                useRoundedCorners = { useRoundedCorners },
                vibrateOnClick = { vibrateOnClick },
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }

    }
}
