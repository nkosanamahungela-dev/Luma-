package com.kaii.photos.compose.grids.albums

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kaii.photos.LocalNavController
import com.kaii.photos.compose.ViewProperties
import com.kaii.photos.compose.app_bars.SelectionActionBar
import com.kaii.photos.compose.app_bars.album_view.SingleAlbumViewTopBar
import com.kaii.photos.compose.app_bars.main_bars.lumaTopBarHeight
import com.kaii.photos.compose.app_bars.main_bars.rememberLumaTopBarScrollState
import com.kaii.photos.compose.dialogs.album_info.AlbumInfoDialog
import com.kaii.photos.compose.grids.media.PhotoGrid
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kaii.photos.compose.side_effects.FileOperationProgressEffect
import com.kaii.photos.compose.side_effects.SharePhotoEffect
import com.kaii.photos.compose.widgets.rememberDeviceOrientation
import com.kaii.photos.compose.widgets.tags.AnimatedMediaTagManager
import com.kaii.photos.database.entities.Tag
import com.kaii.photos.datastore.AlbumType
import com.kaii.photos.domain.Result
import com.kaii.photos.domain.files.FileOperationAction
import com.kaii.photos.domain.files.FileOperationError
import com.kaii.photos.domain.files.FileOperationProgress
import com.kaii.photos.helpers.AnimationConstants
import com.kaii.photos.helpers.Screens
import com.kaii.photos.helpers.grid_management.SelectionManager
import com.kaii.photos.helpers.paging.PhotoLibraryUIModel
import com.kaii.photos.helpers.rememberVibratorManager
import com.kaii.photos.helpers.vibrateShort
import com.kaii.photos.models.CustomAlbumViewModel
import com.kaii.photos.models.ImmichAlbumViewModel
import com.kaii.photos.models.MultiAlbumViewModel
import com.kaii.photos.models.SAFAlbumViewModel
import com.kaii.photos.models.tag_page.TagViewModel
import com.kaii.photos.models.tag_page.TagViewModelFactory
import com.kaii.photos.permissions.files.rememberDynamicActivityResultLauncher
import com.kaii.photos.presentation.selection.SelectionEvent
import com.kaii.photos.presentation.selection.SelectionManagerEventEffect
import com.kaii.photos.screens.isMultiSelect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SingleAlbumView(
    album: AlbumType.Folder,
    viewModel: MultiAlbumViewModel,
    incomingIntent: Intent? = null
) {
    val pagingItems = viewModel.gridMediaFlow.collectAsLazyPagingItems()
    val allAlbums by viewModel.allAlbums.collectAsStateWithLifecycle()

    val navController = LocalNavController.current
    val dynamicAlbum by remember {
        derivedStateOf {
            allAlbums.find { it.id == album.id } as? AlbumType.Folder
        }
    }

    LaunchedEffect(dynamicAlbum) {
        delay(1000.milliseconds)
        if (dynamicAlbum == null) navController.popBackStack(Screens.MainPages.MainGrid.GridView::class, inclusive = false)
    }

    if (dynamicAlbum == null) return

    LaunchedEffect(dynamicAlbum) {
        viewModel.changeAlbum(
            album = dynamicAlbum!!
        )
    }

    val tagViewModel = viewModel<TagViewModel>(
        factory = TagViewModelFactory(
            context = LocalContext.current
        )
    )

    val tags by tagViewModel.tags.collectAsStateWithLifecycle()
    val selectedTags by tagViewModel.appliedTags.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.selectionManager) {
        viewModel.selectionManager.selection.collect { selectedItems ->
            tagViewModel.setMediaIds(
                ids = selectedItems.fastMap { it.id }
            )
        }
    }

    val columnSize by viewModel.columnSize.collectAsStateWithLifecycle()
    val openVideosExternally by viewModel.openVideosExternally.collectAsStateWithLifecycle()
    val cacheThumbnails by viewModel.cacheThumbnails.collectAsStateWithLifecycle()
    val thumbnailSize by viewModel.thumbnailSize.collectAsStateWithLifecycle()
    val useRoundedCorners by viewModel.useRoundedCorners.collectAsStateWithLifecycle()
    val confirmToDelete by viewModel.confirmToDelete.collectAsStateWithLifecycle()
    val doNotTrash by viewModel.doNotTrash.collectAsStateWithLifecycle()
    val autoDetectAlbums by viewModel.autoDetectAlbums.collectAsStateWithLifecycle()
    val vibrateOnClick by viewModel.vibrateOnClick.collectAsStateWithLifecycle()
    val mediaCount by viewModel.mediaCount.collectAsStateWithLifecycle()
    val mediaSize by viewModel.mediaSize.collectAsStateWithLifecycle()

    SingleAlbumViewCommon(
        pagingItems = pagingItems,
        fileOperationProgress = viewModel.fileOperationProgress,
        fileShareIntent = viewModel.fileShareIntent,
        eventFlow = viewModel.selectionManager.eventFlow,
        album = { dynamicAlbum!! },
        albums = { allAlbums },
        autoDetectAlbums = { autoDetectAlbums },
        selectionManager = viewModel.selectionManager,
        incomingIntent = incomingIntent,
        viewProperties = ViewProperties.Album,
        columnSize = { columnSize },
        openVideosExternally = { openVideosExternally },
        cacheThumbnails = { cacheThumbnails },
        thumbnailSize = { thumbnailSize },
        useRoundedCorners = { useRoundedCorners },
        confirmToDelete = { confirmToDelete },
        doNotTrash = { doNotTrash },
        vibrateOnClick = { vibrateOnClick },
        tags = { tags },
        selectedTags = { selectedTags },
        mediaCount = { mediaCount },
        mediaSize = { mediaSize },
        onTagAdd = tagViewModel::insertTag,
        onTagClick = tagViewModel::toggleTag,
        onTagDelete = tagViewModel::deleteTag,
        editAlbum = viewModel::editAlbum,
        removeAlbum = viewModel::removeAlbum,
        runAction = viewModel::runAction,
    )
}

