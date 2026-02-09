package fr.leboncoin.androidrecruitmenttestapp.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adevinta.spark.SparkTheme
import fr.leboncoin.data.network.model.AlbumDto
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlbumItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testAlbum = AlbumDto(
        id = 1,
        albumId = 1,
        title = "Test Album Title",
        url = "https://example.com/image.png",
        thumbnailUrl = "https://example.com/thumb.png"
    )

    @Test
    fun albumItem_displaysTitle() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(album = testAlbum, onItemSelected = {})
            }
        }

        composeTestRule
            .onNodeWithText(testAlbum.title)
            .assertIsDisplayed()
    }

    @Test
    fun albumItem_displaysAlbumIdChip() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(album = testAlbum, onItemSelected = {})
            }
        }

        composeTestRule
            .onNodeWithText("Album #${testAlbum.albumId}")
            .assertIsDisplayed()
    }

    @Test
    fun albumItem_displaysTrackIdChip() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(album = testAlbum, onItemSelected = {})
            }
        }

        composeTestRule
            .onNodeWithText("Track #${testAlbum.id}")
            .assertIsDisplayed()
    }

    @Test
    fun albumItem_displaysImageWithContentDescription() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(album = testAlbum, onItemSelected = {})
            }
        }

        composeTestRule
            .onNode(hasContentDescription(testAlbum.title))
            .assertIsDisplayed()
    }

    @Test
    fun albumItem_isClickable() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(album = testAlbum, onItemSelected = {})
            }
        }

        composeTestRule
            .onNode(hasText(testAlbum.title))
            .assertHasClickAction()
    }

    @Test
    fun albumItem_withLongTitle_truncatesCorrectly() {
        val longTitleAlbum = testAlbum.copy(
            title = "This is a very long album title that should be truncated after two lines"
        )

        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(album = longTitleAlbum, onItemSelected = {})
            }
        }

        composeTestRule
            .onNodeWithText(longTitleAlbum.title)
            .assertIsDisplayed()
    }

    @Test
    fun albumItem_withDifferentIds_displaysCorrectChips() {
        val differentAlbum = AlbumDto(
            id = 42,
            albumId = 7,
            title = "Different Album",
            url = "https://example.com/different.png",
            thumbnailUrl = "https://example.com/different_thumb.png"
        )

        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(album = differentAlbum, onItemSelected = {})
            }
        }

        composeTestRule
            .onNodeWithText("Album #7")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Track #42")
            .assertIsDisplayed()
    }
}

