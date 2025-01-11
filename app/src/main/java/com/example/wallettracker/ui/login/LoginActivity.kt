package com.example.wallettracker.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wallettracker.MainActivity
import com.example.wallettracker.data.login.LoginDAO
import com.example.wallettracker.data.login.LoginRequest
import com.example.wallettracker.databinding.ActivityLoginBinding
import com.example.wallettracker.util.Util

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private fun startMainActivity(token: String, userId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("TOKEN_KEY", token)
            putExtra("USER_ID", userId)
        }
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAutoLogin()

        initListeners()

    }

    private fun initListeners() {
        binding.login.setOnClickListener {

            //validation
            if (binding.inputUsername.text.toString().trim()
                    .isEmpty() || binding.inputPassword.text.toString().trim().isEmpty()
            ) {
                return@setOnClickListener
            }

            doLogin()


        }
    }

    private fun doLogin() {
        val credentials = LoginRequest(
            binding.inputUsername.text.toString(),
            binding.inputPassword.text.toString()
        )
        val LoginDAO = LoginDAO(credentials)
        var token = ""
        var user = 0
        LoginDAO.login(
            onSuccess = { login ->
                token = login.token
                user = login.userId

                Util.createAndWriteToCache(
                    applicationContext,
                    "autologin.txt",
                    user.toString(),
                    isSuccess = {})


                startMainActivity(token, user)
            },
            onFailure = { error ->
                showError("Login error: $error")
            }
        )
    }

    private fun checkAutoLogin() {
        binding.loadingPanel.visibility = View.VISIBLE
        binding.loginForm.visibility = View.GONE
        val string = Util.readFromCache(applicationContext, "autologin.txt")
        if (string.isNotEmpty()) {
            val userId = string.toInt()
            LoginDAO.autologin(
                userId,
                onSuccess = { login ->
                    startMainActivity(login.token, login.userId)
                },
                onFailure = {
                    binding.loadingPanel.visibility = View.GONE
                    binding.loginForm.visibility = View.VISIBLE
                }
            )
        }else {
            binding.loadingPanel.visibility = View.GONE
            binding.loginForm.visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }


}