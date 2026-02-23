package fr.leboncoin.androidrecruitmenttestapp.navigation

import fr.leboncoin.androidrecruitmenttestapp.R

/**
 * Represents each tab in the bottom navigation bar.
 */
sealed class BottomNavItem(
    val title: String,
    val icon: Int,
) {
    data object Albums : BottomNavItem("Albums", R.drawable.ic_album)
    data object Favorites : BottomNavItem("Favorites", R.drawable.ic_bookmarks)
}

