package com.offmind.photofeed.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.offmind.photofeed.BuildConfig
import com.offmind.photofeed.model.Photo
import com.offmind.photofeed.network.PexelsApiService

class PhotoPagingSource(
    private val service: PexelsApiService
) : PagingSource<Int, Photo>() {

    companion object {
        private const val STARTING_PAGE_INDEX = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val position = params.key ?: STARTING_PAGE_INDEX

        return try {
            val response = service.getCuratedPhotos(apiKey = BuildConfig.PEXELS_API_KEY, perPage = 5, page = position)
            val photos = response.photos

            LoadResult.Page(
                data = photos,
                prevKey = if (position == STARTING_PAGE_INDEX) null else position - 1,
                nextKey = if (photos.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int =
        ((state.anchorPosition ?: 0) - state.config.initialLoadSize / 2)
            .coerceAtLeast(0)
}