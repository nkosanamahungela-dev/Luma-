package com.kaii.photos.compose.widgets.albums

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kaii.photos.R

private val CategoryGlassTint = Color.White.copy(alpha = 0.07f)

@Composable
fun CategoryList(
    backdrop: Backdrop,
    navigateToTrash: () -> Unit,
    navigateToFavourites: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryGlassPill(
            backdrop = backdrop,
            icon = R.drawable.favourite,
            label = stringResource(R.string.favourites),
            onClick = navigateToFavourites,
            modifier = Modifier.weight(1f)
        )
        CategoryGlassPill(
            backdrop = backdrop,
            icon = R.drawable.trash,
            label = stringResource(R.string.trash_bin_short),
            onClick = navigateToTrash,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CategoryGlassPill(
    backdrop: Backdrop,
    icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .height(52.dp)
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
                onDrawSurface = { drawRect(CategoryGlassTint) }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

