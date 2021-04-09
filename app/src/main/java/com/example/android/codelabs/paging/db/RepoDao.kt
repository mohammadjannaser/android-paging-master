package com.example.android.codelabs.paging.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.android.codelabs.paging.model.Repo


@Dao
interface RepoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repos : List<Repo>)

    @Query("SELECT * FROM repo_table WHERE name LIKE :queryString OR description LIKE :queryString ORDER BY stars DESC,name ASC")
    fun reposByName(queryString: String) : PagingSource<Int,Repo>
    
    
    @Query("DELETE FROM repo_table")
    suspend fun clearRepos()
    
}