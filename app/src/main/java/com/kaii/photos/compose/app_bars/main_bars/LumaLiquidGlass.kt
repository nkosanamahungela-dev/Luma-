package com.kaii.photos.compose.app_bars.main_bars

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow

/** Shared real Backdrop layer for the Home glass surfaces. */
fun Modifier.lumaLiquidGlassSurface(
    backdrop: Backdrop,
    shape: Shape,
    fillAlpha: Float = 0.07f,
    blurRadius: Dp = 22.dp,
    lensWidth: Dp = 18.dp,
    lensHeight: Dp = 24.dp
): Modifier = drawBackdrop(
    backdrop = backdrop,
    shape = { shape },
    effects = {
        blur(blurRadius.toPx())
        colorControls(brightness = 0.15f, saturation = 2f)
        lens(lensWidth.toPx(), lensHeight.toPx())
    },
    highlight = {
        Highlight.Default.copy(
            color = Color.White.copy(alpha = 0.44f),
            angle = 135f,
            falloff = 0.85f
        )
    },
    shadow = {
        Shadow(
            radius = 18.dp,
            offset = DpOffset(0.dp, 8.dp),
            color = Color.Black.copy(alpha = 0.26f)
        )
    },
    innerShadow = {
        InnerShadow(
            radius = 8.dp,
            offset = DpOffset(0.dp, 1.dp),
            color = Color.White.copy(alpha = 0.16f),
            alpha = 0.42f
        )
    },
    onDrawSurface = {
        drawRect(Color.White.copy(alpha = fillAlpha))
    },
    onDrawFront = {
        // Inner-edge specular glints: bright upper-left, soft lower-right.
        drawLine(
            color = Color.White.copy(alpha = 0.38f),
            start = Offset(10.dp.toPx(), 1.dp.toPx()),
            end = Offset(size.width * 0.34f, 1.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.16f),
            start = Offset(size.width * 0.68f, size.height - 1.dp.toPx()),
            end = Offset(size.width - 10.dp.toPx(), size.height - 1.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
    }
).border(
    width = 0.5.dp,
    brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.52f),
            Color.White.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.28f)
        )
    ),
    shape = shape
)
