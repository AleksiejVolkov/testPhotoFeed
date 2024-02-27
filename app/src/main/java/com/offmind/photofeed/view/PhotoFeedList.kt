package com.offmind.photofeed.view

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter.State.Error
import coil.compose.AsyncImagePainter.State.Loading
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.offmind.photofeed.model.Photo
import com.offmind.photofeed.viewmodel.PhotoFeedViewModel
import com.offmind.photofeed.viewmodel.ScreenState
import com.offmind.photofeed.viewmodel.UserEvent


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PhotoFeedScreen(vm: PhotoFeedViewModel) {
    val viewState = vm.viewState.collectAsState(initial = ScreenState())

    val photosFlow = viewState.value.photos.collectAsLazyPagingItems()
    val lazyListState = rememberLazyGridState()

    val showExpandedPhoto = rememberSaveable(viewState.value.selectedPhoto) {
        mutableStateOf(viewState.value.selectedPhoto != null)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {
        viewState.value.selectedPhoto?.let {
            AnimatedVisibility(showExpandedPhoto.value) {
                FullscreenPhoto(
                    photo = it,
                    hasConnection = viewState.value.hasConnection
                )
            }
            BackHandler {
                vm.onUserEvent(UserEvent.OnPhotoClosed)
            }
        }

        AnimatedVisibility(showExpandedPhoto.value.not()) {
            val state = rememberPullRefreshState(
                viewState.value.refreshing,
                { vm.onUserEvent(UserEvent.OnRefresh) })

            Box(Modifier.pullRefresh(state)) {
                PhotoFeedList(
                    photos = photosFlow,
                    listState = lazyListState,
                    onUserEvent = vm::onUserEvent
                )
                PullRefreshIndicator(viewState.value.refreshing, state, Modifier.align(Alignment.TopCenter))
                if (viewState.value.error) {
                    Text(
                        text = "No internet connection",
                        fontSize = 25.sp,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoFeedList(
    photos: LazyPagingItems<Photo>,
    listState: LazyGridState,
    onUserEvent: (UserEvent) -> Unit
) {
    val config = LocalConfiguration.current

    val isLandscape = remember(config.orientation) {
        config.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        columns = GridCells.Fixed(if (isLandscape) 3 else 2),
        content = {
            items(photos.itemCount)
            { index ->
                photos[index]?.let { photoItem ->
                    PhotoCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.8f),
                        photo = photoItem
                    ) {
                        onUserEvent(UserEvent.OnPhotoSelected(it))
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoCard(
    modifier: Modifier = Modifier,
    photo: Photo,
    onClick: (photo: Photo) -> Unit = { }
) {
    Card(
        modifier = modifier
            .padding(12.dp)
            .shadow(8.dp, shape = RoundedCornerShape(4.dp)),
        onClick = {
            onClick(photo)
        }) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = photo.src.medium,
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
            Text(
                text = photo.photographer,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .background(MaterialTheme.colors.surface.copy(alpha = 0.8f))
                    .padding(5.dp),
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun FullscreenPhoto(
    photo: Photo,
    hasConnection: Boolean
) {
    val config = LocalConfiguration.current
    val context = LocalContext.current

    val isLandscape = remember(config.orientation) {
        config.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(if (isLandscape) photo.src.landscape else photo.src.portrait)
            .build()
    )

    Image(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        painter = imagePainter,
        contentScale = ContentScale.Fit,
        contentDescription = null,
    )
    when (imagePainter.state) {
        is Loading -> {
            StatusMessage("Loading", isError = false)
        }

        is Error -> {
            StatusMessage("Failed to load photo", isError = true)
            LaunchedEffect(key1 = hasConnection) {
                if (hasConnection) {
                    imagePainter.onForgotten()
                    imagePainter.onRemembered()
                }
            }
        }

        else -> {
            // Do nothing
        }
    }
}

@Composable
private fun StatusMessage(message: String, isError: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 24.sp,
            color = if (isError) MaterialTheme.colors.error else MaterialTheme.colors.onSurface
        )
    }
}