package com.kaii.photos.compose.app_bars.main_bars

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import com.kyant.backdrop.Backdrop
import com.kaii.photos.LocalNavController
import com.kaii.photos.R
import com.kaii.photos.helpers.Screens
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs

private val LumaBarColor = Color(0xFF0B0E1A)
private val LumaTitleRowHeight = 76.dp
private val LumaBottomFadeHeight = 20.dp
private val LumaStatusFadeHeight = 8.dp

private val LumaTitleRowHeightPxDp = 76.dp

class LumaTopBarScrollState internal constructor(
    val offsetPx: () -> Float,
    val connection: NestedScrollConnection
)

@Composable
fun rememberLumaTopBarScrollState(isSelecting: Boolean): LumaTopBarScrollState {
    val density = LocalDensity.current
    val maxOffset = with(density) { LumaTitleRowHeightPxDp.toPx() }
    val scope = rememberCoroutineScope()
    var offset by remember { mutableFloatStateOf(0f) }
    val settleJob = remember { AtomicReference<Job?>(null) }

    val connection = remember(maxOffset, isSelecting) {
        object : NestedScrollConnection {
            private fun update(delta: Float, forceShown: Boolean = false) {
                settleJob.getAndSet(null)?.cancel()
                offset = if (forceShown) 0f else (offset + delta).coerceIn(-maxOffset, 0f)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (isSelecting || (available.y > 0f && consumed.y == 0f)) update(0f, forceShown = true)
                else if (consumed.y != 0f) update(consumed.y)
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val target = if (isSelecting) 0f else when {
                    abs(available.y) > 200f -> if (available.y < 0f) -maxOffset else 0f
                    offset <= -maxOffset / 2f -> -maxOffset
                    else -> 0f
                }
                settleJob.getAndSet(null)?.cancel()
                settleJob.set(scope.launch {
                    animate(initialValue = offset, targetValue = target, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { value, _ ->
                        offset = value
                    }
                })
                return Velocity.Zero
            }
        }
    }

    LaunchedEffect(isSelecting) {
        if (isSelecting) {
            settleJob.getAndSet(null)?.cancel()
            offset = 0f
        }
    }

    return remember(connection) { LumaTopBarScrollState({ offset }, connection) }
}

@Composable
fun lumaTopBarHeight(): Dp {
    val density = LocalDensity.current
    val topInset = WindowInsets.statusBars.union(WindowInsets.displayCutout).getTop(density)
    return with(density) { topInset.toDp() } + LumaTitleRowHeight
}

@Composable
fun LumaTopBar(
    modifier: Modifier = Modifier,
    backdrop: Backdrop,
    titleRowOffsetPx: () -> Float = { 0f },
    leading: @Composable () -> Unit = {
        Text(
            text = "Luma",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 20.dp)
        )
    },
    trailing: @Composable () -> Unit = {
        val navController = LocalNavController.current
        LiquidGlassIconButton(
            backdrop = backdrop,
            icon = painterResource(R.drawable.settings),
            contentDescription = "Settings",
            onClick = { navController.navigate(Screens.MainPages.MainGrid.SettingsDialog) },
            modifier = Modifier.padding(end = 16.dp)
        )
    },
    floatingLeading: @Composable () -> Unit = {},
    floatingTrailing: @Composable () -> Unit = trailing
) {
    val density = LocalDensity.current
    val insets = WindowInsets.statusBars.union(WindowInsets.displayCutout)
    val topInset = with(density) { insets.getTop(density).toDp() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(topInset + LumaTitleRowHeight)
            .zIndex(10f)
    ) {
        // The status-bar strip and its fade stay fixed while the title row collapses.
        Box(
            Modifier
                .fillMaxWidth()
                .height(topInset)
                .background(LumaBarColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(LumaStatusFadeHeight)
                .offset(y = topInset)
                .drawWithContent {
                    drawRect(Brush.verticalGradient(listOf(LumaBarColor, LumaBarColor.copy(alpha = 0f))))
                }
        )
        // Only the title and opaque header surface follow the scroll offset.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(LumaTitleRowHeight)
                .offset(y = topInset)
                .graphicsLayer {
                    translationY = titleRowOffsetPx()
                    alpha = (1f + titleRowOffsetPx() / with(density) { LumaTitleRowHeight.toPx() }).coerceIn(0f, 1f)
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LumaTitleRowHeight - LumaBottomFadeHeight)
                    .background(LumaBarColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LumaTitleRowHeight - LumaBottomFadeHeight)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LumaTitleRowHeight),
                contentAlignment = Alignment.CenterStart
            ) { leading() }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(LumaBottomFadeHeight)
                    .drawWithContent {
                        drawRect(Brush.verticalGradient(listOf(LumaBarColor, LumaBarColor.copy(alpha = 0f))))
                    }
            )
        }

        // Glass controls remain pinned above the moving title/background layer.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(LumaTitleRowHeight)
                .offset(y = topInset)
                .zIndex(2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) { floatingLeading() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) { floatingTrailing() }
        }
    }
}


@Composable
fun LiquidGlassSelectButton(
    backdrop: Backdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        scale.animateTo(
            if (pressed) 1.06f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.62f)
        )
    }

    Box(
        modifier = modifier
            .height(46.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(percent = 50) },
                effects = {
                    blur(3.dp.toPx())
                    lens(10.dp.toPx(), 18.dp.toPx())
                },
                highlight = { Highlight.Default.copy(alpha = 0.45f) },
                shadow = { Shadow(alpha = 0.22f) },
                innerShadow = { InnerShadow(radius = 3.dp, alpha = 0.12f) },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.07f)) }
            )
            .clip(RoundedCornerShape(percent = 50))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Select",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
