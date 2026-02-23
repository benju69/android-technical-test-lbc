package fr.leboncoin.androidrecruitmenttestapp.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.data.network.model.AlbumDto
import fr.leboncoin.data.repository.AlbumRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModel @Inject constructor(
    private val repository: AlbumRepository,
) : ViewModel() {

    private val _albumId = MutableStateFlow<Int?>(null)

    private val _album = MutableStateFlow<AlbumDto?>(null)
    val album: StateFlow<AlbumDto?> = _album.asStateFlow()

    init {
        _albumId
            .filterNotNull()
            .flatMapLatest { id -> repository.observeAlbumById(id) }
            .onEach { updatedAlbum -> _album.value = updatedAlbum }
            .launchIn(viewModelScope)
    }

    fun setAlbum(albumDto: AlbumDto) {
        _album.value = albumDto
        _albumId.value = albumDto.id
    }

    fun toggleFavorite() {
        val currentAlbum = _album.value ?: return
        viewModelScope.launch {
            repository.toggleFavorite(currentAlbum.id)
        }
    }
}
