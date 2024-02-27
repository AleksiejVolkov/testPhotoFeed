package com.offmind.photofeed.di

import coil.ImageLoader
import com.offmind.photofeed.BuildConfig
import com.offmind.photofeed.network.PexelsApiService
import com.offmind.photofeed.viewmodel.PhotoFeedViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val myModules = module {

    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.PEXELS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(PexelsApiService::class.java)
    }

    viewModel {
        PhotoFeedViewModel(
            application = get(),
            get()
        )
    }

    single {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    single {
        ImageLoader.Builder(androidContext())
            .okHttpClient(okHttpClient = get())
            .build()
    }
}