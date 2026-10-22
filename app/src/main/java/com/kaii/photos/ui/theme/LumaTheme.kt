package com.kaii.photos.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pixel Photos visual tokens. Screen composables should reference these values
 * instead of defining local colors, type sizes, spacing, or corner radii.
 */
object LumaTheme {
    val Background = Color(0xFF0B0E1A)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0x99FFFFFF)
    val TextTertiary = Color(0x66FFFFFF)
    val Accent = Color(0xFF7CB4FF)

    val Surface = Color(0x0FFFFFFF) // white at 6%
    val Divider = Color(0x14FFFFFF) // white at 8%
    val RowPressed = Color(0x14FFFFFF) // white at 8%
    val VideoScrim = Color(0x59000000) // black at 35%
    val SecureRingOuter = Color(0x0AFFFFFF) // white at 4%
    val SecureRingMiddle = Color(0x0FFFFFFF) // white at 6%
    val SecureRingInner = Color(0x17FFFFFF) // white at 9%
    val SecureYellow = Color(0xFFF2C94C)
    val SecurePink = Color(0xFFE84A8A)
    val SecureBlue = Color(0xFF4F9CF0)

    val ScreenMargin = 16.dp
    val GridUnit = 4.dp
    val SectionGap = 22.dp
    val TitleRowHeight = 46.dp
    val LargeTitle = 34.sp
    val SectionTitle = 20.sp
    val RowTitle = 16.sp
    val DateHeader = 20.sp
    val Caption = 15.sp
    val Small = 13.sp

    // Pixel Photos timeline: one wide hero, two medium tiles, then square thumbnails.
    val PhotoHeroHeight = 220.dp
    val PhotoHeroRadius = 24.dp
    val PhotoPairHeight = 176.dp
    val PhotoPairRadius = 20.dp
    val PhotoGridRadius = 14.dp
    val AlbumCoverRadius = 20.dp
    val CardRadius = 24.dp
    val PinnedTileHeight = 96.dp
    val PinnedTileRadius = 22.dp
    val GlassButtonSize = 46.dp
    val GlassIconSize = 22.dp
    val BarIconSize = 24.dp
    val TabIconSize = 26.dp
    val SecureLockIconSize = 52.dp
}
