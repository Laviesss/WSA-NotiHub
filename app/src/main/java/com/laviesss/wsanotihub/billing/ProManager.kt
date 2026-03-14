package com.laviesss.wsanotihub.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*

object ProManager {
    // ---------------------------------------------------------------
    // BILLING FEATURE FLAG
    // false = app is fully free, all Pro features unlocked, billing
    //         library is dormant. Use this for sideloaded distribution.
    // true  = Google Play Billing is active, Pro gates are enforced.
    //         Only use this when distributing via Google Play Store.
    // ---------------------------------------------------------------
    private const val BILLING_ENABLED = false

    private const val TAG = "ProManager"
    private const val PREF_NAME = "pro_prefs"
    private const val KEY_IS_PRO = "is_pro"
    private const val PRODUCT_ID = "pro_unlock"

    private var isPro: Boolean = false
    private lateinit var billingClient: BillingClient
    private var productDetails: ProductDetails? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "User canceled the purchase flow")
        } else {
            Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
        }
    }

    fun init(context: Context) {
        if (!BILLING_ENABLED) return

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        isPro = prefs.getBoolean(KEY_IS_PRO, false)

        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        startConnection(context)
    }

    private fun startConnection(context: Context) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases(context)
                    queryProductDetails()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to reconnect?
            }
        })
    }

    private fun queryPurchases(context: Context) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var foundPro = false
                for (purchase in purchases) {
                    if (purchase.products.contains(PRODUCT_ID) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        foundPro = true
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase, context)
                        }
                    }
                }
                setProStatus(context, foundPro)
            }
        }
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = productDetailsList.find { it.productId == PRODUCT_ID }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        if (!BILLING_ENABLED) return

        val details = productDetails
        if (details == null) {
            Log.e(TAG, "Product details not loaded")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        isPro = true
                    }
                }
            } else {
                isPro = true
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase, context: Context) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                setProStatus(context, true)
            }
        }
    }

    private fun setProStatus(context: Context, status: Boolean) {
        isPro = status
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_PRO, status)
            .apply()
    }

    fun isPro(): Boolean {
        if (!BILLING_ENABLED) return true
        return isPro
    }
}
