package com.kaii.photos.compose.app_bars.main_bars

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
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
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kaii.photos.R
import com.kaii.photos.compose.app_bars.SelectingBottomBarItems
import com.kaii.photos.datastore.AlbumType
import com.kaii.photos.datastore.BottomBarTab
import com.kaii.photos.datastore.DefaultTabs
import com.kaii.photos.domain.files.FileOperationAction
import com.kaii.photos.helpers.AnimationConstants
import com.kaii.photos.helpers.grid_management.SelectionManager
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val LumaActiveBlue = Color(0xFF7CB4FF)
private val LumaTabInk = Color.White.copy(alpha = 0.88f)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainAppBottomBar(
    pagerState: PagerState,
    tabs: List<BottomBarTab>,
    selectionManager: SelectionManager,
    scrollBehaviour: FloatingToolbarScrollBehavior,
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
    val currentVisibleIndex = visibleTabs.indexOf(currentTab).coerceAtLeast(0)
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

    val lensPosition = remember { Animatable(currentVisibleIndex.toFloat()) }
    val lensPressed = remember { Animatable(0f) }
    val tabCount = visibleTabs.size
    LaunchedEffect(currentVisibleIndex, isSelecting) {
        if (!isSelecting && lensPressed.value == 0f && tabCount > 0) {
            lensPosition.animateTo(
                currentVisibleIndex.coerceIn(0, tabCount - 1).toFloat(),
                spring(dampingRatio = 0.72f, stiffness = 360f)
            )
        }
    }

    Box(Modifier.wrapContentHeight().fillMaxWidth()) {
        HorizontalFloatingToolbar(
            expanded = false,
            collapsedShadowElevation = 0.dp,
            expandedShadowElevation = 0.dp,
            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = Color.Transparent,
                toolbarContentColor = Color.White
            ),
            scrollBehavior = scrollBehaviour,
            modifier = Modifier
                .align(Alignment.Center)
                .windowInsetsPadding(WindowInsets.systemBars)
                .offset(y = (-6).dp)
                .widthIn(max = (screenWidthDp - 32.dp).coerceAtLeast(0.dp))
                .height(64.dp),
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = 286.dp)
                            .height(64.dp)
                            .lumaLiquidGlassSurface(liquidGlassBackdrop, CircleShape, blurRadius = 3.dp)
                            .clip(CircleShape)
                    ) {
                        AnimatedContent(
                            targetState = isSelecting,
                            transitionSpec = {
                                (slideInHorizontally() + fadeIn()).togetherWith(
                                    scaleOut(transformOrigin = TransformOrigin(1f, 0.5f)) + fadeOut()
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        ) { selectionMode ->
                            if (selectionMode) {
                                Row(
                                    Modifier.fillMaxWidth().height(64.dp),
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
                            } else if (visibleTabs.isNotEmpty()) {
                                val tabsBackdrop = rememberLayerBackdrop()
                                BoxWithConstraints(Modifier.fillMaxSize()) {
                                    val density = LocalDensity.current
                                    val padPx = with(density) { 4.dp.toPx() }
                                    val tabWidthPx = with(density) {
                                        (maxWidth - 8.dp).toPx() / visibleTabs.size
                                    }
                                    val tabWidthDp = with(density) { tabWidthPx.toDp() }
                                    val combinedBackdrop = rememberCombinedBackdrop(liquidGlassBackdrop, tabsBackdrop)

                                    // Record tab labels and icons separately; the moving lens reads this layer too.
                                    Row(
                                        Modifier.fillMaxSize().padding(horizontal = 4.dp).layerBackdrop(tabsBackdrop),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        visibleTabs.forEachIndexed { index, tab ->
                                            val near = (1f - abs(lensPosition.value - index)).coerceIn(0f, 1f)
                                            val tint = androidx.compose.ui.graphics.lerp(LumaTabInk, LumaActiveBlue, near)
                                            Column(
                                                Modifier.weight(1f).fillMaxHeight().clip(CircleShape),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = tab.icon.nonFilled),
                                                    contentDescription = stringResource(
                                                        id = R.string.tabs_navigate_to,
                                                        tab.name
                                                    ),
                                                    tint = tint,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = tab.name,
                                                    color = tint,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    // The lens is a separate sibling above the recorded tab row.
                                    Box(
                                        Modifier
                                            .padding(vertical = 4.dp)
                                            .width(tabWidthDp)
                                            .fillMaxHeight()
                                            .offset(x = with(density) { (padPx + lensPosition.value * tabWidthPx).toDp() })
                                            .drawBackdrop(
                                                backdrop = combinedBackdrop,
                                                shape = { CircleShape },
                                                effects = {
                                                    vibrancy()
                                                    lens(
                                                        with(density) { (8 + 10 * lensPressed.value).dp.toPx() },
                                                        with(density) { (14 + 26 * lensPressed.value).dp.toPx() },
                                                    )
                                                },
                                                layerBlock = {
                                                    val scale = 1f + 0.18f * lensPressed.value
                                                    scaleX = scale
                                                    scaleY = scale
                                                },
                                                onDrawSurface = {
                                                    drawRect(Color.White.copy(alpha = 0.04f + 0.12f * (1f - lensPressed.value)))
                                                }
                                            )
                                    )

                                    // Drag on the pill to move the lens; release snaps to the nearest tab.
                                    Box(
                                        Modifier
                                            .matchParentSize()
                                            .pointerInput(tabCount, tabWidthPx) {
                                                awaitEachGesture {
                                                    val down = awaitFirstDown()
                                                    var lastX = down.position.x
                                                    coroutineScope.launch {
                                                        lensPressed.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 420f))
                                                    }
                                                    val startIndex = ((lastX - padPx) / tabWidthPx).roundToInt().coerceIn(0, visibleTabs.lastIndex)
                                                    coroutineScope.launch {
                                                        lensPosition.animateTo(startIndex.toFloat(), spring(dampingRatio = 0.7f, stiffness = 420f))
                                                    }
                                                    do {
                                                        val event = awaitPointerEvent()
                                                        val change = event.changes.first()
                                                        if (change.positionChanged()) {
                                                            lastX = change.position.x
                                                            val value = ((lastX - padPx) / tabWidthPx - 0.5f)
                                                                .coerceIn(0f, visibleTabs.lastIndex.toFloat())
                                                            coroutineScope.launch { lensPosition.snapTo(value) }
                                                            change.consume()
                                                        }
                                                    } while (event.changes.any { it.pressed })
                                                    val target = ((lastX - padPx) / tabWidthPx)
                                                        .roundToInt().coerceIn(0, visibleTabs.lastIndex)
                                                    coroutineScope.launch {
                                                        lensPressed.animateTo(0f, spring(dampingRatio = 0.72f, stiffness = 360f))
                                                        lensPosition.animateTo(target.toFloat(), spring(dampingRatio = 0.58f, stiffness = 280f))
                                                    }
                                                    navigateToTab(tabs.indexOf(visibleTabs[target]))
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    }

                    if (!isSelecting) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .lumaLiquidGlassSurface(liquidGlassBackdrop, CircleShape, blurRadius = 3.dp)
                                .clip(CircleShape)
                                .clickable { navigateToTab(searchIndex) }
                                .semantics { testTagsAsResourceId = true }
                                .testTag("main_search_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = stringResource(id = R.string.navigation_search),
                                tint = LumaTabInk,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        )
    }
}
