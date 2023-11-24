package com.nameiskey.intermediate2.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nameiskey.intermediate2.R
import com.nameiskey.intermediate2.adapter.LoadingStateAdapter
import com.nameiskey.intermediate2.adapter.StoryListAdapter
import com.nameiskey.intermediate2.databinding.ActivityMainBinding
import com.nameiskey.intermediate2.factory.ViewModelFactory
import com.nameiskey.intermediate2.model.User
import com.nameiskey.intermediate2.util.Preferences
import com.nameiskey.intermediate2.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(this)
    }

    private val adapter = StoryListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        binding.rvStory.layoutManager = layoutManager

        setData(adapter)

        mainViewModel.story.observe(this) {
            adapter.submitData(lifecycle, it)
        }

        binding.fab.setOnClickListener {
            val intentToAddStory = Intent(this, AddStoryActivity::class.java)
            launcherAddStoryActivity.launch(intentToAddStory)
        }
    }

    private fun setData(adapter: StoryListAdapter) {
        binding.rvStory.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_maps -> {
                val intentToMaps = Intent(this, MapsActivity::class.java)
                startActivity(intentToMaps)
            }
            R.id.action_logout -> {
                clearUserSession()
                val intentToLogin = Intent(this, LoginActivity::class.java)
                startActivity(intentToLogin)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clearUserSession() {
        val userPreference = Preferences(this)
        val user = User("", "", "")

        userPreference.setUser(user)
    }

    private val launcherAddStoryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == ADD_STORY_RESULT) {
            adapter.refresh()
            setData(adapter)
        }
    }

    companion object {
        const val ADD_STORY_RESULT = 200
    }
}