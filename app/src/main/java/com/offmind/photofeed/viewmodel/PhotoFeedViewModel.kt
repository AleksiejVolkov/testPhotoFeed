package com.offmind.photofeed.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.offmind.photofeed.model.Photo
import com.offmind.photofeed.network.PexelsApiService
import com.offmind.photofeed.repository.PhotoPagingSource
import com.offmind.photofeed.utility.connectivityStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PhotoFeedViewModel(
    private val application: Application,
    private val photoRepository: PexelsApiService
) : AndroidViewModel(application = application) {

    private val _state: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState())
    val viewState: SharedFlow<ScreenState> = _state

    private var currentPager: Pager<Int, Photo>? = null

    init {
        observeNetworkConnectivity()
        refresh()
    }

    private fun observeNetworkConnectivity() {
        viewModelScope.launch {
            application.connectivityStatus().collectLatest { status ->
                _state.value = _state.value.copy(hasConnection = status)
            }
        }
    }

    private fun createPager(): Flow<PagingData<Photo>> {
        val newPager = Pager(
            config = PagingConfig(enablePlaceholders = true, pageSize = 10),
            pagingSourceFactory = { PhotoPagingSource(photoRepository) }
        )
        currentPager = newPager
        return newPager.flow.cachedIn(viewModelScope)
    }

    private fun refresh() {
        viewModelScope.launch {
            if (_state.value.hasConnection.not()) {
                _state.value = _state.value.copy(error = true, photos = emptyFlow())
                return@launch
            }
            _state.value = _state.value.copy(refreshing = true, error = false)
            _state.value = _state.value.copy(photos = createPager(), refreshing = false)
        }
    }

    fun onUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.OnPhotoSelected -> {
                _state.value = _state.value.copy(selectedPhoto = event.photo)
            }

            is UserEvent.OnPhotoClosed -> {
                _state.value = _state.value.copy(selectedPhoto = null)
            }

            is UserEvent.OnRefresh -> {
                refresh()
            }
        }
    }
}

data class ScreenState(
    val photos: Flow<PagingData<Photo>> = emptyFlow(),
    val selectedPhoto: Photo? = null,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val hasConnection: Boolean = true
)