package com.kaii.photos.compose.app_bars

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kaii.photos.R
import com.kaii.photos.compose.app_bars.main_bars.LiquidGlassIconButton
import com.kaii.photos.datastore.AlbumType
import com.kaii.photos.domain.files.FileOperationAction
import com.kaii.photos.helpers.grid_management.SelectionManager

@Composable
fun SelectionGlassChip(
    backdrop: Backdrop,
    selectionManager: SelectionManager,
    modifier: Modifier = Modifier
) {
    val count by selectionManager.count.collectAsStateWithLifecycle(initialValue = 0)
    Row(
        modifier = modifier
            .height(46.dp)
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(percent = 50) },
                effects = {
                    blur(2.dp.toPx())
                    lens(10.dp.toPx(), 18.dp.toPx(), depthEffect = true)
                },
                highlight = { Highlight.Default },
                shadow = { Shadow(alpha = 0.3f) },
                innerShadow = { InnerShadow(radius = 5.dp, alpha = 0.16f) },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.07f)) }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = selectionManager::clear
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.close),
                contentDescription = "Clear selection",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        AnimatedContent(
            targetState = count,
            transitionSpec = {
                (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
            },
            label = "selection count"
        ) { animatedCount ->
            Text(
                text = animatedCount.toString(),
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
fun SelectionGlassTagButton(
    backdrop: Backdrop,
    showTagDialog: () -> Boolean,
    setShowTagDialog: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassIconButton(
        backdrop = backdrop,
        icon = painterResource(R.drawable.sell),
        onClick = { setShowTagDialog(!showTagDialog()) },
        contentDescription = stringResource(R.string.tags),
        modifier = modifier
    )
}

@Composable
fun SelectionTopBar(
    backdrop: Backdrop,
    selectionManager: SelectionManager,
    showTagDialog: () -> Boolean,
    setShowTagDialog: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectionGlassChip(backdrop = backdrop, selectionManager = selectionManager)
        SelectionGlassTagButton(
            backdrop = backdrop,
            showTagDialog = showTagDialog,
            setShowTagDialog = setShowTagDialog
        )
    }
}

@Composable
fun SelectionActionBar(
    backdrop: Backdrop,
    albumInfo: AlbumType,
    selectionManager: SelectionManager,
    confirmToDelete: () -> Boolean,
    doNotTrash: () -> Boolean,
    runAction: (FileOperationAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, end = 16.dp, bottom = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .drawBackdrop(
                    backdrop = backdrop, // Shared tab-host backdrop.
                    shape = { RoundedCornerShape(percent = 50) },
                    effects = {
                        blur(2.dp.toPx())
                        lens(10.dp.toPx(), 18.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Default },
                    shadow = { Shadow(alpha = 0.3f) },
                    innerShadow = { InnerShadow(radius = 5.dp, alpha = 0.16f) },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 0.07f)) }
                )
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectingBottomBarItems(
                albumInfo = albumInfo,
                selectionManager = selectionManager,
                confirmToDelete = confirmToDelete,
                doNotTrash = doNotTrash,
                runAction = runAction
            )
        }
    }
}

@Composable
fun SelectionGlassActionButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
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

    Row(
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .graphicsLayer {
                val scale = 1f + 0.1f * pressed.value
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.35f
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White.copy(alpha = 0.88f)) {
            content()
        }
    }
}
