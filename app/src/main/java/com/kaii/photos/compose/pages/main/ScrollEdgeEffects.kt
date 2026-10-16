package com.kaii.photos.compose.pages.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.calculateBottomPadding
import androidx.compose.foundation.layout.calculateTopPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur

private val TOP_EDGE_ZONE_HEIGHT = 144.dp
private val BOTTOM_EDGE_ZONE_HEIGHT = 132.dp
private val TOP_EDGE_BLUR_RADIUS = 18.dp
private val BOTTOM_EDGE_BLUR_RADIUS = 14.dp
private const val TOP_EDGE_SCRIM_ALPHA = 0.55f
private const val BOTTOM_EDGE_SCRIM_ALPHA = 0.45f
private val EDGE_SCRIM_COLOR = Color(0xFF0B0E1A)

@Composable
fun ScrollEdgeEffects(backdrop: Backdrop, modifier: Modifier = Modifier) {
    val statusInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val topBlurMask = remember {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0f to Color.White,
                0.32f to Color.White.copy(alpha = 0.78f),
                0.68f to Color.White.copy(alpha = 0.28f),
                1f to Color.Transparent
            )
        )
    }
    val bottomBlurMask = remember {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                0.32f to Color.White.copy(alpha = 0.28f),
                0.68f to Color.White.copy(alpha = 0.78f),
                1f to Color.White
            )
        )
    }
    val topScrim = remember {
        Brush.verticalGradient(
            colors = listOf(EDGE_SCRIM_COLOR.copy(alpha = TOP_EDGE_SCRIM_ALPHA), Color.Transparent)
        )
    }
    val bottomScrim = remember {
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, EDGE_SCRIM_COLOR.copy(alpha = BOTTOM_EDGE_SCRIM_ALPHA))
        )
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(TOP_EDGE_ZONE_HEIGHT + statusInset)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawBackdrop(
                    backdrop = backdrop, // Shared tab-host backdrop.
                    shape = { RectangleShape },
                    effects = { blur(TOP_EDGE_BLUR_RADIUS.toPx()) }
                )
                .drawWithContent {
                    drawContent()
                    drawRect(topBlurMask, blendMode = BlendMode.DstIn)
                }
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(TOP_EDGE_ZONE_HEIGHT + statusInset)
                .background(topScrim)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(BOTTOM_EDGE_ZONE_HEIGHT + navigationInset)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawBackdrop(
                    backdrop = backdrop, // Shared tab-host backdrop.
                    shape = { RectangleShape },
                    effects = { blur(BOTTOM_EDGE_BLUR_RADIUS.toPx()) }
                )
                .drawWithContent {
                    drawContent()
                    drawRect(bottomBlurMask, blendMode = BlendMode.DstIn)
                }
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(BOTTOM_EDGE_ZONE_HEIGHT + navigationInset)
                .background(bottomScrim)
        )
    }
}
