package com.example.vin.metron

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.vin.metron.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mainActivityBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)
        setBottomNav()
    }

    private fun setBottomNav(){
        val navController = findNavController(R.id.navigation_host)
        val appBarConfiguration = AppBarConfiguration.Builder(setOf(
            R.id.navigation_home, R.id.navigation_history, R.id.navigation_info,R.id.navigation_profile,
        )).build()

        setupActionBarWithNavController(navController, appBarConfiguration)
        mainActivityBinding.bottomNavView.setupWithNavController(navController)
    }

}