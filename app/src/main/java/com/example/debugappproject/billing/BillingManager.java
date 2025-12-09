package com.example.debugappproject.billing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.List;

/**
 * BillingManager handles Google Play Billing for DebugMaster Pro subscriptions.
 * 
 * Products to create in Google Play Console:
 * - debugmaster_pro_monthly: $4.99/month
 * - debugmaster_pro_yearly: $39.99/year (33% savings)
 * - debugmaster_lifetime: $99.99 one-time
 */
public class BillingManager implements PurchasesUpdatedListener {

    private static final String TAG = "BillingManager";
    private static final String PREFS_NAME = "debugmaster_billing";
    private static final String KEY_IS_PRO = "is_pro_user";
    private static final String KEY_SUBSCRIPTION_TYPE = "subscription_type";
    
    // Product IDs - must match Google Play Console exactly
    public static final String PRODUCT_MONTHLY = "debugmaster_pro_monthly";
    public static final String PRODUCT_YEARLY = "debugmaster_pro_yearly";
    public static final String PRODUCT_LIFETIME = "debugmaster_lifetime";
    
    // Demo mode - set to true to bypass Play Store
    private static final boolean DEMO_MODE = true;

    // Singleton instance
    private static BillingManager instance;

    private final Context context;
    private BillingClient billingClient;
    private final MutableLiveData<Boolean> isProUser = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<ProductDetails>> productDetails = new MutableLiveData<>(new ArrayList<>());
    private boolean isConnecting = false;
    
    private BillingCallback callback;

    public interface BillingCallback {
        void onPurchaseSuccess(String productId);
        void onPurchaseFailed(String error);
        void onBillingReady();
        default void onPricesLoaded() {}
        default void onProductsLoaded(List<ProductDetails> products) {}
    }

    /**
     * Get singleton instance of BillingManager
     */
    public static synchronized BillingManager getInstance(Context context) {
        if (instance == null) {
            instance = new BillingManager(context);
        }
        return instance;
    }

    private BillingManager(Context context) {
        this.context = context.getApplicationContext();
        loadProStatus();
        initBillingClient();
    }

