package fr.leboncoin.androidrecruitmenttestapp.feature.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.card.Card
import com.adevinta.spark.components.chips.ChipTinted
import com.adevinta.spark.components.iconbuttons.IconButtonGhost
import com.adevinta.spark.icons.BookmarkFill
import com.adevinta.spark.icons.BookmarkOutline
import com.adevinta.spark.icons.SparkIcons
import fr.leboncoin.data.network.model.AlbumDto

@OptIn(ExperimentalSparkApi::class)
@Composable
fun AlbumItem(
    album: AlbumDto,
    onItemSelected: (AlbumDto) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onClickStable = remember(album.id) { { onItemSelected(album) } }
    val onToggleFavoriteStable = remember(album.id) { { onToggleFavorite(album.id) } }

    val favoriteIcon = remember(album.isFavorite) {
        if (album.isFavorite) SparkIcons.BookmarkFill else SparkIcons.BookmarkOutline
    }
    val favoriteContentDescription = remember(album.isFavorite) {
        if (album.isFavorite) "Remove from favorites" else "Add to favorites"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 16.dp),
        onClick = onClickStable,
    ) {
        Row {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album.thumbnailUrl)
                    .httpHeaders(
                        NetworkHeaders.Builder()
                            .add("User-Agent", "LeboncoinApp/1.0")
                            .build()
                    )
                    .build(),
                contentDescription = album.title,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = album.title,
                        style = SparkTheme.typography.caption,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButtonGhost(
                        icon = favoriteIcon,
                        contentDescription = favoriteContentDescription,
                        onClick = onToggleFavoriteStable,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChipTinted(
                        text = "Album #${album.albumId}"
                    )
                    ChipTinted(
                        text = "Track #${album.id}"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumItemPreview() {
    SparkTheme {
        AlbumItem(
            album = AlbumDto(
                albumId = 1,
                id = 1,
                title = "accusamus beatae ad facilis cum similique qui sunt",
                url = "",
                thumbnailUrl = "",
                isFavorite = false
            ),
            onItemSelected = {},
            onToggleFavorite = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumItemLongTitlePreview() {
    SparkTheme {
        AlbumItem(
            album = AlbumDto(
                albumId = 42,
                id = 123,
                title = "This is a very long title that should overflow and be truncated after two lines to demonstrate the ellipsis behavior",
                url = "",
                thumbnailUrl = "",
                isFavorite = true
            ),
            onItemSelected = {},
            onToggleFavorite = {}
        )
    }
}

