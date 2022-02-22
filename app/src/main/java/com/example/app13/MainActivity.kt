package com.example.app13

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.NavDestination
import com.example.app13.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    internal lateinit var binding: ActivityMainBinding
    private val model: BaseNoteModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.isSaveFromParentEnabled = false
        setSupportActionBar(binding.appBarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_notes, R.id.nav_deleted, R.id.nav_archived, R.id.nav_search
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
        binding.appBarMain.toolbar.setOnClickListener {
            navController.navigate(R.id.NotesToSearch)
        }
        binding.appBarMain.searchLayout.setStartIconOnClickListener {
            hideSoftKeyboard(binding.appBarMain.EnterSearchKeyword)
            super.onBackPressed()
        }


//        binding.appBarMain.toolbarNormal.setNavigationOnClickListener {
//            val navController = findNavController(R.id.nav_host_fragment_content_main)
//            navController.navigateUp(appBarConfiguration)
//        }


        setupSearch()
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
        when (destination.id) {
            R.id.nav_notes -> {
                binding.appBarMain.fab.show()
                binding.appBarMain.cosmeticView.setBackgroundColor(Color.parseColor("#CDFFFFFF"))
                binding.appBarMain.bottomAppBar.performShow()
            }
            R.id.nav_search -> {
                binding.appBarMain.fab.hide()
                binding.appBarMain.bottomAppBar.performHide()
                binding.appBarMain.cosmeticView.setBackgroundColor(Color.WHITE)
                showSoftKeyboard(binding.appBarMain.EnterSearchKeyword)
            }


            R.id.nav_deleted -> {
                binding.appBarMain.fab.hide()
                binding.appBarMain.bottomAppBar.performHide()
                binding.appBarMain.cosmeticView.setBackgroundColor(Color.WHITE)
//                binding.appBarMain.toolbarNormal.title = "Deleted"
//                binding.appBarMain.toolbarNormal.inflateMenu(R.menu.delete_all)
            }


            else -> {
                binding.appBarMain.fab.hide()
//                binding.appBarMain.cosmeticView.setBackgroundColor(Color.TRANSPARENT)
                binding.appBarMain.cosmeticView.setBackgroundColor(Color.WHITE)

            }
        }
        binding.appBarMain.toolbar.isVisible = (destination.id == R.id.nav_notes)
        binding.appBarMain.toolbarSearch.isVisible = (destination.id == R.id.nav_search)
//        binding.appBarMain.toolbarNormal.isVisible = (destination.id == R.id.nav_deleted) || (destination.id == R.id.nav_archived)
        binding.appBarMain.toolbarNormal.isVisible = (destination.id == R.id.nav_deleted) || (destination.id == R.id.nav_archived)
    }

    private fun goToActivity(activity: Class<*>, baseNote: BaseNote? = null) {
        val intent = Intent(this, activity)
        intent.putExtra(Constants.SelectedBaseNote, baseNote)
        startActivity(intent)
    }
    private fun setupSearch() {
        binding.appBarMain.EnterSearchKeyword.setText(model.keyword)
        binding.appBarMain.EnterSearchKeyword.addTextChangedListener(onTextChanged = { text, start, count, after ->
            model.keyword = text?.trim()?.toString() ?: String()
        })
    }
    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
    private fun hideSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}

