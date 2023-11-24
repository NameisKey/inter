package com.nameiskey.intermediate2.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.nameiskey.intermediate2.R
import com.nameiskey.intermediate2.databinding.ActivityDetailBinding
import com.nameiskey.intermediate2.model.DetailResponse
import com.nameiskey.intermediate2.util.HttpResponseCode
import com.nameiskey.intermediate2.util.Preferences
import com.nameiskey.intermediate2.viewmodel.DetailViewModel

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    private lateinit var detailViewModel: DetailViewModel

    private lateinit var mUserPreference: Preferences

    private lateinit var intentData: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.text_detail)

        detailViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        )[DetailViewModel::class.java]

        detailViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        detailViewModel.responseType.observe(this) {
            if (it != HttpResponseCode.SUCCESS) storyDetailFailed(it)
        }

        mUserPreference = Preferences(this)
        val userToken = mUserPreference.getUser()

        intentData = intent.getStringExtra(EXTRA_DATA).toString()

        detailViewModel.setAuth(userToken.token, intentData)

        detailViewModel.detail.observe(this) {
            binding.apply {
                errorMessage.visibility = View.GONE
                btnReload.visibility = View.GONE
            }
            setStoryDetailData(it)
        }

        binding.btnReload.setOnClickListener {
            detailViewModel.setAuth(userToken.token, intentData)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun storyDetailFailed(statusCode: Int?) {
        val message = when (statusCode) {
            HttpResponseCode.FAILED -> resources.getString(R.string.error_story_failed)
            else -> resources.getString(R.string.error_server)
        }
        binding.apply {
            errorMessage.text = message
            errorMessage.visibility = View.VISIBLE
            btnReload.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.apply {
                progressBar.visibility = View.VISIBLE
                errorMessage.visibility = View.GONE
                btnReload.visibility = View.GONE
            }
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun setStoryDetailData(it: DetailResponse) {
        binding.apply {
            Glide.with(ivDetailPhoto).load(it.story.photoUrl)
                .into(ivDetailPhoto)
            tvDetailName.text = it.story.name
            tvDetailDescription.text = it.story.description
            tvDetailDate.text = it.story.createdAt
        }
    }

    companion object {
        const val EXTRA_DATA = "extra_data"
    }
}