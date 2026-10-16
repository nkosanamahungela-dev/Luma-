package com.kaii.photos.compose.app_bars.main_bars

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kaii.photos.R
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource

private const val SEARCH_OPEN_SPRING_DAMPING = 0.7f
private const val SEARCH_OPEN_SPRING_STIFFNESS = 420f
private const val SEARCH_GLASS_TINT_ALPHA = 0.07f
private val SEARCH_ROW_HEIGHT = 62.dp
private val SEARCH_ROW_MARGIN = 16.dp
private val SEARCH_ROW_GAP = 10.dp
private val SEARCH_TAB_WIDTH = 286.dp
private val SEARCH_ACCENT = Color(0xFF7CB4FF)
private val SEARCH_GLASS_TINT = Color.White.copy(alpha = SEARCH_GLASS_TINT_ALPHA)

@Composable
fun LiquidBottomRow(
    backdrop: Backdrop,
    searchOpen: Boolean,
    activeTabIcon: Painter,
    activeTabName: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchOpen: () -> Unit,
    onSearchClose: () -> Unit,
    onCollapsedTabClick: () -> Unit,
    onBarPressedChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(if (searchOpen) 1f else 0f) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(searchOpen) {
        progress.animateTo(
            if (searchOpen) 1f else 0f,
            spring(
                dampingRatio = SEARCH_OPEN_SPRING_DAMPING,
                stiffness = SEARCH_OPEN_SPRING_STIFFNESS
            )
        )
        if (searchOpen) focusRequester.requestFocus()
        else keyboard?.hide()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SEARCH_ROW_MARGIN)
            .graphicsLayer { alpha = if (searchOpen || progress.value > 0.001f) 1f else 0f }
            .then(if (searchOpen) Modifier.consumeAllPointerChanges(onBarPressedChanged) else Modifier)
    ) {
        val p = progress.value
        val tabWidth = SEARCH_TAB_WIDTH + (SEARCH_ROW_HEIGHT - SEARCH_TAB_WIDTH) * p
        val closeWidth = SEARCH_ROW_HEIGHT * p
        val fieldWidth = maxWidth - tabWidth - SEARCH_ROW_GAP * 2 - closeWidth

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SEARCH_ROW_GAP),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(tabWidth)
                    .height(SEARCH_ROW_HEIGHT)
                    .graphicsLayer { clip = true }
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
                        onDrawSurface = { drawRect(SEARCH_GLASS_TINT) }
                    )
                    .clickable(enabled = searchOpen && p > 0.5f) { onCollapsedTabClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().alpha(1f - p),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(activeTabIcon, contentDescription = activeTabName, tint = Color.White, modifier = Modifier.size(24.dp))
                    Text(activeTabName, color = Color.White, fontSize = 11.sp)
                }
                Icon(
                    activeTabIcon,
                    contentDescription = activeTabName,
                    tint = SEARCH_ACCENT,
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(p)
                )
            }

            Box(
                modifier = Modifier
                    .width(fieldWidth.coerceAtLeast(SEARCH_ROW_HEIGHT))
                    .height(SEARCH_ROW_HEIGHT)
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
                        onDrawSurface = { drawRect(SEARCH_GLASS_TINT) }
                    )
                    .clickable(enabled = searchOpen && p < 0.01f) { onSearchOpen() },
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(Color.White),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier.weight(1f).alpha(p)
                            .focusRequester(focusRequester),
                        decorationBox = { inner ->
                            Box {
                                if (query.isEmpty()) {
                                    Text("Search", color = Color.White.copy(alpha = 0.65f), fontSize = 16.sp)
                                }
                                inner()
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .alpha(p),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Name", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (p > 0f) {
                Box(
                    modifier = Modifier
                        .width(closeWidth)
                        .height(SEARCH_ROW_HEIGHT)
                        .graphicsLayer {
                            scaleX = 0.6f + 0.4f * p
                            scaleY = 0.6f + 0.4f * p
                            alpha = p
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
                            onDrawSurface = { drawRect(SEARCH_GLASS_TINT) }
                        )
                        .clickable { onSearchClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.close), contentDescription = "Close search", tint = Color.White)
                }
            }
        }
    }
}
