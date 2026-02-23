package fr.leboncoin.androidrecruitmenttestapp.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import fr.leboncoin.androidrecruitmenttestapp.feature.albums.AlbumsScreen
import fr.leboncoin.androidrecruitmenttestapp.feature.albums.AlbumsViewModel
import fr.leboncoin.androidrecruitmenttestapp.feature.favorites.FavoritesScreen
import fr.leboncoin.data.network.model.AlbumDto

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
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
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

