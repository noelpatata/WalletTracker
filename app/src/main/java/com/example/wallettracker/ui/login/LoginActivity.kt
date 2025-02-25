package com.example.wallettracker.ui.login

import Cryptography
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.wallettracker.MainActivity
import com.example.wallettracker.data.session.SessionDAO
import com.example.wallettracker.data.login.LoginDAO
import com.example.wallettracker.data.login.LoginRequest
import com.example.wallettracker.data.login.ServerPubKeyRequest
import com.example.wallettracker.data.session.Session
import com.example.wallettracker.databinding.ActivityLoginBinding
import com.example.wallettracker.util.Util

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding


    private fun startMainActivity(token: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("TOKEN_KEY", token)
        }

        startActivity(intent)
        finish()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        doLogin("jordi", "jordi")
        //checkAutoLogin()

        initListeners()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListeners() {
        binding.login.setOnClickListener {

            //validation
            if (binding.inputUsername.text.toString().trim()
                    .isEmpty() || binding.inputPassword.text.toString().trim().isEmpty()
            ) {
                return@setOnClickListener
            }

            doLogin(binding.inputUsername.text.toString(),
                binding.inputPassword.text.toString())


        }

        binding.inputPassword.setOnEditorActionListener { v, actionId, event -> //cuando se presiona enter
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {

                doLogin(binding.inputUsername.text.toString(),
                    binding.inputPassword.text.toString())

                return@setOnEditorActionListener true
            }
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun doLogin(username: String, password: String) {
        val credentials = LoginRequest(
            username,
            password
        )
        val LoginDAO = LoginDAO(credentials)
        var token = ""
        var user = 0
        LoginDAO.login(
            onSuccess = { login ->
                token = login.token

                //generate client keys
                val keys = Cryptography().generateKeys()
                val privateKey = keys[0]
                val publicKey = keys[1]

                //send to server
                val request = ServerPubKeyRequest(credentials.username, credentials.password, publicKey)
                LoginDAO.setUserClientPubKey(request,
                    onSuccess = {
                        //get server's public key
                        LoginDAO.getUserServerPubKey(LoginRequest(credentials.username, credentials.password),
                            onSuccess={
                                val serverPublicKey = it.publicKey
                                SessionDAO(this).use { sSess ->
                                    sSess.deleteAll()//clears all sessions

                                    //creates a new session
                                    val newSess = Session()
                                    newSess.userId = it.userId
                                    newSess.serverPublicKey = serverPublicKey
                                    newSess.privateKey = privateKey
                                    sSess.insert(newSess)

                                    //start activity
                                    startMainActivity(token)
                                }

                            },
                            onFailure = {
                                showError("Error getting server public key:\n ${it.message}")
                            }
                        )
                    },
                    onFailure = { it
                        showError("Error sending public key:\n ${it.message}")
                    }
                )


            },
            onFailure = { error ->
                showError("Login error: $error")
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAutoLogin() {
        binding.loadingPanel.visibility = View.VISIBLE
        binding.loginForm.visibility = View.GONE
        SessionDAO(this).use { sSess ->
            val session = sSess.getFirstSession()
            if(session != null && session.id >= 0){
                LoginDAO.autologin(
                    session,
                    onSuccess = { login ->
                        if(login.token != null){
                            startMainActivity(login.token)
                        }

                    },
                    onFailure = {
                        binding.loadingPanel.visibility = View.GONE
                        binding.loginForm.visibility = View.VISIBLE
                    }
                )
            }

        }
        binding.loadingPanel.visibility = View.GONE
        binding.loginForm.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }


}