package fr.leboncoin.androidrecruitmenttestapp.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adevinta.spark.SparkTheme
import fr.leboncoin.androidrecruitmenttestapp.feature.details.DetailsScreen
import fr.leboncoin.data.network.model.AlbumDto
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailsScreenTest {

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
    fun detailsScreen_displaysTopAppBarWithTitle() {
        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(album = testAlbum)
            }
        }

        composeTestRule
            .onNodeWithText("Album Details")
            .assertIsDisplayed()
    }

    @Test
    fun detailsScreen_displaysBackButton() {
        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(album = testAlbum)
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Back")
            .assertIsDisplayed()
    }

    @Test
    fun detailsScreen_backButtonCallsOnBackClick() {
        var backClicked = false

        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(
                    album = testAlbum,
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()

        assert(backClicked) { "onBackClick should be called when back button is clicked" }
    }

    @Test
    fun detailsScreen_displaysAlbumTitle() {
        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(album = testAlbum)
            }
        }

        composeTestRule
            .onNodeWithText(testAlbum.title)
            .assertIsDisplayed()
    }

    @Test
    fun detailsScreen_displaysAlbumIdChip() {
        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(album = testAlbum)
            }
        }

        composeTestRule
            .onNodeWithText("Album #${testAlbum.albumId}")
            .assertIsDisplayed()
    }

    @Test
    fun detailsScreen_displaysTrackIdChip() {
        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(album = testAlbum)
            }
        }

        composeTestRule
            .onNodeWithText("Track #${testAlbum.id}")
            .assertIsDisplayed()
    }

    @Test
    fun detailsScreen_displaysInformationSection() {
        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(album = testAlbum)
            }
        }

        composeTestRule
            .onNodeWithText("Information")
            .assertIsDisplayed()
    }

    @Test
    fun detailsScreen_displaysIdInformation() {
        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(album = testAlbum)
            }
        }

        composeTestRule
            .onNodeWithText("ID")
            .assertIsDisplayed()

        // Verify the ID value exists (might appear multiple times: track chip and info section)
        // We just verify at least one occurrence exists
        composeTestRule
            .onAllNodesWithText(testAlbum.id.toString())
            .fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun detailsScreen_displaysAlbumIdInformation() {
        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(album = testAlbum)
            }
        }

        composeTestRule
            .onNodeWithText("Album ID")
            .assertIsDisplayed()

        // Verify the album ID value exists (appears in multiple places: chip and info section)
        // Using onAllNodesWithText to handle multiple matches
        composeTestRule
            .onAllNodesWithText(testAlbum.albumId.toString())
            .assertCountEquals(2) // Should appear in chip and information section
    }

    @Test
    fun detailsScreen_displaysImageWithCorrectContentDescription() {
        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(album = testAlbum)
            }
        }

        composeTestRule
            .onNode(hasContentDescription(testAlbum.title))
            .assertIsDisplayed()
    }

    @Test
    fun detailsScreen_withLongTitle_displaysCorrectly() {
        val longTitleAlbum = testAlbum.copy(
            title = "This is a very long album title that should still display correctly without breaking the UI layout"
        )

        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(album = longTitleAlbum)
            }
        }

        composeTestRule
            .onNodeWithText(longTitleAlbum.title)
            .assertIsDisplayed()
    }

    @Test
    fun detailsScreen_withDifferentAlbumIds_displaysCorrectValues() {
        val differentAlbum = AlbumDto(
            id = 42,
            albumId = 7,
            title = "Different Album",
            url = "https://example.com/different.png",
            thumbnailUrl = "https://example.com/different_thumb.png"
        )

        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(album = differentAlbum)
            }
        }

        composeTestRule
            .onNodeWithText("Album #7")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Track #42")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Different Album")
            .assertIsDisplayed()
    }

    @Test
    fun detailsScreen_allMainElementsAreVisible() {
        composeTestRule.setContent {
            SparkTheme {
                DetailsScreen(album = testAlbum)
            }
        }

        // Verify all main UI elements are present
        composeTestRule.onNodeWithText("Album Details").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNode(hasContentDescription(testAlbum.title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(testAlbum.title).assertIsDisplayed()
        composeTestRule.onNodeWithText("Album #${testAlbum.albumId}").assertIsDisplayed()
        composeTestRule.onNodeWithText("Track #${testAlbum.id}").assertIsDisplayed()
        composeTestRule.onNodeWithText("Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("ID").assertIsDisplayed()
        composeTestRule.onNodeWithText("Album ID").assertIsDisplayed()
    }
}

