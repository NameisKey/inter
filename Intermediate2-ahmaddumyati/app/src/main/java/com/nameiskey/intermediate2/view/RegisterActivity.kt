package com.nameiskey.intermediate2.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.nameiskey.intermediate2.R
import com.nameiskey.intermediate2.databinding.ActivityRegisterBinding
import com.nameiskey.intermediate2.util.HttpResponseCode
import com.nameiskey.intermediate2.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = resources.getString(R.string.text_register)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        registerViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        )[RegisterViewModel::class.java]

        registerViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        registerViewModel.responseType.observe(this) {
            if (it != HttpResponseCode.SUCCESS) registerFailed(it)
        }

        registerViewModel.user.observe(this) {
            successRegister()
        }

        binding.btnRegisterSubmit.setOnClickListener {
            if (binding.edRegisterName.length() == 0 || binding.edRegisterEmail.isError || binding.edRegisterPassword.isError) {
                Toast.makeText(
                    this@RegisterActivity,
                    resources.getString(R.string.error_submit_register),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.apply {
                    val email = edRegisterEmail.text.toString()
                    val name = edRegisterName.text.toString()
                    val password = edRegisterPassword.text.toString()

                    registerViewModel.userRegister(name, email, password)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.apply {
                edRegisterEmail.isEnabled = false
                edRegisterName.isEnabled = false
                edRegisterPassword.isEnabled = false
                btnRegisterSubmit.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }
        } else {
            binding.apply {
                edRegisterEmail.isEnabled = true
                edRegisterName.isEnabled = true
                edRegisterPassword.isEnabled = true
                btnRegisterSubmit.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun registerFailed(statusCode: Int) {
        val message = when (statusCode) {
            HttpResponseCode.FAILED -> resources.getString(R.string.error_register_failed)
            else -> resources.getString(R.string.error_server)
        }
        Toast.makeText(this@RegisterActivity, "$statusCode: $message", Toast.LENGTH_SHORT).show()
    }

    private fun successRegister() {
        Toast.makeText(
            this@RegisterActivity,
            resources.getString(R.string.success_register),
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}