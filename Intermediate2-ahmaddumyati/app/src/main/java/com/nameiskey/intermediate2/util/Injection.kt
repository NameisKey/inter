package com.nameiskey.intermediate2.util

import android.content.Context
import com.nameiskey.intermediate2.api.ApiConfig
import com.nameiskey.intermediate2.database.StoryDatabase
import com.nameiskey.intermediate2.repository.StoryRepository

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        val authKey = Preferences(context).getUser().token
        return StoryRepository(database, apiService, authKey)
    }
}