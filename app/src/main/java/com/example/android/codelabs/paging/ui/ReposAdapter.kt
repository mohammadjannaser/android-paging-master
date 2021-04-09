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

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.android.codelabs.paging.R
import java.lang.UnsupportedOperationException

/**
 * Adapter for the list of repositories.
 */
class ReposAdapter : PagingDataAdapter<SearchRepositoriesViewModel.UiModel, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == R.layout.repo_view_item) RepoViewHolder.create(parent)
        else SeparatorViwHolder.create(parent)
    }


    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is SearchRepositoriesViewModel.UiModel.RepoItem -> R.layout.repo_view_item
            is SearchRepositoriesViewModel.UiModel.SeparatorItem -> R.layout.separator_view_item
            null -> throw  UnsupportedOperationException("Unknown view")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val uiModel = getItem(position)
        uiModel.let {
            when(uiModel){
                is SearchRepositoriesViewModel.UiModel.RepoItem -> (holder as RepoViewHolder).bind(uiModel.repo)
                is SearchRepositoriesViewModel.UiModel.SeparatorItem -> (holder as SeparatorViwHolder).bind(uiModel.description)
            }
        }
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<SearchRepositoriesViewModel.UiModel>() {
            override fun areItemsTheSame(oldItem : SearchRepositoriesViewModel.UiModel, newItem: SearchRepositoriesViewModel.UiModel): Boolean =
                    (oldItem is SearchRepositoriesViewModel.UiModel.RepoItem && newItem is SearchRepositoriesViewModel.UiModel.RepoItem && oldItem.repo.fullName == newItem.repo.fullName) ||
                            (oldItem is SearchRepositoriesViewModel.UiModel.SeparatorItem && newItem is SearchRepositoriesViewModel.UiModel.SeparatorItem && oldItem.description == newItem.description)

            override fun areContentsTheSame(oldItem: SearchRepositoriesViewModel.UiModel, newItem: SearchRepositoriesViewModel.UiModel): Boolean = oldItem == newItem
        }
    }
}
