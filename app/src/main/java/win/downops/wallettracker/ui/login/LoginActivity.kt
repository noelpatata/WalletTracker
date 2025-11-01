package win.downops.wallettracker.ui.login

import Cryptography
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import win.downops.wallettracker.MainActivity
import win.downops.wallettracker.data.models.Session
import win.downops.wallettracker.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import win.downops.wallettracker.R
import win.downops.wallettracker.data.LoginRepository
import win.downops.wallettracker.data.api.ApiClient
import win.downops.wallettracker.data.api.communication.requests.LoginRequest
import win.downops.wallettracker.data.api.communication.requests.ServerPubKeyRequest
import win.downops.wallettracker.data.SessionRepository
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.CipheredCredentials
import win.downops.wallettracker.util.AppResultHandler
import win.downops.wallettracker.util.Biometrics
import win.downops.wallettracker.util.Logger
import java.nio.charset.Charset
import java.security.UnrecoverableKeyException
import java.util.Base64
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject lateinit var sessionRepo: SessionRepository
    @Inject lateinit var loginRepo: LoginRepository

    private lateinit var binding: ActivityLoginBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var foundCredentials = false
    private var isFingerprintActive = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loginForm.visibility = View.VISIBLE

        initUi()
        initBiometricPrompt()
        checkStoredCredentials()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initUi() {
        binding.apply {
            fingerprintAuth.setOnClickListener {
                isFingerprintActive = !isFingerprintActive
                updateFingerprintFabState()
            }

            login.setOnClickListener { attemptLogin() }

            inputPassword.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                    attemptLogin()
                    true
                } else false
            }

            offlineMode.setOnClickListener {
                sessionRepo.deleteAll()
                navigateToMain()
            }

            lifecycleScope.launch {
                offlineMode.isEnabled = !ApiClient.isServerReachable()
            }
        }
    }

    private fun updateFingerprintFabState() {
        val backgroundColor = if (isFingerprintActive) R.color.black else R.color.white
        val iconColor = if (isFingerprintActive) R.color.white else R.color.black

        binding.fingerprintAuth.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, backgroundColor))
        binding.fingerprintAuth.setColorFilter(ContextCompat.getColor(this, iconColor))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkStoredCredentials() {
        val session = sessionRepo.getFirstSession()
        foundCredentials = session?.fingerPrint == true && session.cipheredCredentials.isNotEmpty()

        if (foundCredentials) {
            isFingerprintActive = true
            updateFingerprintFabState()
            loginWithFingerprint(session!!.iv)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loginWithFingerprint(iv: String) {
        try {
            val cipher = Biometrics.getCipher()
            val secretKey = Biometrics.getSecretKey()
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(Base64.getDecoder().decode(iv)))
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } catch (e: UnrecoverableKeyException) {
            Logger.log("Key not found")
        } catch (e: Exception) {
            Logger.log(e)
        }
    }

    private fun initBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                handleBiometricSuccess(result)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                sessionRepo.deleteAll()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                sessionRepo.deleteAll()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleBiometricSuccess(result: BiometricPrompt.AuthenticationResult) {
        val session = sessionRepo.getFirstSession()
        if (session != null && session.cipheredCredentials.isNotEmpty()) {
            decryptStoredCredentials(session, result)
        } else {
            encryptAndLoginCredentials(result)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun decryptStoredCredentials(session: Session, result: BiometricPrompt.AuthenticationResult) {
        try {
            val input = Base64.getDecoder().decode(session.cipheredCredentials)
            val decryptedBytes = result.cryptoObject?.cipher?.doFinal(input)
            val decryptedArray = decryptedBytes?.toString(Charset.defaultCharset())?.split(";") ?: return

            if (decryptedArray.size == 2) {
                lifecycleScope.launch {
                    doLogin(decryptedArray[0], decryptedArray[1], CipheredCredentials(session.cipheredCredentials, session.iv))
                }
            }
        } catch (e: Exception) {
            Logger.log(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun encryptAndLoginCredentials(result: BiometricPrompt.AuthenticationResult) {
        try {
            val input = "${binding.inputUsername.text};${binding.inputPassword.text}".toByteArray(Charset.defaultCharset())
            val encryptedBytes = result.cryptoObject?.cipher?.doFinal(input)
            val iv = result.cryptoObject?.cipher?.iv ?: throw Exception("Invalid iv")

            val cipheredCredentials = CipheredCredentials(
                Base64.getEncoder().encodeToString(encryptedBytes),
                Base64.getEncoder().encodeToString(iv)
            )

            lifecycleScope.launch {
                doLogin(binding.inputUsername.text.toString(), binding.inputPassword.text.toString(), cipheredCredentials)
            }
        } catch (e: Exception) {
            Logger.log(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun attemptLogin() {
        val username = binding.inputUsername.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()
        if (username.isEmpty() || password.isEmpty()) return

        if (isFingerprintActive) {
            promptAuthentication()
        } else {
            lifecycleScope.launch {
                doLogin(username, password)
            }
        }
    }

    private fun promptAuthentication() {
        val keySpec = KeyGenParameterSpec.Builder(
            "USER_KEY",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .build()

        Biometrics.generateSecretKey(keySpec)
        val secretKey = Biometrics.getSecretKey()
        val cipher = Biometrics.getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun doLogin(username: String, password: String, cipheredCredentials: CipheredCredentials? = null) {
        try {
            showLoading(true)
            val credentials = LoginRequest(username, password)
            val loginResponse = handleResult(loginRepo.login(credentials)) ?: return
            val jwt = loginResponse.token

            val (privateKey, publicKey) = Cryptography().generateKeys()
            handleResult(loginRepo.setUserClientPubKey(jwt, ServerPubKeyRequest(publicKey))) ?: return

            val serverPublicKey = handleResult(loginRepo.getUserServerPubKey(jwt))?.publicKey
                ?: throw IllegalStateException("Server's public key is missing")

            saveSession(jwt, privateKey, serverPublicKey, cipheredCredentials)
            navigateToMain()

        } catch (e: Exception) {
            Logger.log(e)
        } finally {
            showLoading(false)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingPanel.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loginForm.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun <T> handleResult(result: AppResult<T>): T? = when (result) {
        is AppResult.Success -> result.data
        is AppResult.Error -> {
            AppResultHandler.handleError(this, result)
            null
        }
    }

    private fun saveSession(jwt: String, privateKey: String, serverPublicKey: String, cipheredCredentials: CipheredCredentials?) {
        val oldSession = sessionRepo.getFirstSession()
        val newSession = Session().apply {
            id = oldSession?.id ?: 0
            token = jwt
            this.privateKey = privateKey
            this.serverPublicKey = serverPublicKey
            this.cipheredCredentials = cipheredCredentials?.credentials.orEmpty()
            this.iv = cipheredCredentials?.iv.orEmpty()
            fingerPrint = cipheredCredentials?.credentials?.isNotEmpty() == true
        }

        if (oldSession == null) sessionRepo.insert(newSession)
        else sessionRepo.edit(newSession)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
