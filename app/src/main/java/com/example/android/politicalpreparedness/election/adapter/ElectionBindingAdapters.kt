package com.example.android.politicalpreparedness.election.adapter

import android.widget.Button
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.models.Election

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Election>?) {
    val adapter = recyclerView.adapter as ElectionListAdapter
    adapter.submitList(data)
}

@BindingAdapter("buttonText")
fun Button.bindButtonText(isFollowed: Boolean) {
    text = if (isFollowed) {
        resources.getString(R.string.unfollow_election)
    } else {
        resources.getString(R.string.follow_election)
    }
}