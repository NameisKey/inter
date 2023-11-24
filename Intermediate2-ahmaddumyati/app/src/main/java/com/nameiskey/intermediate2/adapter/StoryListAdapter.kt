package com.nameiskey.intermediate2.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nameiskey.intermediate2.databinding.ItemRowBinding
import com.nameiskey.intermediate2.model.StoryList
import com.nameiskey.intermediate2.view.DetailActivity

class StoryListAdapter :
    PagingDataAdapter<StoryList, StoryListAdapter.ListViewHolder>(DIFF_CALLBACK) {

    class ListViewHolder(var binding: ItemRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StoryList) {
            binding.apply {
                Glide.with(ivItemPhoto).load(data.photoUrl)
                    .into(ivItemPhoto)
                tvItemName.text = data.name
                tvItemDate.text = data.createdAt
                tvItemDescription.text = data.description
            }

            itemView.setOnClickListener {
                val intentToDetail = Intent(itemView.context, DetailActivity::class.java)
                intentToDetail.putExtra(DetailActivity.EXTRA_DATA, data.id)

                binding.apply {
                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            itemView.context as Activity,
                            Pair(ivItemUser, ICON),
                            Pair(ivItemPhoto, PHOTO),
                            Pair(tvItemName, NAME),
                            Pair(tvItemDescription, DESCRIPTION),
                            Pair(tvItemDate, DATE)
                        )

                    itemView.context.startActivity(
                        intentToDetail,
                        optionsCompat.toBundle()
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding =
            ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(listViewHolder: ListViewHolder, position: Int) {
        val index = getItem(position)

        if (index != null) {
            listViewHolder.bind(index)
        }
    }

    companion object {
        private const val ICON = "icon"
        private const val PHOTO = "photo"
        private const val NAME = "name"
        private const val DESCRIPTION = "description"
        private const val DATE = "date"

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryList>() {
            override fun areItemsTheSame(oldItem: StoryList, newItem: StoryList): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryList, newItem: StoryList): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

}