@Composable
fun SingleAlbumView(
    album: AlbumType.Custom,
    viewModel: CustomAlbumViewModel,
    incomingIntent: Intent? = null
) {
    val allAlbums by viewModel.allAlbums.collectAsStateWithLifecycle()

    val navController = LocalNavController.current
    val dynamicAlbum by remember {
        derivedStateOf {
            allAlbums.find { it.id == album.id } as? AlbumType.Custom
        }
    }

    LaunchedEffect(dynamicAlbum) {
        delay(1000.milliseconds)
        if (dynamicAlbum == null) navController.popBackStack(Screens.MainPages.MainGrid.GridView::class, inclusive = false)
    }

    if (dynamicAlbum == null) return

    val pagingItems = viewModel.gridMediaFlow.collectAsLazyPagingItems()

    val columnSize by viewModel.columnSize.collectAsStateWithLifecycle()
    val openVideosExternally by viewModel.openVideosExternally.collectAsStateWithLifecycle()
    val cacheThumbnails by viewModel.cacheThumbnails.collectAsStateWithLifecycle()
    val thumbnailSize by viewModel.thumbnailSize.collectAsStateWithLifecycle()
    val useRoundedCorners by viewModel.useRoundedCorners.collectAsStateWithLifecycle()
    val confirmToDelete by viewModel.confirmToDelete.collectAsStateWithLifecycle()
    val doNotTrash by viewModel.doNotTrash.collectAsStateWithLifecycle()
    val autoDetectAlbums by viewModel.autoDetectAlbums.collectAsStateWithLifecycle()
    val vibrateOnClick by viewModel.vibrateOnClick.collectAsStateWithLifecycle()
    val mediaCount by viewModel.mediaCount.collectAsStateWithLifecycle()
    val mediaSize by viewModel.mediaSize.collectAsStateWithLifecycle()

    val tagViewModel = viewModel<TagViewModel>(
        factory = TagViewModelFactory(
            context = LocalContext.current
        )
    )

    val tags by tagViewModel.tags.collectAsStateWithLifecycle()
    val selectedTags by tagViewModel.appliedTags.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.selectionManager) {
        viewModel.selectionManager.selection.collectLatest { selectedItems ->
            tagViewModel.setMediaIds(
                ids = selectedItems.fastMap { it.id }
            )
        }
    }

    SingleAlbumViewCommon(
        pagingItems = pagingItems,
        fileOperationProgress = viewModel.fileOperationProgress,
        fileShareIntent = viewModel.fileShareIntent,
        eventFlow = viewModel.selectionManager.eventFlow,
        album = { dynamicAlbum!! },
        albums = { allAlbums },
        autoDetectAlbums = { autoDetectAlbums },
        selectionManager = viewModel.selectionManager,
        incomingIntent = incomingIntent,
        viewProperties = ViewProperties.CustomAlbum,
        columnSize = { columnSize },
        openVideosExternally = { openVideosExternally },
        cacheThumbnails = { cacheThumbnails },
        thumbnailSize = { thumbnailSize },
        useRoundedCorners = { useRoundedCorners },
        confirmToDelete = { confirmToDelete },
        doNotTrash = { doNotTrash },
        vibrateOnClick = { vibrateOnClick },
        tags = { tags },
        selectedTags = { selectedTags },
        mediaCount = { mediaCount },
        mediaSize = { mediaSize },
        onTagAdd = tagViewModel::insertTag,
        onTagClick = tagViewModel::toggleTag,
        onTagDelete = tagViewModel::deleteTag,
        editAlbum = viewModel::editAlbum,
        removeAlbum = viewModel::removeAlbum,
        runAction = viewModel::runAction,
    )
}

