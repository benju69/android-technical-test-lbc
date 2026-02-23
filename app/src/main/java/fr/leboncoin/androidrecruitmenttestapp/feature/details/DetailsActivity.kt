package fr.leboncoin.androidrecruitmenttestapp.feature.details

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.image.Illustration
import dagger.hilt.android.AndroidEntryPoint
import fr.leboncoin.androidrecruitmenttestapp.R
import fr.leboncoin.androidrecruitmenttestapp.utils.AnalyticsHelper
import fr.leboncoin.data.network.model.AlbumDto
import javax.inject.Inject

@AndroidEntryPoint
class DetailsActivity : ComponentActivity() {

    private val viewModel: DetailsViewModel by viewModels()

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        analyticsHelper.trackScreenView("Details")

        val album = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_ALBUM, AlbumDto::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_ALBUM)
        }

        setContent {
            SparkTheme {
                if (album != null) {
                    LaunchedEffect(album.id) {
                        viewModel.setAlbum(album)
                    }
                    DetailsScreen(
                        viewModel = viewModel,
                        onBackClick = { finish() }
                    )
                } else {
                    Illustration(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(id = R.drawable.work_in_progress),
                        contentDescription = null,
                        contentScale = ContentScale.Inside,
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_ALBUM = "extra_album"
    }
}

