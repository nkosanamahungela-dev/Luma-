package com.kaii.photos.compose.app_bars.secure_folder

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.Backdrop
import com.kaii.photos.LocalNavController
import com.kaii.photos.R
import com.kaii.photos.compose.app_bars.SelectionGlassChip
import com.kaii.photos.compose.app_bars.SelectionGlassTagButton
import com.kaii.photos.compose.app_bars.main_bars.LiquidGlassIconButton
import com.kaii.photos.compose.app_bars.main_bars.LumaTopBar
import com.kaii.photos.helpers.Screens
import com.kaii.photos.helpers.grid_management.SelectionManager

@Composable
fun SecureFolderViewTopAppBar(
    isLoading: () -> Boolean,
    selectionManager: SelectionManager,
    onBackClicked: () -> Unit,
    backdrop: Backdrop,
    showTagDialog: () -> Boolean,
    setShowTagDialog: (Boolean) -> Unit,
    topBarOffsetPx: () -> Float = { 0f }
) {
    val isSelecting by selectionManager.enabled.collectAsStateWithLifecycle(initialValue = false)
    val navController = LocalNavController.current

    LumaTopBar(
        backdrop = backdrop,
        titleRowOffsetPx = topBarOffsetPx,
        trailing = {},
        leading = {
            AnimatedContent(
                targetState = isSelecting,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { 24 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -24 })
                    } else {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { 24 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -24 })
                    }
                },
                label = "secure title selection swap"
            ) { selecting ->
                if (!selecting) {
                    Text(
                        text = stringResource(id = R.string.secure_folder),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }
            }
        },
        floatingLeading = {
            AnimatedContent(
                targetState = isSelecting,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { 24 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -24 })
                    } else {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { 24 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -24 })
                    }
                },
                label = "secure back selection swap"
            ) { selecting ->
                if (selecting) {
                    SelectionGlassChip(backdrop, selectionManager)
                } else {
                    LiquidGlassIconButton(
                        backdrop = backdrop,
                        icon = painterResource(R.drawable.back_arrow),
                        contentDescription = stringResource(R.string.return_to_previous_page),
                        onClick = onBackClicked,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        floatingTrailing = {
            AnimatedContent(
                targetState = isSelecting,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { 24 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -24 })
                    } else {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { 24 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -24 })
                    }
                },
                label = "secure actions selection swap"
            ) { selecting ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selecting) {
                        SelectionGlassTagButton(
                            backdrop = backdrop,
                            showTagDialog = showTagDialog,
                            setShowTagDialog = setShowTagDialog,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    } else {
                        if (isLoading()) {
                            ContainedLoadingIndicator(modifier = Modifier.padding(end = 8.dp))
                        }
                        LiquidGlassIconButton(
                            backdrop = backdrop,
                            icon = painterResource(R.drawable.settings),
                            contentDescription = "Settings",
                            onClick = { navController.navigate(Screens.MainPages.MainGrid.SettingsDialog) },
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }
        }
    )
}
