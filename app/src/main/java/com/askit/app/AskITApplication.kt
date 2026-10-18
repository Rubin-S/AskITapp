package com.askit.app

import android.app.Application
import com.askit.app.di.AppContainer

/** Owns the app's [AppContainer]; activities read their dependencies from it. */
class AskITApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
