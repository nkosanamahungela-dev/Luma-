package com.kaii.photos.compose.app_bars.main_bars

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kaii.photos.R
import com.kaii.photos.compose.app_bars.SelectingBottomBarItems
import com.kaii.photos.datastore.AlbumType
import com.kaii.photos.datastore.BottomBarTab
import com.kaii.photos.datastore.DefaultTabs
import com.kaii.photos.domain.files.FileOperationAction
import com.kaii.photos.helpers.AnimationConstants
import com.kaii.photos.helpers.grid_management.SelectionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.roundToInt

private val LumaInactive = Color.White.copy(alpha = 0.58f)
private val LumaGlassTint = Color.White.copy(alpha = 0.07f)
private val LumaTabLayerTint = Color(0xFFCFD6E0)

/** Luma adaptation of Kyant0 AndroidLiquidGlass 2.0.0 Catalog LiquidBottomTabs (Apache-2.0). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainAppBottomBar(
    pagerState: androidx.compose.foundation.pager.PagerState,
    tabs: List<BottomBarTab>,
    selectionManager: SelectionManager,
    scrollBehaviour: FloatingToolbarScrollBehavior,
    confirmToDelete: () -> Boolean,
    doNotTrash: () -> Boolean,
    runAction: (action: FileOperationAction) -> Unit,
    liquidGlassBackdrop: Backdrop,
    onBarPressedChanged: (Boolean) -> Unit,
    selectedTabOverride: Int? = null,
    onIsolatedTabSelected: (Int) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val windowWidth = LocalWindowInfo.current.containerSize.width.toFloat()
    val activeTabIndex = selectedTabOverride ?: pagerState.currentPage
    val currentTab = tabs.getOrNull(activeTabIndex)
    val searchIndex = tabs.indexOf(DefaultTabs.TabTypes.search)
    val visibleTabs = tabs.filter { it != DefaultTabs.TabTypes.search }
    val currentVisibleIndex = visibleTabs.indexOf(currentTab).coerceAtLeast(0)
    val isSelecting by selectionManager.enabled.collectAsStateWithLifecycle(initialValue = false)

    fun navigateToTab(index: Int) {
        if (index !in tabs.indices) return
        if (selectedTabOverride != null) {
            onIsolatedTabSelected(index)
            return
        }
        scope.launch {
            pagerState.animateScrollBy(
                value = windowWidth * -(pagerState.currentPage - index) -
                    pagerState.currentPageOffsetFraction * windowWidth,
                animationSpec = AnimationConstants.defaultSpring()
            )
        }
    }

    // The scroll behavior moves the unclipped custom bar, preserving the original hide/show animation.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(scrollBehaviour.floatingScrollBehaviorModifier)
            .wrapContentHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .consumeAllPointerChanges(onBarPressedChanged) // Own touches across the full bar row.
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 16.dp, end = 16.dp, bottom = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 286.dp)
                    .height(64.dp)
                    .consumeAllPointerChanges(onBarPressedChanged),
                contentAlignment = Alignment.CenterStart
            ) {
                val barWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                val tabsCount = visibleTabs.size.coerceAtLeast(1)
                val offsetAnimation = remember { Animatable(0f) }
                val horizontalInsetPx = with(density) { 8.dp.toPx() }
                val rawTabWidthPx = (barWidthPx - horizontalInsetPx) / tabsCount
                val tabWidthPx = rawTabWidthPx.coerceAtLeast(1f)
                val panelOffset by remember(density, barWidthPx) {
                    derivedStateOf {
                        val fraction = (offsetAnimation.value / barWidthPx).coerceIn(-1f, 1f)
                        with(density) {
                            4.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                        }
                    }
                }
                val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
                var currentIndex by remember { mutableIntStateOf(currentVisibleIndex.coerceIn(0, tabsCount - 1)) }

                LaunchedEffect(currentVisibleIndex) {
                    currentIndex = currentVisibleIndex.coerceIn(0, tabsCount - 1)
                }

                val dragAnimation = remember(scope, tabsCount) {
                    DampedDragAnimation(
                        animationScope = scope,
                        initialValue = currentVisibleIndex.toFloat(),
                        valueRange = 0f..(tabsCount - 1).toFloat(),
                        visibilityThreshold = 0.001f,
                        initialScale = 1f,
                        pressedScale = 78f / 56f,
                        onDragStarted = {},
                        onDragStopped = {
                            val targetIndex = targetValue.roundToInt().coerceIn(0, tabsCount - 1)
                            currentIndex = targetIndex
                            animateToValue(targetIndex.toFloat())
                            scope.launch {
                                offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
                            }
                        },
                        onDrag = { _, dragAmount ->
                            updateValue(
                                (targetValue + dragAmount.x / tabWidthPx * if (isLtr) 1f else -1f)
                                    .coerceIn(0f, (tabsCount - 1).toFloat())
                            )
                            scope.launch { offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x) }
                        }
                    )
                }

                LaunchedEffect(dragAnimation) {
                    snapshotFlow { currentIndex }
                        .drop(1)
                        .collectLatest { index ->
                            dragAnimation.animateToValue(index.toFloat())
                            navigateToTab(tabs.indexOf(visibleTabs[index]))
                        }
                }

                val interactiveHighlight = remember(scope, tabWidthPx) {
                    InteractiveHighlight(
                        animationScope = scope,
                        position = { size, _ ->
                            Offset(
                                if (isLtr) (dragAnimation.value + 0.5f) * tabWidthPx + panelOffset
                                else size.width - (dragAnimation.value + 0.5f) * tabWidthPx + panelOffset,
                                size.height / 2f
                            )
                        }
                    )
                }

                val tabsContent: @Composable RowScope.() -> Unit = {
                    visibleTabs.forEachIndexed { index, tab ->
                        val active = index == currentIndex
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = null,
                                    indication = null
                                ) { currentIndex = index }
                                .graphicsLayer {
                                    val scale = 1f + 0.2f * dragAnimation.pressProgress
                                    scaleX = scale
                                    scaleY = scale
                                },
                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = if (active) tab.icon.filled else tab.icon.nonFilled),
                                contentDescription = stringResource(
                                    id = R.string.tabs_navigate_to,
                                    tab.name
                                ),
                                tint = if (active) Color.White else LumaInactive,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = tab.name,
                                color = if (active) Color.White else LumaInactive,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }

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
                            Modifier.fillMaxSize(),
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
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                            Row(
                                modifier = Modifier
                                    .graphicsLayer {
                                        translationX = panelOffset
                                        val scale = 1f + 0.035f * dragAnimation.pressProgress
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .drawBackdrop(
                                        backdrop = liquidGlassBackdrop,
                                        shape = { RoundedCornerShape(percent = 50) },
                                        effects = {
                                            blur(3.dp.toPx())
                                            lens(16.dp.toPx(), 32.dp.toPx())
                                        },
                                        highlight = { Highlight.Default.copy(alpha = 0.72f) },
                                        shadow = { Shadow(alpha = 0.28f) },
                                        innerShadow = { InnerShadow(radius = 5.dp, alpha = 0.14f) },
                                        layerBlock = {
                                            val progress = dragAnimation.pressProgress
                                            val scale = 1f + 16.dp.toPx() / size.width * progress
                                            scaleX = scale
                                            scaleY = scale
                                        },
                                        onDrawSurface = { drawRect(LumaGlassTint) }
                                    )
                                    .then(interactiveHighlight.modifier)
                                    .height(64.dp)
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                content = tabsContent
                            )

                        }
                    }
                }

                // Lightweight animated selection pill; no backdrop capture or refraction shader. 
                if (!isSelecting && visibleTabs.isNotEmpty()) {
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .graphicsLayer {
                                translationX = if (isLtr) {
                                    dragAnimation.value * tabWidthPx + panelOffset
                                } else {
                                    size.width - (dragAnimation.value + 1f) * tabWidthPx + panelOffset
                                }
                            }
                            .then(interactiveHighlight.gestureModifier)
                            .then(dragAnimation.modifier)
                            .consumeAllPointerChanges(onBarPressedChanged)
                            .graphicsLayer {
                                scaleX = dragAnimation.scaleX
                                scaleY = dragAnimation.scaleY
                            }
                            .drawBackdrop(
                                backdrop = liquidGlassBackdrop,
                                shape = { RoundedCornerShape(percent = 50) },
                                effects = {
                                    val progress = dragAnimation.pressProgress
                                    lens(
                                        10.dp.toPx() * progress,
                                        14.dp.toPx() * progress,
                                        chromaticAberration = true
                                    )
                                },
                                highlight = {
                                    Highlight.Default.copy(alpha = 0.22f + 0.78f * dragAnimation.pressProgress)
                                },
                                shadow = { Shadow(alpha = dragAnimation.pressProgress) },
                                innerShadow = {
                                    InnerShadow(
                                        radius = 8.dp * dragAnimation.pressProgress,
                                        alpha = dragAnimation.pressProgress
                                    )
                                },
                                layerBlock = {
                                    scaleX = dragAnimation.scaleX
                                    scaleY = dragAnimation.scaleY
                                    val velocity = dragAnimation.velocity / 10f
                                    scaleX /= 1f - (velocity * 0.75f).coerceIn(-0.2f, 0.2f)
                                    scaleY *= 1f - (velocity * 0.25f).coerceIn(-0.2f, 0.2f)
                                },
                                onDrawSurface = {
                                    val progress = dragAnimation.pressProgress
                                    drawRect(Color.Black.copy(alpha = 0.10f), alpha = 1f - progress)
                                    drawRect(Color.Black.copy(alpha = 0.03f * progress))
                                }
                            )
                            .height(56.dp)
                            .fillMaxWidth(1f / tabsCount)
                    )
                }
            }

            if (!isSelecting) {
                SearchGlassButton(
                    backdrop = liquidGlassBackdrop,
                    onClick = { navigateToTab(searchIndex) },
                    onBarPressedChanged = onBarPressedChanged
                )
            }
        }
    }
}

@Composable
private fun SearchGlassButton(
    backdrop: Backdrop,
    onClick: () -> Unit,
    onBarPressedChanged: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressedState by interactionSource.collectIsPressedAsState()
    val pressed = remember { Animatable(0f) }
    LaunchedEffect(pressedState) {
        pressed.animateTo(
            if (pressedState) 1f else 0f,
            spring(dampingRatio = 0.6f, stiffness = 420f)
        )
    }

    Box(
        Modifier
            .size(62.dp)
            .graphicsLayer {
                val scale = 1f + 0.12f * pressed.value
                scaleX = scale
                scaleY = scale
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = {
                    blur(3.dp.toPx())
                    lens(
                        16.dp.toPx() * (1f + 0.25f * pressed.value),
                        32.dp.toPx() * (1f + 0.25f * pressed.value),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                },
                highlight = { Highlight.Default.copy(alpha = 0.35f + 0.4f * pressed.value) },
                shadow = { Shadow(alpha = 0.3f) },
                innerShadow = { InnerShadow(radius = 5.dp, alpha = 0.16f) },
                onDrawSurface = { drawRect(LumaGlassTint) }
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .semantics { testTagsAsResourceId = true }
            .testTag("main_search_button")
            .consumeAllPointerChanges(onBarPressedChanged),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.search),
            contentDescription = stringResource(id = R.string.navigation_search),
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(26.dp)
        )
    }
}

/** Lock scroll-driven hiding for the full lifetime of a touch on the bar. */
private fun Modifier.consumeAllPointerChanges(
    onBarPressedChanged: (Boolean) -> Unit
): Modifier = pointerInput(onBarPressedChanged) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        onBarPressedChanged(true)
        try {
            var pressed = true
            while (pressed) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                pressed = event.changes.any { it.pressed }
            }
        } finally {
            onBarPressedChanged(false)
        }
    }
}.pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
        down.consume()
        do {
            val event = awaitPointerEvent(PointerEventPass.Final)
            event.changes.forEach { change ->
                change.consume()
            }
        } while (event.changes.any { it.pressed })
    }
}
