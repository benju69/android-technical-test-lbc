package fr.leboncoin.androidrecruitmenttestapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.scaffold.Scaffold
import com.adevinta.spark.components.text.Text
import fr.leboncoin.androidrecruitmenttestapp.AlbumsViewModel
import fr.leboncoin.data.network.model.AlbumDto

@Composable
fun FavoritesScreen(
    viewModel: AlbumsViewModel,
    onItemSelected: (AlbumDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val favorites by viewModel.favoriteAlbums.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier) { paddingValues ->
        if (favorites.isEmpty()) {
            EmptyFavorites(paddingValues)
        } else {
            FavoritesList(
                favorites = favorites,
                paddingValues = paddingValues,
                onItemSelected = onItemSelected,
                onToggleFavorite = { albumId -> viewModel.toggleFavorite(albumId) }
            )
        }
    }
}

@Composable
private fun EmptyFavorites(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No favorites yet",
                style = SparkTheme.typography.headline2
            )
            Text(
                text = "Tap the bookmark icon to add albums to favorites",
                style = SparkTheme.typography.body2
            )
        }
    }
}

@Composable
private fun FavoritesList(
    favorites: List<AlbumDto>,
    paddingValues: PaddingValues,
    onItemSelected: (AlbumDto) -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = paddingValues,
    ) {
        items(
            items = favorites,
            key = { album -> album.id }
        ) { album ->
            AlbumItem(
                album = album,
                onItemSelected = onItemSelected,
                onToggleFavorite = onToggleFavorite
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyFavoritesPreview() {
    SparkTheme {
        EmptyFavorites(paddingValues = PaddingValues(0.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesListPreview() {
    SparkTheme {
        FavoritesList(
            favorites = listOf(
                AlbumDto(
                    albumId = 1,
                    id = 1,
                    title = "accusamus beatae ad facilis cum similique qui sunt",
                    url = "",
                    thumbnailUrl = "",
                    isFavorite = true
                ),
                AlbumDto(
                    albumId = 1,
                    id = 2,
                    title = "reprehenderit est deserunt velit ipsam",
                    url = "",
                    thumbnailUrl = "",
                    isFavorite = true
                )
            ),
            paddingValues = PaddingValues(0.dp),
            onItemSelected = {},
            onToggleFavorite = {}
        )
    }
}

