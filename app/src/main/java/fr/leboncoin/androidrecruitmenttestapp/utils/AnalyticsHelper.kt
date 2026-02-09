package fr.leboncoin.androidrecruitmenttestapp.utils

import android.content.Context
import android.util.Log
import androidx.core.content.edit

class AnalyticsHelper {

    private var appContext: Context? = null

    fun initialize(context: Context) {
        // Use applicationContext (to avoid memory leaks)
        this.appContext = context.applicationContext
    }

    fun trackSelection(itemId: String) {
        val context = appContext ?: run {
            Log.w(TAG, "Analytics not initialized, skipping trackSelection")
            return
        }

        val prefs = context.getSharedPreferences(ANALYTICS_SHARED_PREFS, Context.MODE_PRIVATE)
        prefs.edit { putString(SELECTED_ITEM_KEY, itemId) }

        // Simulate some analytics logging
        Log.d(TAG, "Analytics: User selected item - $itemId")
    }

    fun trackScreenView(screenName: String) {
        appContext ?: run {
            Log.w(TAG, "Analytics not initialized, skipping trackScreenView")
            return
        }

        // Simulate some analytics logging
        Log.d(TAG, "Analytics: Screen viewed - $screenName")
    }

    companion object {
        private const val TAG = "AnalyticsHelper"
    }
}

private const val ANALYTICS_SHARED_PREFS = "analytics_prefs"
private const val SELECTED_ITEM_KEY = "selected_item"