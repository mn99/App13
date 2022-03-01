package com.example.app13

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavDestination
import com.example.app13.databinding.ActivityMainBinding
import com.example.app13.databinding.ContentMainBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    internal lateinit var binding: ActivityMainBinding
    private val model: BaseNoteModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        binding.appBarMain.toolbarDeleted.setNavigationOnClickListener {
            navController.navigateUp(appBarConfiguration)
        }

        binding.appBarMain.toolbarArchived.setNavigationOnClickListener {
            navController.navigateUp(appBarConfiguration)
        }
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
                binding.appBarMain.cosmeticView.setBackgroundColor(Color.TRANSPARENT)
                showSoftKeyboard(binding.appBarMain.EnterSearchKeyword)
            }


            R.id.nav_deleted -> {
                binding.appBarMain.fab.hide()
                binding.appBarMain.bottomAppBar.performHide()
                binding.appBarMain.cosmeticView.setBackgroundColor(Color.TRANSPARENT)
            }
            R.id.nav_archived -> {
                binding.appBarMain.fab.hide()
                binding.appBarMain.bottomAppBar.performHide()
                binding.appBarMain.cosmeticView.setBackgroundColor(Color.TRANSPARENT)
            }

            else -> {
                binding.appBarMain.fab.hide()
//                binding.appBarMain.cosmeticView.setBackgroundColor(Color.TRANSPARENT)
                binding.appBarMain.cosmeticView.setBackgroundColor(Color.WHITE)

            }
        }
        binding.appBarMain.toolbar.isVisible = (destination.id == R.id.nav_notes)
        binding.appBarMain.toolbarSearch.isVisible = (destination.id == R.id.nav_search)
        binding.appBarMain.toolbarDeleted.isVisible = (destination.id == R.id.nav_deleted)
        binding.appBarMain.toolbarArchived.isVisible = (destination.id == R.id.nav_archived)
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

    fun confirmDeletionOfAllNotes(item: MenuItem) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.delete_all_notes)
            .setPositiveButton(R.string.delete) { dialog, which ->
                model.deleteAllBaseNotes()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}

