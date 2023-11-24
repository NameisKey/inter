package com.nameiskey.intermediate2.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.nameiskey.intermediate2.R
import com.nameiskey.intermediate2.databinding.ActivitySplashScreenBinding
import com.nameiskey.intermediate2.model.User
import com.nameiskey.intermediate2.util.Preferences

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    private lateinit var mUserPreference: Preferences

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        mUserPreference = Preferences(this)

        getExistingPreference()
    }

    private fun getExistingPreference() {
        user = mUserPreference.getUser()
        setIntentLocation(user)
    }

    private fun setIntentLocation(user: User) {
        Handler(
            Looper.getMainLooper()
        ).postDelayed({
            val intent = if (user.userId.isEmpty() || user.name.isEmpty() || user.token.isEmpty()) {
                Intent(this, LoginActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, SPLASH_DURATION_MILLIS)
    }

    companion object {
        private const val SPLASH_DURATION_MILLIS: Long = 2500
    }
}