    private void loadProStatus() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isProUser.postValue(prefs.getBoolean(KEY_IS_PRO, false));
    }

    private void saveProStatus(boolean isPro, String subscriptionType) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_IS_PRO, isPro);
        editor.putString(KEY_SUBSCRIPTION_TYPE, subscriptionType);
        editor.apply();
        isProUser.postValue(isPro);
    }

    private void initBillingClient() {
        if (billingClient != null) {
            try {
                billingClient.endConnection();
            } catch (Exception e) {
                // Ignore - client might already be disconnected
            }
        }
        
        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        startConnection();
    }

    private void startConnection() {
        if (billingClient == null) {
            initBillingClient();
            return;
        }
        
        if (billingClient.isReady()) {
            android.util.Log.d(TAG, "Billing client already ready");
            return;
        }
        
        if (isConnecting) {
            android.util.Log.d(TAG, "Already connecting...");
            return;
        }

        isConnecting = true;
        
        try {
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    isConnecting = false;
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        android.util.Log.d(TAG, "Billing client connected");
                        queryProducts();
                        queryExistingPurchases();
                        if (callback != null) {
                            callback.onBillingReady();
                        }
                    } else {
                        android.util.Log.e(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    isConnecting = false;
                    android.util.Log.w(TAG, "Billing client disconnected");
                    // Don't auto-retry to prevent loops
                }
            });
        } catch (Exception e) {
            isConnecting = false;
            android.util.Log.e(TAG, "Error starting billing connection", e);
        }
    }
    
    /**
     * Ensure billing client is ready, reconnecting if necessary
     */
    private void ensureConnected() {
        if (billingClient == null) {
            initBillingClient();
        } else if (!billingClient.isReady() && !isConnecting) {
            // Client exists but not ready - try to reinitialize
            initBillingClient();
        }
    }

    private void queryProducts() {
        if (billingClient == null || !billingClient.isReady()) {
            return;
        }
        
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        
        // Subscriptions
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build());
        
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, subsList) -> {
            List<ProductDetails> allProducts = new ArrayList<>();
            
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                allProducts.addAll(subsList);
            }

            // Query lifetime product
            List<QueryProductDetailsParams.Product> inAppList = new ArrayList<>();
            inAppList.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_LIFETIME)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());

            QueryProductDetailsParams inAppParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(inAppList)
                    .build();

            billingClient.queryProductDetailsAsync(inAppParams, (inAppResult, inAppProducts) -> {
                if (inAppResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    allProducts.addAll(inAppProducts);
                }
                
                productDetails.postValue(allProducts);
                
                if (callback != null) {
                    callback.onPricesLoaded();
                    callback.onProductsLoaded(allProducts);
                }
            });
        });
    }

    private void queryExistingPurchases() {
        if (billingClient == null || !billingClient.isReady()) {
            return;
        }
        
        // Check subscriptions
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                (billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        boolean hasActiveSub = false;
                        String activeProduct = "";
                        
                        for (Purchase purchase : purchases) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                hasActiveSub = true;
                                if (!purchase.getProducts().isEmpty()) {
                                    activeProduct = purchase.getProducts().get(0);
                                }
                                if (!purchase.isAcknowledged()) {
                                    acknowledgePurchase(purchase);
                                }
                            }
                        }
                        
                        if (hasActiveSub) {
                            saveProStatus(true, activeProduct);
                        } else {
                            checkLifetimePurchase();
                        }
                    }
                }
        );
    }

    private void checkLifetimePurchase() {
        if (billingClient == null || !billingClient.isReady()) {
            return;
        }
        
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                (billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        boolean hasLifetime = false;
                        
                        for (Purchase purchase : purchases) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                if (purchase.getProducts().contains(PRODUCT_LIFETIME)) {
                                    hasLifetime = true;
                                    if (!purchase.isAcknowledged()) {
                                        acknowledgePurchase(purchase);
                                    }
                                }
                            }
                        }
                        
                        if (hasLifetime) {
                            saveProStatus(true, PRODUCT_LIFETIME);
                        } else if (!DEMO_MODE || !isProUserSync()) {
                            // Only reset if not in demo mode or not already pro (preserve demo purchases)
                            saveProStatus(false, "");
                        }
                    }
                }
        );
    }

    public void purchaseSubscription(Activity activity, String productId) {
        ensureConnected();
        
        if (billingClient == null || !billingClient.isReady()) {
            if (callback != null) callback.onPurchaseFailed("Not connected to Play Store");
            return;
        }

        isLoading.postValue(true);

        List<ProductDetails> details = productDetails.getValue();
        if (details == null || details.isEmpty()) {
            isLoading.postValue(false);
            if (callback != null) callback.onPurchaseFailed("Products not loaded");
            return;
        }

        ProductDetails selectedProduct = null;
        for (ProductDetails pd : details) {
            if (pd.getProductId().equals(productId)) {
                selectedProduct = pd;
                break;
            }
        }

        if (selectedProduct == null) {
            isLoading.postValue(false);
            if (callback != null) callback.onPurchaseFailed("Product not found");
            return;
        }

        BillingFlowParams billingFlowParams;
        
        if (productId.equals(PRODUCT_LIFETIME)) {
            billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(List.of(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(selectedProduct)
                                    .build()))
                    .build();
        } else {
            List<ProductDetails.SubscriptionOfferDetails> offers = selectedProduct.getSubscriptionOfferDetails();
            if (offers == null || offers.isEmpty()) {
                isLoading.postValue(false);
                if (callback != null) callback.onPurchaseFailed("No offers available");
                return;
            }

            billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(List.of(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(selectedProduct)
                                    .setOfferToken(offers.get(0).getOfferToken())
                                    .build()))
                    .build();
        }

        isLoading.postValue(false);
        BillingResult result = billingClient.launchBillingFlow(activity, billingFlowParams);
        
        if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            if (callback != null) callback.onPurchaseFailed(result.getDebugMessage());
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        isLoading.postValue(false);
        
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            if (callback != null) callback.onPurchaseFailed("Purchase cancelled");
        } else {
            if (callback != null) callback.onPurchaseFailed(billingResult.getDebugMessage());
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            String productId = purchase.getProducts().isEmpty() ? "" : purchase.getProducts().get(0);
            saveProStatus(true, productId);
            
            if (!purchase.isAcknowledged()) {
                acknowledgePurchase(purchase);
            }
            
            if (callback != null) callback.onPurchaseSuccess(productId);
        }
    }

    private void acknowledgePurchase(Purchase purchase) {
        if (billingClient == null || !billingClient.isReady()) {
            return;
        }
        
        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(params, billingResult -> {
            android.util.Log.d(TAG, "Acknowledge result: " + billingResult.getResponseCode());
        });
    }

    public String getFormattedPrice(String productId) {
        List<ProductDetails> details = productDetails.getValue();
        if (details != null) {
            for (ProductDetails pd : details) {
                if (pd.getProductId().equals(productId)) {
                    if (productId.equals(PRODUCT_LIFETIME)) {
                        return pd.getOneTimePurchaseOfferDetails() != null 
                                ? pd.getOneTimePurchaseOfferDetails().getFormattedPrice() 
                                : "$99.99";
                    } else {
                        List<ProductDetails.SubscriptionOfferDetails> offers = pd.getSubscriptionOfferDetails();
                        if (offers != null && !offers.isEmpty()) {
                            List<ProductDetails.PricingPhase> phases = offers.get(0).getPricingPhases().getPricingPhaseList();
                            if (!phases.isEmpty()) {
                                return phases.get(0).getFormattedPrice();
                            }
                        }
                    }
                }
            }
        }
        
        // Fallback prices
        switch (productId) {
            case PRODUCT_MONTHLY: return "$4.99";
            case PRODUCT_YEARLY: return "$39.99";
            case PRODUCT_LIFETIME: return "$99.99";
            default: return "---";
        }
    }

    public void setCallback(BillingCallback callback) {
        this.callback = callback;
    }

    public void clearCallback() {
        this.callback = null;
    }

    public LiveData<Boolean> getIsProUser() { return isProUser; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<List<ProductDetails>> getProductDetails() { return productDetails; }

    public boolean isProUserSync() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_PRO, false);
    }

    public void refreshPurchases() {
        ensureConnected();
        if (billingClient != null && billingClient.isReady()) {
            queryExistingPurchases();
        }
    }

    /**
     * Called when a fragment is destroyed.
     * Does NOT end the billing connection since this is a singleton.
     * The connection will be maintained for the app lifecycle.
     */
    public void destroy() {
        // Don't actually destroy the connection - this is a singleton
        // Just clear the callback to prevent leaks
        callback = null;
        android.util.Log.d(TAG, "BillingManager callback cleared (singleton connection maintained)");
    }
    
    /**
     * Actually end the billing connection. 
     * Call this only when the app is being fully destroyed.
     */
    public static void destroyInstance() {
        if (instance != null) {
            if (instance.billingClient != null) {
                try {
                    instance.billingClient.endConnection();
                } catch (Exception e) {
                    // Ignore
                }
            }
            instance = null;
        }
    }

    /**
     * Demo mode purchase - bypasses Play Store for testing
     */
    public void demoPurchase(String productId) {
        if (DEMO_MODE) {
            android.util.Log.d(TAG, "Demo purchase: " + productId);
            saveProStatus(true, productId);
            if (callback != null) {
                callback.onPurchaseSuccess(productId);
            }
        }
    }

    /**
     * Demo mode - deactivate pro status
     */
    public void demoDeactivate() {
        if (DEMO_MODE) {
            saveProStatus(false, "");
        }
    }

    /**
     * Check if demo mode is enabled
     */
    public static boolean isDemoMode() {
        return DEMO_MODE;
    }
}
