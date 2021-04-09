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

package com.example.android.codelabs.paging.ui

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.android.codelabs.paging.Injection
import com.example.android.codelabs.paging.databinding.ActivitySearchRepositoriesBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchRepositoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchRepositoriesBinding
    private lateinit var viewModel: SearchRepositoriesViewModel
    private val adapter = ReposAdapter()

    private var searchJob : Job? = null
    private lateinit var mContext : Context

    /***********************************************************************************************
     * Sometimes, the collect function might not be automatically detected by Android Studio.
     * In this case, make sure you import kotlinx.coroutines.flow.collect.
     **********************************************************************************************/
    private fun search(query : String){
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.searchRepo(query).collectLatest { adapter.submitData(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchRepositoriesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        mContext = this

        // get the view model
        viewModel = ViewModelProvider(this, Injection.provideViewModelFactory(mContext)).get(SearchRepositoriesViewModel::class.java)

        // add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        binding.list.addItemDecoration(decoration)

        initAdapter()

        val query = savedInstanceState?.getString(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY
        search(query)
        initSearch(query)
        binding.retryButton.setOnClickListener { adapter.retry() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LAST_SEARCH_QUERY, binding.searchRepo.text.trim().toString())
    }

    private fun initAdapter() {
        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
                header = ReposLoadStateAdapter { adapter.retry()},
                footer = ReposLoadStateAdapter { adapter.retry()}
        )

        // Show loading spinner during initial load or refresh
        // This section is used to show loading bar in the main page if you are using room database
        // there is no need for this loading cause the database is fast enough to load the data.

//        adapter.addLoadStateListener { combinedLoadStates ->
//            // Only show the list if refresh succeeds.
//            binding.list.isVisible = combinedLoadStates.source.refresh is LoadState.NotLoading
//            // Show loading spinner during initial load or refresh
//            // binding.progressBar.isVisible = combinedLoadStates.source.refresh is LoadState.Loading
//            // Show the retry state if initial load or refresh fails
//            binding.retryButton.isVisible = combinedLoadStates.source.refresh is LoadState.Error
//
//            val errorState = combinedLoadStates.source.append as? LoadState.Error
//                    ?: combinedLoadStates.source.prepend as? LoadState.Error
//                    ?: combinedLoadStates.append as? LoadState.Error
//                    ?: combinedLoadStates.prepend as? LoadState.Error
//
//            errorState?.let { Toast.makeText(this,"\uD83D\uDE28 Wooops ${it.error}",Toast.LENGTH_LONG).show() }
//        }

    }

    private fun initSearch(query: String) {
        binding.searchRepo.setText(query)

        binding.searchRepo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateRepoListFromInput()
                true
            } else false
        }
        binding.searchRepo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateRepoListFromInput()
                true
            } else false
        }

        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            adapter.loadStateFlow
                    // Only emit when REFRESH LoadState Changes.
                    .distinctUntilChangedBy {it.refresh}
                    // Only react to cases where REFRESH completes. i.e., NotLoading.
                    .filter { it.refresh is LoadState.NotLoading}
                    .collect { binding.list.scrollToPosition(0) }

        }
    }

    private fun updateRepoListFromInput(){
        binding.searchRepo.text.trim().let {
            if (it.isNotEmpty()){
                binding.list.scrollToPosition(0)
                search(it.toString())
            }
        }
    }

    companion object {
        private const val LAST_SEARCH_QUERY: String = "last_search_query"
        private const val DEFAULT_QUERY = "Android"
    }
}
