package fr.leboncoin.androidrecruitmenttestapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.chips.ChipTinted
import fr.leboncoin.androidrecruitmenttestapp.R
import fr.leboncoin.data.network.model.AlbumDto

@OptIn(ExperimentalSparkApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    album: AlbumDto,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Album Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album.url)
                    .httpHeaders(
                        NetworkHeaders.Builder()
                            .add("User-Agent", "LeboncoinApp/1.0")
                            .build()
                    )
                    .crossfade(true)
                    .build(),
                contentDescription = album.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = album.title,
                style = SparkTheme.typography.headline1,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Informations
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipTinted(
                    text = "Album #${album.albumId}"
                )
                ChipTinted(
                    text = "Track #${album.id}"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Détails supplémentaires
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Information",
                    style = SparkTheme.typography.headline2,
                )

                DetailRow(label = "ID", value = album.id.toString())
                DetailRow(label = "Album ID", value = album.albumId.toString())
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = SparkTheme.typography.caption,
            color = SparkTheme.colors.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = SparkTheme.typography.body2,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsScreenPreview() {
    SparkTheme {
        DetailsScreen(
            album = AlbumDto(
                albumId = 1,
                id = 1,
                title = "accusamus beatae ad facilis cum similique qui sunt",
                url = "https://placehold.co/600x600/92c952/white/png",
                thumbnailUrl = "https://placehold.co/150x150/92c952/white/png"
            )
        )
    }
}