@Composable
fun SingleAlbumView(
    album: AlbumType.Cloud,
    viewModel: ImmichAlbumViewModel,
    incomingIntent: Intent? = null
) {
    val allAlbums by viewModel.allAlbums.collectAsStateWithLifecycle()

    val navController = LocalNavController.current
    val dynamicAlbum by remember {
        derivedStateOf {
            allAlbums.find { it.id == album.id } as? AlbumType.Cloud
        }
    }

    LaunchedEffect(dynamicAlbum) {
        delay(1000.milliseconds)
        if (dynamicAlbum == null) navController.popBackStack(Screens.MainPages.MainGrid.GridView::class, inclusive = false)
    }

    if (dynamicAlbum == null) return

    val pagingItems = viewModel.gridMediaFlow.collectAsLazyPagingItems()

    val columnSize by viewModel.columnSize.collectAsStateWithLifecycle()
    val openVideosExternally by viewModel.openVideosExternally.collectAsStateWithLifecycle()
    val cacheThumbnails by viewModel.cacheThumbnails.collectAsStateWithLifecycle()
    val thumbnailSize by viewModel.thumbnailSize.collectAsStateWithLifecycle()
    val useRoundedCorners by viewModel.useRoundedCorners.collectAsStateWithLifecycle()
    val confirmToDelete by viewModel.confirmToDelete.collectAsStateWithLifecycle()
    val doNotTrash by viewModel.doNotTrash.collectAsStateWithLifecycle()
    val autoDetectAlbums by viewModel.autoDetectAlbums.collectAsStateWithLifecycle()
    val vibrateOnClick by viewModel.vibrateOnClick.collectAsStateWithLifecycle()
    val mediaCount by viewModel.mediaCount.collectAsStateWithLifecycle()
    val mediaSize by viewModel.mediaSize.collectAsStateWithLifecycle()

    val tagViewModel = viewModel<TagViewModel>(
        factory = TagViewModelFactory(
            context = LocalContext.current
        )
    )

    val tags by tagViewModel.tags.collectAsStateWithLifecycle()
    val selectedTags by tagViewModel.appliedTags.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.selectionManager.selection) {
        viewModel.selectionManager.selection.collectLatest { selectedItems ->
            tagViewModel.setMediaIds(
                ids = selectedItems.fastMap { it.id }
            )
        }
    }

    SingleAlbumViewCommon(
        pagingItems = pagingItems,
        fileOperationProgress = viewModel.fileOperationProgress,
        fileShareIntent = viewModel.fileShareIntent,
        eventFlow = viewModel.selectionManager.eventFlow,
        album = { dynamicAlbum!! },
        albums = { allAlbums },
        autoDetectAlbums = { autoDetectAlbums },
        selectionManager = viewModel.selectionManager,
        incomingIntent = incomingIntent,
        viewProperties = ViewProperties.Immich,
        columnSize = { columnSize },
        openVideosExternally = { openVideosExternally },
        cacheThumbnails = { cacheThumbnails },
        thumbnailSize = { thumbnailSize },
        useRoundedCorners = { useRoundedCorners },
        confirmToDelete = { confirmToDelete },
        doNotTrash = { doNotTrash },
        vibrateOnClick = { vibrateOnClick },
        tags = { tags },
        selectedTags = { selectedTags },
        mediaCount = { mediaCount },
        mediaSize = { mediaSize },
        onTagAdd = tagViewModel::insertTag,
        onTagClick = tagViewModel::toggleTag,
        onTagDelete = tagViewModel::deleteTag,
        editAlbum = viewModel::editAlbum,
        removeAlbum = viewModel::removeAlbum,
        runAction = viewModel::runAction,
    )
}

