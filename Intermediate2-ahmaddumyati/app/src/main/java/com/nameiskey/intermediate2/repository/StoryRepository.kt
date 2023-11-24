package com.nameiskey.intermediate2.repository

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.nameiskey.intermediate2.api.ApiService
import com.nameiskey.intermediate2.data.StoryRemoteMediator
import com.nameiskey.intermediate2.database.StoryDatabase
import com.nameiskey.intermediate2.model.StoryList

class StoryRepository (
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    private val authKey: String
) {
    fun getStory(): LiveData<PagingData<StoryList>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                initialLoadSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, authKey),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }
}