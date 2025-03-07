package com.example.wallettracker

import android.content.Context
import android.content.Intent
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
import com.example.wallettracker.data.expense.OnlineExpenseDAO
import com.example.wallettracker.data.session.SessionDAO
import com.example.wallettracker.databinding.ActivityMainBinding
import com.example.wallettracker.ui.login.LoginActivity

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
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_categories,
                R.id.nav_importsheet,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val headerView = navView.getHeaderView(0)


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isOnline(context: Context): Boolean {
        val session = SessionDAO(context).getFirstSession()
        return session?.online ?: true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
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
                ResetExpenses()
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

    private fun doLogOut() {
        SessionDAO(this).use { sSess ->
            sSess.deleteAll()//clears all sessions
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ResetExpenses() {
        OnlineExpenseDAO(this).deleteAll(
            onSuccess = { response ->
                if (response.success) {
                    Toast.makeText(this, "Expenses reset successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to reset expenses", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = { error ->
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        doLogOut()
    }
}