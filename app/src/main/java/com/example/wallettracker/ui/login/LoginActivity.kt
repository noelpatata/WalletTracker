package com.example.wallettracker.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wallettracker.MainActivity
import com.example.wallettracker.R
import com.example.wallettracker.data.login.LoginDAO
import com.example.wallettracker.data.login.LoginRequest
import com.example.wallettracker.databinding.ActivityLoginBinding
import com.example.wallettracker.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)




        binding.login.setOnClickListener {

            //validation
            if (binding.inputUsername.text.toString().trim().isEmpty() || binding.inputPassword.text.toString().trim().isEmpty()) {
                return@setOnClickListener
            }

            val credentials = LoginRequest(binding.inputUsername.text.toString(), binding.inputPassword.text.toString())
            val LoginDAO = LoginDAO(credentials)
            var token = ""
            var userId = 0
            LoginDAO.login(
                onSuccess = { login ->
                    token = login.token
                    userId = login.userId

                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("TOKEN_KEY", token)
                        putExtra("USER_ID", userId)
                    }
                    startActivity(intent)
                },
                onFailure = { error ->
                    showError("Login error: $error")
                }
            )


        }
    }

    private fun showError(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}