package win.downops.wallettracker

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import win.downops.wallettracker.data.ExpenseRepository
import win.downops.wallettracker.data.sqlite.session.SessionService
import win.downops.wallettracker.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import provideExpenseRepository
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.util.AppResultHandler

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
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
        val expenseDAO: ExpenseRepository = provideExpenseRepository(this.applicationContext)

        when (val result = expenseDAO.deleteAll()) {
            is AppResult.Success<*> -> {
                Toast.makeText(this, "Expenses reset successfully", Toast.LENGTH_SHORT).show()
            }
            is AppResult.Error -> {
                AppResultHandler.handleError(this, result)
            }
        }
    }



    private fun doLogOut() {
        SessionService(this).use { sSess ->
            sSess.deleteAll()
        }

    }
}