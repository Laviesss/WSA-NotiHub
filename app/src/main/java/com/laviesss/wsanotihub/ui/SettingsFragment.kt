package com.laviesss.wsanotihub.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.laviesss.wsanotihub.R
import com.laviesss.wsanotihub.billing.ProManager
import com.laviesss.wsanotihub.data.NotiHubDatabase
import com.laviesss.wsanotihub.service.ApiServerService
import com.laviesss.wsanotihub.service.FloatingPanelService
import kotlinx.coroutines.launch

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "automation_rules" -> {
                if (!ProManager.isPro()) {
                    ProUpgradeDialog().show(parentFragmentManager, "pro_upgrade")
                    return true
                }
            }
            "kofi" -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/laviesss"))
                startActivity(intent)
                return true
            }
            "github_sponsors" -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sponsors/Laviesss"))
                startActivity(intent)
                return true
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<SwitchPreferenceCompat>("floating_panel")?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                if (!ProManager.isPro()) {
                    ProUpgradeDialog().show(parentFragmentManager, "pro_upgrade")
                    return@setOnPreferenceChangeListener false
                }
                if (checkOverlayPermission()) {
                    startFloatingPanel()
                } else {
                    requestOverlayPermission()
                    return@setOnPreferenceChangeListener false
                }
            } else {
                stopFloatingPanel()
            }
            true
        }

        findPreference<SwitchPreferenceCompat>("api_bridge")?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                if (!ProManager.isPro()) {
                    ProUpgradeDialog().show(parentFragmentManager, "pro_upgrade")
                    return@setOnPreferenceChangeListener false
                }
                startApiBridge()
            } else {
                stopApiBridge()
            }
            true
        }

        findPreference<Preference>("clear_all")?.setOnPreferenceClickListener {
            lifecycleScope.launch {
                NotiHubDatabase.getDatabase(requireContext()).notificationDao().clearAll()
                Toast.makeText(context, "Cleared all notifications", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(requireContext())
        } else true
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))
            startActivity(intent)
            Toast.makeText(context, R.string.overlay_permission_msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun startFloatingPanel() {
        val intent = Intent(requireContext(), FloatingPanelService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
    }

    private fun stopFloatingPanel() {
        requireContext().stopService(Intent(requireContext(), FloatingPanelService::class.java))
    }

    private fun startApiBridge() {
        val intent = Intent(requireContext(), ApiServerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
    }

    private fun stopApiBridge() {
        requireContext().stopService(Intent(requireContext(), ApiServerService::class.java))
    }
}
