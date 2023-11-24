package com.nameiskey.intermediate2.viewmodel

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nameiskey.intermediate2.model.StoryList
import com.nameiskey.intermediate2.repository.StoryRepository

class MainViewModel(storyRepository: StoryRepository) : ViewModel(), LifecycleObserver {
    val story: LiveData<PagingData<StoryList>> = storyRepository.getStory().cachedIn(viewModelScope)
}