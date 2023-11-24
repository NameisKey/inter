package com.nameiskey.intermediate2.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nameiskey.intermediate2.model.StoryList

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(quote: List<StoryList>)

    @Query("SELECT * FROM StoryList")
    fun getAllStory(): PagingSource<Int, StoryList>

    @Query("DELETE FROM StoryList")
    suspend fun deleteAll()
}