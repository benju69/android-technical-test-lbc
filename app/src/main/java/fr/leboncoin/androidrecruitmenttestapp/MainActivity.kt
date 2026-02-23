package fr.leboncoin.androidrecruitmenttestapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.adevinta.spark.SparkTheme
import dagger.hilt.android.AndroidEntryPoint
import fr.leboncoin.androidrecruitmenttestapp.feature.albums.AlbumsViewModel
import fr.leboncoin.androidrecruitmenttestapp.feature.details.DetailsActivity
import fr.leboncoin.androidrecruitmenttestapp.navigation.MainScreenWithNavigation
import fr.leboncoin.androidrecruitmenttestapp.utils.AnalyticsHelper
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: AlbumsViewModel by viewModels()

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SparkTheme {
                MainScreenWithNavigation(
                    viewModel = viewModel,
                    onItemSelected = { album ->
                        analyticsHelper.trackSelection(album.id.toString())
                        val intent = Intent(this, DetailsActivity::class.java).apply {
                            putExtra(DetailsActivity.EXTRA_ALBUM, album)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
