package fr.leboncoin.androidrecruitmenttestapp.feature.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.data.network.model.AlbumDto
import fr.leboncoin.data.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val repository: AlbumRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumsUiState>(AlbumsUiState.Loading)
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val favoriteAlbums: StateFlow<List<AlbumDto>> = repository.getFavoriteAlbums()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadAlbums()
    }

    fun loadAlbums() {
        viewModelScope.launch {
            _uiState.value = AlbumsUiState.Loading

            repository.getAlbumsWithCache().collect { result ->
                result.fold(
                    onSuccess = { albums ->
                        _uiState.value = AlbumsUiState.Success(albums)
                    },
                    onFailure = { e ->
                        _uiState.value = AlbumsUiState.Error(
                            "Something happened: ${e.message}"
                        )
                    }
                )
            }
        }

        // Observe cached data for real-time updates (like favorite changes)
        viewModelScope.launch {
            repository.getCachedAlbums().collect { albums ->
                if (albums.isNotEmpty()) {
                    _uiState.value = AlbumsUiState.Success(albums)
                }
            }
        }
    }

    fun refreshAlbums() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshAlbums()
            _isRefreshing.value = false
        }
    }

    fun toggleFavorite(albumId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(albumId)
        }
    }

    fun setFavorite(albumId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.setFavorite(albumId, isFavorite)
        }
    }

    suspend fun isFavorite(albumId: Int): Boolean {
        return repository.isFavorite(albumId)
    }
}

