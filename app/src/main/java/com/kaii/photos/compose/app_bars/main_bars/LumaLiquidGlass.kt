package com.kaii.photos.compose.app_bars.main_bars
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer

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
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow

/** Shared real Backdrop layer for the Home glass surfaces. */
fun Modifier.lumaLiquidGlassSurface(
    backdrop: Backdrop,
    shape: Shape,
    fillAlpha: Float = 0.07f,
    blurRadius: Dp = 3.dp,
    lensWidth: Dp = 18.dp,
    lensHeight: Dp = 24.dp
): Modifier = drawBackdrop(
    backdrop = backdrop,
    shape = { shape },
    effects = {
        vibrancy()
        blur(blurRadius.toPx())
        colorControls(brightness = 0.15f, saturation = 2f)
        lens(lensWidth.toPx(), lensHeight.toPx())
    },
    highlight = {
        Highlight.Default.copy(
            width = 0.8.dp,
            blurRadius = 1.2.dp,
            alpha = 1f,
            style = HighlightStyle.Default(
                color = Color.White.copy(alpha = 0.44f),
                angle = 135f,
                falloff = 0.85f
            )
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


@androidx.compose.runtime.Composable
fun LiquidGlassIconButton(
    backdrop: Backdrop,
    icon: androidx.compose.ui.graphics.painter.Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .graphicsLayer {
                    val scale = 1f + 0.1f * pressed.value
                    scaleX = scale
                    scaleY = scale
                }
                .drawBackdrop(
                    backdrop = backdrop, // Shared tab-host backdrop.
                    shape = { CircleShape },
                    effects = {
                        blur(2.dp.toPx())
                        lens(10.dp.toPx(), 18.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Default },
                    shadow = { Shadow(alpha = 0.3f) },
                    innerShadow = { InnerShadow(radius = 5.dp, alpha = 0.16f) },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 0.07f)) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
