package com.kaii.photos.compose.app_bars.main_bars

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.Backdrop
import com.kaii.photos.R
import com.kaii.photos.compose.app_bars.SelectingBottomBarItems
import com.kaii.photos.datastore.AlbumType
import com.kaii.photos.datastore.BottomBarTab
import com.kaii.photos.datastore.DefaultTabs
import com.kaii.photos.domain.files.FileOperationAction
import com.kaii.photos.helpers.AnimationConstants
import com.kaii.photos.helpers.grid_management.SelectionManager
import kotlinx.coroutines.launch

private val LumaActiveBlue = Color(0xFF7CB4FF)

@Composable
fun MainAppBottomBar(
    pagerState: PagerState,
    tabs: List<BottomBarTab>,
    selectionManager: SelectionManager,
    confirmToDelete: () -> Boolean,
    doNotTrash: () -> Boolean,
    runAction: (action: FileOperationAction) -> Unit,
    liquidGlassBackdrop: Backdrop
) {
    val coroutineScope = rememberCoroutineScope()
    val windowInfo = LocalWindowInfo.current
    val screenWidthDp = with(LocalDensity.current) { windowInfo.containerSize.width.toDp() }
    val windowWidth = windowInfo.containerSize.width.toFloat()
    val currentTab = tabs.getOrNull(pagerState.currentPage)
    val searchIndex = tabs.indexOf(DefaultTabs.TabTypes.search)
    val visibleTabs = tabs.filter { it != DefaultTabs.TabTypes.search }
    val isSelecting by selectionManager.enabled.collectAsStateWithLifecycle(initialValue = false)
    fun navigateToTab(index: Int) {
        if (index !in tabs.indices) return
        coroutineScope.launch {
            pagerState.animateScrollBy(
                value = windowWidth * -(pagerState.currentPage - index) -
                    pagerState.currentPageOffsetFraction * windowWidth,
                animationSpec = AnimationConstants.defaultSpring()
            )
        }
    }

    Box(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        // Fresh custom layout: no Material floating-toolbar container or legacy geometry.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 286.dp)
                    .height(64.dp)
                    .lumaLiquidGlassSurface(liquidGlassBackdrop, CircleShape)
                    .clip(CircleShape)
            ) {
                AnimatedContent(
                    targetState = isSelecting,
                    transitionSpec = {
                        (slideInHorizontally() + fadeIn()).togetherWith(
                            scaleOut(
                                transformOrigin = TransformOrigin(1f, 0.5f)
                            ) + fadeOut()
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { selectionMode ->
                    if (selectionMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val selectedTab = currentTab ?: tabs.firstOrNull()
                            if (selectedTab != null) {
                                SelectingBottomBarItems(
                                    albumInfo = AlbumType.Folder(
                                        id = selectedTab.id.toString(),
                                        name = selectedTab.name,
                                        paths = selectedTab.albumPaths,
                                        pinned = false,
                                        immichId = null
                                    ),
                                    selectionManager = selectionManager,
                                    confirmToDelete = confirmToDelete,
                                    doNotTrash = doNotTrash,
                                    runAction = runAction
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            visibleTabs.forEach { tab ->
                                val selected = currentTab == tab
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp)
                                        .padding(horizontal = 2.dp)
                                        .then(
                                            if (selected) {
                                                Modifier.lumaLiquidGlassSurface(
                                                    backdrop = liquidGlassBackdrop,
                                                    shape = CircleShape,
                                                    fillAlpha = 0.17f,
                                                    lensWidth = 12.dp,
                                                    lensHeight = 18.dp
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .clip(CircleShape)
                                        .clickable { navigateToTab(tabs.indexOf(tab)) }
                                        .testTag(tab.name)
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = tab.icon.nonFilled),
                                        contentDescription = stringResource(
                                            id = R.string.tabs_navigate_to,
                                            tab.name
                                        ),
                                        tint = if (selected) LumaActiveBlue
                                        else Color.White.copy(alpha = 0.88f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tab.name,
                                        color = if (selected) LumaActiveBlue
                                        else Color.White.copy(alpha = 0.88f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!isSelecting) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .lumaLiquidGlassSurface(liquidGlassBackdrop, CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            navigateToTab(searchIndex)
                        }
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag("main_search_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = stringResource(id = R.string.navigation_search),
                        tint = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            }
        }
    }
}
