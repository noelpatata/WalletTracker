package win.downops.wallettracker.ui.login

import Cryptography
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import win.downops.wallettracker.MainActivity
import win.downops.wallettracker.data.api.login.LoginHttpService
import win.downops.wallettracker.data.models.Session
import win.downops.wallettracker.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import win.downops.wallettracker.data.api.ApiClient
import win.downops.wallettracker.data.api.communication.requests.LoginRequest
import win.downops.wallettracker.data.api.communication.requests.ServerPubKeyRequest
import win.downops.wallettracker.data.sqlite.SessionRepository
import win.downops.wallettracker.di.SessionRepositoryProvider
import win.downops.wallettracker.util.Biometrics
import win.downops.wallettracker.util.Logger
import java.nio.charset.Charset
import java.security.UnrecoverableKeyException
import java.util.Base64
import java.util.concurrent.Executor
import javax.crypto.Cipher

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    @Inject
    lateinit var sessionProvider: SessionRepositoryProvider
    private lateinit var sessionRepo: SessionRepository
    private lateinit var binding: ActivityLoginBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        try{

            super.onCreate(savedInstanceState)

            sessionRepo = sessionProvider.get()

            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.loginForm.visibility = View.VISIBLE

            initListeners()
            initPromptCallbacks()

            lifecycleScope.launch {
                binding.offlineMode.isEnabled = !ApiClient.isServerReachable()
            }

            loginWithFingerprit()

//            if(BuildConfig.DEBUG){
//                lifecycleScope.launch {
//                    doLogin(BuildConfig.DEFAULT_USER,
//                        BuildConfig.DEFAULT_PASSWORD)
//                }
//            }
        }catch(e: Exception){
            Logger.log(e)
        }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListeners() {
        try{

            binding.login.setOnClickListener {

                if (binding.inputUsername.text.toString().trim()
                        .isEmpty() || binding.inputPassword.text.toString().trim().isEmpty()
                ) {
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    doLogin(binding.inputUsername.text.toString(),
                        binding.inputPassword.text.toString())
                }


            }

            binding.inputPassword.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {

                    lifecycleScope.launch {
                        doLogin(binding.inputUsername.text.toString(),
                            binding.inputPassword.text.toString())
                    }

                    return@setOnEditorActionListener true
                }
                false
            }

            binding.offlineMode.setOnClickListener {
                sessionRepo.deleteAll()
                startMainActivity()
            }
        }catch(e: Exception){
            Logger.log(e)
        }
    }

    private fun loginWithFingerprit(){
        try{
            val secretKey = Biometrics.getSecretKey()
            val cipher = Biometrics.getCipher()
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            biometricPrompt.authenticate(promptInfo,
                BiometricPrompt.CryptoObject(cipher))
        }catch(e: UnrecoverableKeyException){
            Logger.log("Key not found")
        }catch(e: Exception){
            Logger.log(e)
        }
    }
    private fun promptAuthentication(){

        val cipher = Biometrics.getCipher()
        Biometrics.generateSecretKey(KeyGenParameterSpec.Builder(
            "USER_KEY",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .build())
        val secretKey = Biometrics.getSecretKey()

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        biometricPrompt.authenticate(promptInfo,
            BiometricPrompt.CryptoObject(cipher))
    }
    private fun initPromptCallbacks() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val session = sessionRepo.getFirstSession()

                    if(session != null && session.cipheredCredentials.isNotEmpty()){
                        val input = Base64.getDecoder().decode(session.cipheredCredentials)
                        val decryptedBytes: ByteArray? = result.cryptoObject?.cipher?.doFinal(
                            input
                        )
                        val decryptedString = decryptedBytes?.toString(Charset.defaultCharset()) ?: ""
                        val decryptedArray = decryptedString.split(";")
                        if(decryptedArray.size == 2){
                            lifecycleScope.launch {
                                doLogin(decryptedArray[0], decryptedArray[1])
                            }
                        }
                    }
                    else{
                        val input = "{${binding.inputUsername.text};${binding.inputPassword.text}}".toByteArray(Charset.defaultCharset())
                        val encryptedBytes: ByteArray? = result.cryptoObject?.cipher?.doFinal(
                            input
                        )
                        encryptedCredentials = String(Base64.getEncoder().encode(encryptedBytes))
                    }

                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()
    }

    private var encryptedCredentials: String = ""
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun doLogin(username: String, password: String) = withContext(Dispatchers.IO) {
        try {

            if(binding.fingerprintAuth.isActivated){
                promptAuthentication()
            }


            val credentials = LoginRequest(username, password)
            val loginHttpService = LoginHttpService()

            val loginResponse = loginHttpService.login(credentials)
            val jwt = loginResponse.token

            val (privateKey, publicKey) = Cryptography().generateKeys()

            loginHttpService.setUserClientPubKey(jwt, ServerPubKeyRequest(publicKey))

            val serverResponse = loginHttpService.getUserServerPubKey(jwt)
            val serverPublicKey = serverResponse.publicKey

            val newSession = Session().apply {
                this.token = jwt
                this.serverPublicKey = serverPublicKey
                this.privateKey = privateKey
                this.cipheredCredentials = encryptedCredentials
            }
            if(sessionRepo.getFirstSession() == null){
                sessionRepo.insert(newSession)
            }else{
                sessionRepo.edit(newSession)
            }

            withContext(Dispatchers.Main) {
                startMainActivity()
            }

        } catch (e: Exception) {
            Logger.log(e)
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {}

        startActivity(intent)

    }
}
