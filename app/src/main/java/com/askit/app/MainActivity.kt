package com.askit.app

import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.defaultExploreFilterOptions
import com.askit.app.listservice.ListServiceViewModel
import com.askit.app.posttask.PostTaskViewModel
import com.askit.designsystem.theme.AskITTheme
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializePlaces()
        val exploreViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { ExploreViewModel(createSavedStateHandle()) }
            },
        )[ExploreViewModel::class.java]
        val postTaskViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { PostTaskViewModel(createSavedStateHandle()) }
            },
        )[PostTaskViewModel::class.java]
        val listServiceViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { ListServiceViewModel(createSavedStateHandle()) }
            },
        )[ListServiceViewModel::class.java]
        val createPostViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { CreatePostViewModel(createSavedStateHandle()) }
            },
        )[CreatePostViewModel::class.java]
        setContent {
            AskITTheme {
                AskITApp(
                    exploreViewModel = exploreViewModel,
                    postTaskViewModel = postTaskViewModel,
                    listServiceViewModel = listServiceViewModel,
                    createPostViewModel = createPostViewModel,
                    onExit = ::finish,
                    availableFilterOptions = defaultExploreFilterOptions(),
                    treatUnresolvedSearchAsEmpty = true,
                )
            }
        }
    }

    private fun initializePlaces() {
        val apiKey = packageManager
            .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData
            ?.getString("com.google.android.geo.API_KEY")
            .orEmpty()
        if (apiKey.isBlank() || apiKey == "DEFAULT_API_KEY" || Places.isInitialized()) return
        runCatching {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        }
    }
}
