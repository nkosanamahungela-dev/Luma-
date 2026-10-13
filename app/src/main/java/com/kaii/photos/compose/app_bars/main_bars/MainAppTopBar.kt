package com.kaii.photos.compose.app_bars.main_bars

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.kaii.photos.LocalNavController
import com.kaii.photos.R
import com.kaii.photos.compose.app_bars.DualFunctionTopAppBar
import com.kaii.photos.compose.dialogs.user_action.AlbumAddChoiceDialog
import com.kaii.photos.compose.widgets.AnimatedLoginIcon
import com.kaii.photos.compose.widgets.SelectViewTopBarLeftButtons
import com.kaii.photos.compose.widgets.SelectViewTopBarRightButtons
import com.kaii.photos.datastore.AlbumGroup
import com.kaii.photos.datastore.AlbumType
import com.kaii.photos.datastore.ImmichBasicInfo
import com.kaii.photos.helpers.AnimationConstants
import com.kaii.photos.helpers.Screens
import com.kaii.photos.helpers.grid_management.SelectionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppTopBar(
    alternate: () -> Boolean,
    selectionManager: SelectionManager,
    immichInfo: () -> ImmichBasicInfo,
    showAddAlbumButton: () -> Boolean,
    showTagDialog: () -> Boolean,
    isFromMediaPicker: Boolean,
    groups: () -> List<AlbumGroup>,
    setShowTagDialog: (show: Boolean) -> Unit,
    addAlbum: (album: AlbumType) -> Unit,
    addGroup: (name: String) -> Unit
) {
    DualFunctionTopAppBar(
        alternated = alternate(),
        title = {
            val split = stringResource(id = R.string.app_name_full).split(" ")

            Row(
                modifier = Modifier
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "${split.first()} ",
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(22f, TextUnitType.Sp)
                )

                Text(
                    text =
                        if (split.size >= 2) split[1]
                        else "",
                    fontWeight = FontWeight.Normal,
                    fontSize = TextUnit(22f, TextUnitType.Sp)
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(
                    visible = showAddAlbumButton() && !isFromMediaPicker,
                    enter = scaleIn(
                        animationSpec = AnimationConstants.expressiveSpring()
                    ),
                    exit = scaleOut(
                        animationSpec = AnimationConstants.expressiveSpring()
                    )
                ) {
                    var showAlbumTypeDialog by remember { mutableStateOf(false) }
                    if (showAlbumTypeDialog) {
                        AlbumAddChoiceDialog(
                            groups = groups(),
                            addAlbum = addAlbum,
                            addGroup = addGroup
                        ) {
                            showAlbumTypeDialog = false
                        }
                    }

                    IconButton(
                        onClick = {
                            showAlbumTypeDialog = true
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.add),
                            contentDescription = stringResource(id = R.string.album_add),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (!isFromMediaPicker) {
                    val navController = LocalNavController.current
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(48.dp)
                            .shadow(
                                elevation = 14.dp,
                                shape = CircleShape,
                                clip = false,
                                ambientColor = Color.Black.copy(alpha = 0.40f),
                                spotColor = Color.Black.copy(alpha = 0.40f)
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.24f),
                                        Color.White.copy(alpha = 0.04f),
                                        Color.White.copy(alpha = 0.10f)
                                    )
                                ),
                                CircleShape
                            )
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.035f)
                                    )
                                ),
                                CircleShape
                            )
                            .border(
                                width = 0.5.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.88f),
                                        Color.White.copy(alpha = 0.20f),
                                        Color.White.copy(alpha = 0.48f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedLoginIcon(immichInfo = immichInfo) {
                            navController.navigate(Screens.MainPages.MainGrid.SettingsDialog)
                        }
                    }
                } else {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            (context as Activity).finish()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = "Close media picker",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        alternateTitle = {
            SelectViewTopBarLeftButtons(selectionManager = selectionManager)
        },
        alternateActions = {
            SelectViewTopBarRightButtons(
                showTagDialog = showTagDialog,
                setShowTagDialog = setShowTagDialog
            )
        }
    )
}