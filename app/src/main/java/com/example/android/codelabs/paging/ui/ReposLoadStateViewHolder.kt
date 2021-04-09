package com.example.android.codelabs.paging.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.example.android.codelabs.paging.R
import com.example.android.codelabs.paging.databinding.RepoViewItemNetworkLayoutBinding

class ReposLoadStateViewHolder(private val binding : RepoViewItemNetworkLayoutBinding, retry :() -> Unit) : RecyclerView.ViewHolder(binding.root) {


    init {
        binding.retryButton.setOnClickListener{retry.invoke()}
    }

    fun bind(loadingState : LoadState){

        if (loadingState is LoadState.Error){
            binding.errorMsg.text = loadingState.error.localizedMessage
        }
        binding.progressBar.isVisible = loadingState is LoadState.Loading
        binding.retryButton.isVisible = loadingState !is LoadState.Loading
        binding.errorMsg.isVisible = loadingState !is LoadState.Loading
    }

    companion object{
        fun create(parent : ViewGroup, retry: () -> Unit) : ReposLoadStateViewHolder{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.repo_view_item_network_layout,parent,false)
            val binding = RepoViewItemNetworkLayoutBinding.bind(view)
            return ReposLoadStateViewHolder(binding,retry)
        }
    }

}