/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.codelabs.paging.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.android.codelabs.paging.api.GithubService
import com.example.android.codelabs.paging.db.RepoDatabase
import com.example.android.codelabs.paging.model.Repo
import kotlinx.coroutines.flow.Flow
// GitHub page API is 1 based: https://developer.github.com/v3/#pagination


/***************************************************************************************************
 * Repository class that works with local and remote data sources.
 * To construct the PagingData, we first need to decide what API we want to use to pass the
 * PagingData to other layers of our app:
 * 1. Kotlin Flow - use Pager.flow.
 * 2. LiveData - use Pager.liveData.
 * 3. RxJava Flowable - use Pager.flowable.
 * 4. RxJava Observable - use Pager.observable.
 **************************************************************************************************/
@OptIn(ExperimentalPagingApi::class)
class GithubRepository(private val service: GithubService,private val database: RepoDatabase) {


    /***********************************************************************************************
     * Search repositories whose names match the query, exposed as a stream of data that will emit
     * every time we get more data from the network.
     **********************************************************************************************/
    fun getSearchResultStream(query: String) : Flow<PagingData<Repo>> {

        Log.d("GithubRepository","New Query: $query")

        val dbQuery = "%${query.replace(' ','%')}%"
        val databasePagingSourceFactory = {database.reposDao().reposByName(dbQuery)}

        return Pager(
                config = PagingConfig(pageSize = NETWORK_PAGE_SIZE,enablePlaceholders = false),
                remoteMediator = GithubRemoteMediator(query,service,database),
                pagingSourceFactory = databasePagingSourceFactory
        ).flow
    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 50
    }
}
