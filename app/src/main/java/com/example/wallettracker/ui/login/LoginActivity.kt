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
import com.example.wallettracker.MainActivity
import com.example.wallettracker.data.session.SessionDAO
import com.example.wallettracker.data.login.LoginDAO
import com.example.wallettracker.data.login.LoginRequest
import com.example.wallettracker.data.login.ServerPubKeyRequest
import com.example.wallettracker.data.session.Session
import com.example.wallettracker.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding


    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {}

        startActivity(intent)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAutoLogin()

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

        binding.offlineMode.setOnClickListener {
            SessionDAO(this).use { sSess ->
                sSess.deleteAll()//clears all sessions

                //creates a new session
                val newSess = Session().apply{
                    online = false
                }
                sSess.insert(newSess)
                sSess.close()

                startMainActivity()


            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun doLogin(username: String, password: String) {
        val credentials = LoginRequest(
            username,
            password
        )
        val LoginDAO = LoginDAO(credentials)
        var tokenn = ""
        var user = 0
        LoginDAO.login(
            onSuccess = { login ->
                tokenn = login.token

                //generate client keys
                val keys = Cryptography().generateKeys()
                val privateKeyy = keys[0]
                val publicKey = keys[1]

                //send to server
                val request = ServerPubKeyRequest(credentials.username, credentials.password, publicKey)
                LoginDAO.setUserClientPubKey(request,
                    onSuccess = {
                        //get server's public key
                        LoginDAO.getUserServerPubKey(LoginRequest(credentials.username, credentials.password),
                            onSuccess={
                                val serverPublicKeyy = it.publicKey
                                SessionDAO(this).use { sSess ->
                                    sSess.deleteAll()//clears all sessions

                                    //creates a new session
                                    val newSess = Session().apply{
                                        userId = it.userId
                                        token = tokenn
                                        online = true
                                        serverPublicKey = serverPublicKeyy
                                        privateKey = privateKeyy
                                        remember = binding.cboxRememberpassw.isChecked
                                    }

                                    sSess.insert(newSess)
                                    sSess.close()

                                    startMainActivity()


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

        val sSess = SessionDAO(this)  // Open SessionDAO manually
        val session = sSess.getFirstSession()

        if (session != null && session.id >= 0 && session.remember) {
            LoginDAO.autologin(
                session,
                onSuccess = { login ->
                    if (login.token != null) {
                        session.token = login.token
                        sSess.edit(session)  // Database is still open here
                    }
                    sSess.close()  // Manually close SessionDAO after use
                    startMainActivity()
                },
                onFailure = {
                    sSess.close()  // Manually close SessionDAO even on failure
                    binding.loadingPanel.visibility = View.GONE
                    binding.loginForm.visibility = View.VISIBLE
                }
            )
        } else {
            sSess.close()  // Close if no session found
            binding.loadingPanel.visibility = View.GONE
            binding.loginForm.visibility = View.VISIBLE
        }
    }


    private fun showError(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }


}