package com.laviesss.wsanotihub.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.laviesss.wsanotihub.billing.ProManager

class ProUpgradeDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (ProManager.isPro()) {
            dismiss()
            return super.onCreateDialog(savedInstanceState)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("WSA NotiHub Pro")
            .setMessage("Upgrade to Pro to unlock:\n" +
                    "• Floating Overlay Panel\n" +
                    "• Automation Rules Engine\n" +
                    "• Local API Bridge\n" +
                    "• OTP Auto-Copy button")
            .setPositiveButton("Upgrade — One-Time Purchase") { _, _ ->
                ProManager.launchPurchaseFlow(requireActivity())
            }
            .setNegativeButton("Maybe Later", null)
            .create()
    }
}
