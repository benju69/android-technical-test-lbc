package fr.leboncoin.androidrecruitmenttestapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.adevinta.spark.components.scaffold.Scaffold
import fr.leboncoin.androidrecruitmenttestapp.AlbumsViewModel
import fr.leboncoin.androidrecruitmenttestapp.R
import fr.leboncoin.data.network.model.AlbumDto

sealed class BottomNavItem(
    val title: String,
    val icon: Int
) {
    object Albums : BottomNavItem("Albums", R.drawable.ic_album)
    object Favorites : BottomNavItem("Favorites", R.drawable.ic_bookmarks)
}

@Composable
fun MainScreenWithNavigation(
    viewModel: AlbumsViewModel,
    onItemSelected: (AlbumDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val albumsListState = rememberLazyListState()
    val favoritesListState = rememberLazyListState()

    val items = listOf(BottomNavItem.Albums, BottomNavItem.Favorites)

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.title
                            )
                        },
                        label = { Text(text = item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> AlbumsScreen(
                viewModel = viewModel,
                onItemSelected = onItemSelected,
                listState = albumsListState,
                modifier = Modifier.padding(paddingValues)
            )
            1 -> FavoritesScreen(
                viewModel = viewModel,
                onItemSelected = onItemSelected,
                listState = favoritesListState,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

