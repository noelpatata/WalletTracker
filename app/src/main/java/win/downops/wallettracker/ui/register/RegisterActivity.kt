package win.downops.wallettracker.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import win.downops.wallettracker.MainActivity
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.databinding.ActivityRegisterBinding
import win.downops.wallettracker.ui.login.LoginActivity

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.registerForm.visibility = View.VISIBLE

        initUi()
        observeViewModel()
    }

    private fun initUi() {
        binding.apply {
            register.setOnClickListener { attemptRegister() }

            inputConfirmPassword.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                    attemptRegister()
                    true
                } else false
            }

            goToLogin.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.registerResult.observe(this) { result ->
            when (result) {
                is AppResult.Success -> {
                    showLoading(false)
                    navigateToMain()
                }
                is AppResult.Error -> {
                    showLoading(false)
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun attemptRegister() {
        val username = binding.inputUsername.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()
        val confirmPassword = binding.inputConfirmPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) return

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        viewModel.register(username, password)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingPanel.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.registerForm.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
