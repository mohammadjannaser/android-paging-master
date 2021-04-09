package com.example.android.codelabs.paging.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.android.codelabs.paging.api.GithubService
import com.example.android.codelabs.paging.api.IN_QUALIFIER
import com.example.android.codelabs.paging.db.RemoteKeys
import com.example.android.codelabs.paging.db.RepoDatabase
import com.example.android.codelabs.paging.model.Repo
import retrofit2.HttpException
import java.io.IOException
import java.io.InvalidObjectException


private const val GITHUB_STARTING_PAGE_INDEX = 1


@OptIn(ExperimentalPagingApi::class)
class GithubRemoteMediator (private val query : String, private val service : GithubService, private val repoDatabase: RepoDatabase) : RemoteMediator<Int,Repo>(){


    /**********************************************************************************************
     * PagingState - this gives us information about the pages that were loaded before, the most
     * recently accessed index in the list, and the PagingConfig we defined when initializing the
     * paging stream.
     * LoadType - this tells us whether we need to load data at the end (LoadType.APPEND) or
     * at the beginning of the data (LoadType.PREPEND) that we previously loaded, or if this
     * the first time we're loading data (LoadType.REFRESH).
     **********************************************************************************************/
    override suspend fun load(loadType: LoadType, state: PagingState<Int, Repo>): MediatorResult {


        /*******************************************************************************************
         *      Let's see how we can implement the GithubRemoteMediator.load() method:
         * Find out what page we need to load from the network, based on the LoadType.
         * Trigger the network request.
         * Once the network request completes, if the received list of repositories is not empty,
         * then do the following:
         * We compute the RemoteKeys for every Repo.
         * If this a new query (loadType = REFRESH) then we clear the database.
         * Save the RemoteKeys and Repos in the database.
         * Return MediatorResult.Success(endOfPaginationReached = false).
         * If the list of repos was empty then we return MediatorResult.
         *          Success(endOfPaginationReached = true).
         * If we get an error requesting data we return MediatorResult.Error.
         ******************************************************************************************/

        val page = when(loadType){

            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: GITHUB_STARTING_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state) ?:
                // The LoadType is PREPEND so some data was loaded before,
                // so we should have been able to get remote keys
                // If the remoteKeys are null, then we're an invalid state and we have a bug
                throw InvalidObjectException("Remote key and the prevKey should not be null")

                // If the previous key is null, then we can't request more data
                remoteKeys.prevKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                remoteKeys.prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                if (remoteKeys?.nextKey == null){
                    throw InvalidObjectException("Remote Key should not be null for $loadType")
                }
                remoteKeys.nextKey
            }
        }


        val apiQuery = query + IN_QUALIFIER

        try {
            val apiResponse  = service.searchRepos(apiQuery,page,state.config.pageSize)

            val repos = apiResponse.items
            val endOfPaginationReached = repos.isEmpty()
            repoDatabase.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH){
                    repoDatabase.remoteKeysDao().clearRemoteKeys()
                    repoDatabase.reposDao().clearRepos()
                }
                val prevKey = if (page == GITHUB_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = repos.map { RemoteKeys(repoId = it.id,prevKey = prevKey, nextKey = nextKey) }
                repoDatabase.remoteKeysDao().insertAll(keys)
                repoDatabase.reposDao().insertAll(repos)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        }catch (exception : IOException){
            return MediatorResult.Error(exception)
        }catch (exception : HttpException){
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Repo>) : RemoteKeys? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item.
        return state.pages.lastOrNull {it.data.isNotEmpty()}?.data?.lastOrNull()?.let { repo ->
            repoDatabase.remoteKeysDao().remoteKeysRepoId(repoId = repo.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state : PagingState<Int,Repo>) : RemoteKeys?{
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull {it.data.isNotEmpty()}?.data?.firstOrNull()?.let { repo ->
            repoDatabase.remoteKeysDao().remoteKeysRepoId(repo.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, Repo>): RemoteKeys? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { repoId ->
                repoDatabase.remoteKeysDao().remoteKeysRepoId(repoId)
            }
        }
    }



}