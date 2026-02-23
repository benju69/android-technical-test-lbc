package fr.leboncoin.androidrecruitmenttestapp.feature.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.buttons.ButtonFilled
import com.adevinta.spark.components.progress.Spinner
import com.adevinta.spark.components.scaffold.Scaffold
import com.adevinta.spark.components.text.Text
import fr.leboncoin.data.network.model.AlbumDto

@Composable
fun AlbumsScreen(
    viewModel: AlbumsViewModel,
    onItemSelected: (AlbumDto) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier) { paddingValues ->
        when (val state = uiState) {
            is AlbumsUiState.Loading -> {
                AlbumsLoading(paddingValues)
            }

            is AlbumsUiState.Success -> {
                AlbumsSuccess(
                    albums = state.albums,
                    paddingValues = paddingValues,
                    listState = listState,
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refreshAlbums() },
                    onItemSelected = onItemSelected,
                    onToggleFavorite = { albumId -> viewModel.toggleFavorite(albumId) }
                )
            }

            is AlbumsUiState.Error -> {
                AlbumsError(
                    message = state.message,
                    paddingValues = paddingValues,
                    onRetry = { viewModel.loadAlbums() }
                )
            }
        }
    }
}

@Composable
private fun AlbumsLoading(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Spinner()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumsSuccess(
    albums: List<AlbumDto>,
    paddingValues: PaddingValues,
    listState: LazyListState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onItemSelected: (AlbumDto) -> Unit,
    onToggleFavorite: (Int) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                items = albums,
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
}

@Composable
private fun AlbumsError(
    message: String,
    paddingValues: PaddingValues,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Error: $message")
            ButtonFilled(
                text = "Retry",
                onClick = onRetry
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumsLoadingPreview() {
    SparkTheme {
        AlbumsLoading(paddingValues = PaddingValues(0.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumsSuccessPreview() {
    SparkTheme {
        AlbumsSuccess(
            albums = listOf(
                AlbumDto(
                    albumId = 1,
                    id = 1,
                    title = "accusamus beatae ad facilis cum similique qui sunt",
                    url = "",
                    thumbnailUrl = ""
                ),
                AlbumDto(
                    albumId = 1,
                    id = 2,
                    title = "reprehenderit est deserunt velit ipsam",
                    url = "",
                    thumbnailUrl = ""
                ),
            ),
            paddingValues = PaddingValues(0.dp),
            listState = rememberLazyListState(),
            isRefreshing = false,
            onRefresh = {},
            onItemSelected = {},
            onToggleFavorite = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumsErrorPreview() {
    SparkTheme {
        AlbumsError(
            message = "Network connection failed",
            paddingValues = PaddingValues(0.dp),
            onRetry = {}
        )
    }
}

