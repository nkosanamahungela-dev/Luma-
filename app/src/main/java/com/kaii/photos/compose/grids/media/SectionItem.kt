package com.kaii.photos.compose.grids.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.kaii.photos.compose.widgets.ShowSelectedState
import com.kaii.photos.helpers.paging.PhotoLibraryUIModel
import com.kaii.photos.ui.theme.LumaTheme

@Composable
fun SectionItem(
    item: PhotoLibraryUIModel.Section,
    selected: () -> Boolean,
    isSelecting: () -> Boolean,
    prefix: String,
    toggleSelection: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(1f)
            .height(64.dp)
            .background(Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = toggleSelection
            )
            .padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Text(
            text = prefix + item.title,
            fontSize = LumaTheme.DateHeader,
            fontWeight = FontWeight.Bold,
            color = LumaTheme.TextPrimary,
            modifier = Modifier
                .align(Alignment.CenterStart)
        )

        ShowSelectedState(
            isSelected = selected,
            showIcon = isSelecting(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
        )
    }
}