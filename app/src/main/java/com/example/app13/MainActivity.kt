package com.example.app13

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.navigation.NavDestination
import com.example.app13.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    internal lateinit var binding: ActivityMainBinding
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
                R.id.nav_notes, R.id.nav_deleted, R.id.nav_archived
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.appBarMain.fab.setOnClickListener { _ ->
            goToActivity(TakeNote::class.java)
        }
        var fragmentIdToLoad: Int? = null
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            fragmentIdToLoad = destination.id
            binding.navView.setCheckedItem(destination.id)
            handleDestinationChange(destination)
        }
//        binding.navView.setNavigationItemSelectedListener { item ->
//            fragmentIdToLoad = item.itemId
//            binding.drawerLayout.closeDrawer(GravityCompat.START, true)
//            return@setNavigationItemSelectedListener true
//        }
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    override fun onBackPressed() {
        if (binding.drawerLayout.isOpen) {
            binding.drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
    private fun handleDestinationChange(destination: NavDestination) {
        if (destination.id == R.id.nav_notes) {
            binding.appBarMain.fab.show()
//            supportActionBar?.show()
//            binding.appBarMain.bottomAppBar.visibility()
        } else {
//            supportActionBar?.hide()
//            setSupportActionBar(binding.appBarMain.toolbarDeletedFragment)
            binding.appBarMain.fab.hide()
        }
//        binding.EnterSearchKeyword.isVisible = (destination.id == androidx.navigation.R.id.Search)
    }
    private fun goToActivity(activity: Class<*>, baseNote: BaseNote? = null) {
        val intent = Intent(this, activity)
        intent.putExtra(Constants.SelectedBaseNote, baseNote)
        startActivity(intent)
    }
}