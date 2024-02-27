package com.offmind.photofeed.viewmodel

import com.offmind.photofeed.model.Photo

sealed class UserEvent {
    class OnPhotoSelected(val photo: Photo) : UserEvent()
    data object OnPhotoClosed : UserEvent()
    data object OnRefresh : UserEvent()
}