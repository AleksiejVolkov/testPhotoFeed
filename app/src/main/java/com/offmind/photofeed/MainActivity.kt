package com.offmind.photofeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.offmind.photofeed.ui.theme.PhotoFeedTheme
import com.offmind.photofeed.view.PhotoFeedScreen
import com.offmind.photofeed.viewmodel.PhotoFeedViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm = koinViewModel<PhotoFeedViewModel>()
            PhotoFeedTheme {
                // A surface container using the 'background' color from the theme
                PhotoFeedScreen(vm)
            }
        }
    }
}