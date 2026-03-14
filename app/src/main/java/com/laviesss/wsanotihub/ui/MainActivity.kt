package com.laviesss.wsanotihub.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.color.DynamicColors
import com.laviesss.wsanotihub.R
import com.laviesss.wsanotihub.billing.ProManager
import com.laviesss.wsanotihub.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProManager.init(applicationContext)

        setupNavigation()

        if (!isNotificationServiceEnabled()) {
            showPermissionDialog()
        }

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }
    }

    private fun setupNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_all, R.id.nav_by_app, R.id.nav_history -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(packageName)
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.notification_access_msg)
            .setPositiveButton(R.string.go_to_settings) { _, _ ->
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
