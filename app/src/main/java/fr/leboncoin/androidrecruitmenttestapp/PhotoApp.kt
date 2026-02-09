package fr.leboncoin.androidrecruitmenttestapp

import android.app.Application
import fr.leboncoin.androidrecruitmenttestapp.di.AppDependencies
import fr.leboncoin.androidrecruitmenttestapp.di.AppDependenciesProvider

class PhotoApp : Application(), AppDependenciesProvider {

    override val dependencies: AppDependencies by lazy { AppDependencies() }

    override fun onCreate() {
        super.onCreate()
        // Initialize analytics only once at the application level
        dependencies.analyticsHelper.initialize(this)
    }
}