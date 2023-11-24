package com.nameiskey.intermediate2.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.nameiskey.intermediate2.R
import com.nameiskey.intermediate2.databinding.ActivityLoginBinding
import com.nameiskey.intermediate2.model.User
import com.nameiskey.intermediate2.util.HttpResponseCode
import com.nameiskey.intermediate2.util.Preferences
import com.nameiskey.intermediate2.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = resources.getString(R.string.text_login)

        loginViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        )[LoginViewModel::class.java]

        loginViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        loginViewModel.responseType.observe(this) {
            if (it != HttpResponseCode.SUCCESS) loginFailed(it)
        }

        loginViewModel.user.observe(this) {
            saveUser(it.loginResult.userId, it.loginResult.name, it.loginResult.token)
            successLogin()
        }

        binding.btnLoginSubmit.setOnClickListener {
            if (binding.edLoginEmail.isError || binding.edLoginPassword.isError) {
                Toast.makeText(
                    this@LoginActivity,
                    resources.getString(R.string.error_submit_login),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val email = binding.edLoginEmail.text.toString()
                val password = binding.edLoginPassword.text.toString()
                loginViewModel.userLogin(email, password)
            }
        }

        binding.btnRegister.setOnClickListener {
            val intentToRegister = Intent(this, RegisterActivity::class.java)
            startActivity(intentToRegister)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.apply {
                edLoginEmail.isEnabled = false
                edLoginPassword.isEnabled = false
                btnLoginSubmit.visibility = View.GONE
                btnRegister.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }
        } else {
            binding.apply {
                edLoginEmail.isEnabled = true
                edLoginPassword.isEnabled = true
                btnLoginSubmit.visibility = View.VISIBLE
                btnRegister.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun loginFailed(statusCode: Int) {
        val message = when (statusCode) {
            HttpResponseCode.FAILED -> resources.getString(R.string.error_failed_login)
            else -> resources.getString(R.string.error_server)
        }

        Toast.makeText(this@LoginActivity, "$statusCode: $message", Toast.LENGTH_SHORT).show()
    }

    private fun saveUser(userId: String, name: String, token: String) {
        val userPreference = Preferences(this)
        val user = User(name, userId, token)

        userPreference.setUser(user)
    }

    private fun successLogin() {
        val intentToMain = Intent(this, MainActivity::class.java)
        startActivity(intentToMain)
        finish()
    }

}