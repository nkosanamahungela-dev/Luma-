package com.kaii.photos.compose.app_bars.main_bars

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
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

private val LumaBarColor = Color(0xFF0B0E1A)
private val LumaTitleRowHeight = 76.dp
private val LumaBottomFadeHeight = 10.dp
private val LumaStatusFadeHeight = 8.dp

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
    titleRowOffsetPx: Float = 0f,
    leading: @Composable () -> Unit = {
        Text(
            text = "Luma",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 22.dp)
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
    }
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
        Box(Modifier.fillMaxWidth().height(topInset).background(LumaBarColor))
        Box(
            Modifier
                .fillMaxWidth()
                .height(LumaStatusFadeHeight)
                .offset(y = topInset)
                .drawWithContent {
                    drawRect(Brush.verticalGradient(listOf(LumaBarColor, LumaBarColor.copy(alpha = 0f))))
                }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(LumaTitleRowHeight)
                .offset(y = topInset)
                .graphicsLayer {
                    translationY = titleRowOffsetPx
                    alpha = (1f + titleRowOffsetPx / with(density) { LumaTitleRowHeight.toPx() }).coerceIn(0f, 1f)
                }
        ) {
            // The opaque title-row surface moves with its controls.
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LumaTitleRowHeight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LumaTitleRowHeight),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) { leading() }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) { trailing() }
                }
            }
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
    }
}
