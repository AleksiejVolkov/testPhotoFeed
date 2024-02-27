package com.offmind.photofeed

import android.app.Application
import coil.Coil
import coil.ImageLoader
import com.offmind.photofeed.di.myModules
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class PhotoFeedApplication : Application() {

    private val imageLoader: ImageLoader by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PhotoFeedApplication)
            modules(myModules)
        }

        Coil.setImageLoader(imageLoader)
    }
}