package win.downops.wallettracker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import win.downops.wallettracker.data.ExpenseRepository
import win.downops.wallettracker.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.SessionRepository
import win.downops.wallettracker.ui.login.LoginActivity
import win.downops.wallettracker.util.AppResultHandler
import win.downops.wallettracker.util.Logger

@AndroidEntryPoint
class MainActivity  : AppCompatActivity() {
    @Inject
    lateinit var expenseRepo: ExpenseRepository
    @Inject
    lateinit var sessionRepo: SessionRepository
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        try{
            super.onCreate(savedInstanceState)

            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setSupportActionBar(binding.appBarMain.toolbar)

            val drawerLayout: DrawerLayout = binding.drawerLayout
            val navView: NavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_content_main)

            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_categories,
                    R.id.nav_importsheet,
                    R.id.nav_settings,
                    R.id.nav_logout
                ), drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
            navView.getHeaderView(0)

        }catch(e: Exception){
            Logger.log(e)
        }


    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.resetExpenses -> {

                lifecycleScope.launch {
                    resetExpenses()
                }
                return true
            }
            R.id.logOff -> {
                doLogOut()
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun resetExpenses() {
        try{

            when (val result = expenseRepo.deleteAll()) {
                is AppResult.Success<*> -> {
                    Toast.makeText(this, "Expenses reset successfully", Toast.LENGTH_SHORT).show()
                }
                is AppResult.Error -> {
                    AppResultHandler.handleError(this, result)
                }
            }
        }catch(e: Exception){
            Logger.log(e)
        }
    }



    fun doLogOut() {
        try{
            sessionRepo.deleteAll()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }catch(e: Exception){
            Logger.log(e)
        }

    }
}