@Composable
fun SingleAlbumView(
    album: AlbumType.SAFFolder,
    viewModel: SAFAlbumViewModel,
    incomingIntent: Intent? = null
) {
    val pagingItems = viewModel.gridMediaFlow.collectAsLazyPagingItems()
    val allAlbums by viewModel.allAlbums.collectAsStateWithLifecycle()

    val navController = LocalNavController.current
    val dynamicAlbum by remember {
        derivedStateOf {
            allAlbums.find { it.id == album.id } as? AlbumType.SAFFolder
        }
    }

    LaunchedEffect(dynamicAlbum) {
        delay(1000.milliseconds)
        if (dynamicAlbum == null) {
            navController.popBackStack(Screens.MainPages.MainGrid.GridView::class, inclusive = false)
        } else {
            viewModel.updateShowNested(
                value = dynamicAlbum!!.showNested
            )
        }
    }

    if (dynamicAlbum == null) return

    val tagViewModel = viewModel<TagViewModel>(
        factory = TagViewModelFactory(
            context = LocalContext.current
        )
    )

    val tags by tagViewModel.tags.collectAsStateWithLifecycle()
    val selectedTags by tagViewModel.appliedTags.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.selectionManager) {
        viewModel.selectionManager.selection.collect { selectedItems ->
            tagViewModel.setMediaIds(
                ids = selectedItems.fastMap { it.id }
            )
        }
    }

    val columnSize by viewModel.columnSize.collectAsStateWithLifecycle()
    val openVideosExternally by viewModel.openVideosExternally.collectAsStateWithLifecycle()
    val cacheThumbnails by viewModel.cacheThumbnails.collectAsStateWithLifecycle()
    val thumbnailSize by viewModel.thumbnailSize.collectAsStateWithLifecycle()
    val useRoundedCorners by viewModel.useRoundedCorners.collectAsStateWithLifecycle()
    val confirmToDelete by viewModel.confirmToDelete.collectAsStateWithLifecycle()
    val doNotTrash by viewModel.doNotTrash.collectAsStateWithLifecycle()
    val autoDetectAlbums by viewModel.autoDetectAlbums.collectAsStateWithLifecycle()
    val vibrateOnClick by viewModel.vibrateOnClick.collectAsStateWithLifecycle()
    val mediaCount by viewModel.mediaCount.collectAsStateWithLifecycle()
    val mediaSize by viewModel.mediaSize.collectAsStateWithLifecycle()

    SingleAlbumViewCommon(
        pagingItems = pagingItems,
        fileOperationProgress = viewModel.fileOperationProgress,
        fileShareIntent = viewModel.fileShareIntent,
        eventFlow = viewModel.selectionManager.eventFlow,
        album = { dynamicAlbum!! },
        albums = { allAlbums },
        autoDetectAlbums = { autoDetectAlbums },
        selectionManager = viewModel.selectionManager,
        incomingIntent = incomingIntent,
        viewProperties = ViewProperties.SAFFolder,
        columnSize = { columnSize },
        openVideosExternally = { openVideosExternally },
        cacheThumbnails = { cacheThumbnails },
        thumbnailSize = { thumbnailSize },
        useRoundedCorners = { useRoundedCorners },
        confirmToDelete = { confirmToDelete },
        doNotTrash = { doNotTrash },
        vibrateOnClick = { vibrateOnClick },
        tags = { tags },
        selectedTags = { selectedTags },
        mediaCount = { mediaCount },
        mediaSize = { mediaSize },
        onTagAdd = tagViewModel::insertTag,
        onTagClick = tagViewModel::toggleTag,
        onTagDelete = tagViewModel::deleteTag,
        editAlbum = viewModel::editAlbum,
        removeAlbum = viewModel::removeAlbum,
        runAction = viewModel::runAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleAlbumViewCommon(
    pagingItems: LazyPagingItems<PhotoLibraryUIModel>,
    fileOperationProgress: Flow<FileOperationProgress<Unit>>,
    fileShareIntent: Flow<Result<Intent, FileOperationError>>,
    eventFlow: Flow<SelectionEvent>,
    album: () -> AlbumType,
    albums: () -> List<AlbumType>,
    autoDetectAlbums: () -> Boolean,
    selectionManager: SelectionManager,
    incomingIntent: Intent?,
    viewProperties: ViewProperties,
    columnSize: () -> Int,
    openVideosExternally: () -> Boolean,
    cacheThumbnails: () -> Boolean,
    thumbnailSize: () -> Int,
    useRoundedCorners: () -> Boolean,
    confirmToDelete: () -> Boolean,
    doNotTrash: () -> Boolean,
    vibrateOnClick: () -> Boolean,
    modifier: Modifier = Modifier,
    tags: () -> List<Tag>,
    selectedTags: () -> List<Tag>,
    mediaCount: () -> Int,
    mediaSize: () -> String,
    onTagAdd: (name: String) -> Unit,
    onTagClick: (tag: Tag) -> Unit,
    onTagDelete: (tag: Tag) -> Unit,
    editAlbum: (id: String, newInfo: AlbumType) -> Unit,
    removeAlbum: (id: String) -> Unit,
    runAction: (action: FileOperationAction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showInfoDialog by remember { mutableStateOf(false) }
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    SelectionManagerEventEffect(eventFlow)

    if (showInfoDialog) {
        val vibratorManager = rememberVibratorManager()

        LaunchedEffect(album()) {
            runAction(
                FileOperationAction.LoadMediaCountAndSize(
                    album = album()
                )
            )
        }

        AlbumInfoDialog(
            albumInfo = album,
            albums = albums,
            autoDetectAlbums = autoDetectAlbums,
            sheetState = sheetState,
            itemCount = mediaCount,
            albumSize = mediaSize,
            toggleSelectionMode = {
                vibratorManager.vibrateShort()
                selectionManager.enterSelectMode()
                coroutineScope.launch {
                    sheetState.hide()
                    showInfoDialog = false
                }
            },
            editAlbum = editAlbum,
            renameAlbum = {
                runAction(
                    FileOperationAction.RenameAlbum(
                        album = album(),
                        newName = it
                    )
                )
            },
            removeAlbum = {
                coroutineScope.launch {
                    sheetState.hide()
                    removeAlbum(it)
                    showInfoDialog = false
                }
            },
            dismiss = {
                coroutineScope.launch {
                    sheetState.hide()
                    showInfoDialog = false
                }
            }
        )
    }

    val dynamicActivityResultLauncher = rememberDynamicActivityResultLauncher()
    SharePhotoEffect(
        shareFlow = fileShareIntent,
        dynamicActivityResultLauncher = dynamicActivityResultLauncher,
        reShare = { files ->
            runAction(
                FileOperationAction.Share(
                    files = files
                )
            )
        }
    )

    FileOperationProgressEffect(
        operationFlow = fileOperationProgress,
        dynamicActivityResultLauncher = dynamicActivityResultLauncher,
        runAction = runAction
    )

    var showTagDialog by remember { mutableStateOf(false) }
    val backdrop = rememberLayerBackdrop()
    val isSelecting by selectionManager.enabled.collectAsStateWithLifecycle(initialValue = false)
    val topBarHeight = lumaTopBarHeight()
    val topBarScroll = rememberLumaTopBarScrollState(isSelecting)

    Scaffold(modifier = modifier.fillMaxSize(1f)) { padding ->
        Box(Modifier.fillMaxSize()) {
            AnimatedMediaTagManager(
                showTagDialog = showTagDialog,
                padding = padding,
                tags = tags,
                selectedTags = selectedTags,
                onTagAdd = onTagAdd,
                onTagClick = onTagClick,
                onTagDelete = onTagDelete,
                onClose = { showTagDialog = false }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(topBarScroll.connection),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PhotoGrid(
                    pagingItems = pagingItems,
                    album = album,
                    selectionManager = selectionManager,
                    viewProperties = viewProperties,
                    isMediaPicker = incomingIntent != null,
                    isMultiSelectMode = incomingIntent.isMultiSelect,
                    isMainPage = false,
                    columnSize = columnSize,
                    openVideosExternally = openVideosExternally,
                    cacheThumbnails = cacheThumbnails,
                    thumbnailSize = thumbnailSize,
                    useRoundedCorners = useRoundedCorners,
                    vibrateOnClick = vibrateOnClick,
                    topContentPadding = topBarHeight,
                    modifier = Modifier.layerBackdrop(backdrop)
                )
            }

            SingleAlbumViewTopBar(
                albumInfo = album,
                selectionManager = selectionManager,
                isMediaPicker = incomingIntent != null,
                showTagDialog = { showTagDialog },
                setShowTagDialog = { showTagDialog = it },
                showDialog = {
                    coroutineScope.launch {
                        showInfoDialog = true
                        delay(50.milliseconds)
                        sheetState.expand()
                    }
                },
                backdrop = backdrop,
                topBarOffsetPx = topBarScroll.offsetPx
            )

            AnimatedVisibility(
                visible = isSelecting && incomingIntent == null,
                enter = fadeIn() + slideInVertically(animationSpec = AnimationConstants.expressiveSpring()) { it },
                exit = fadeOut() + slideOutVertically(animationSpec = AnimationConstants.expressiveTween()) { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                SelectionActionBar(
                    backdrop = backdrop,
                    albumInfo = album(),
                    selectionManager = selectionManager,
                    confirmToDelete = confirmToDelete,
                    doNotTrash = doNotTrash,
                    runAction = runAction
                )
            }
        }
    }
}
