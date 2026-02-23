package fr.leboncoin.androidrecruitmenttestapp.feature.albums

import fr.leboncoin.data.network.model.AlbumDto

/**
 * Represents the UI state of the Albums screen.
 */
sealed interface AlbumsUiState {
    data object Loading : AlbumsUiState
    data class Success(val albums: List<AlbumDto>) : AlbumsUiState
    data class Error(val message: String) : AlbumsUiState
}

