package com.example.debugappproject.ui.premium;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Premium subscription screen.
 */
public class PremiumFragment extends Fragment implements BillingManager.BillingCallback {

    private BillingManager billingManager;
    private RadioButton radioMonthly;
    private RadioButton radioYearly;
    private MaterialButton buttonSubscribe;
    private boolean isYearlySelected = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_premium, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        billingManager = BillingManager.getInstance(requireContext());
        billingManager.setCallback(this);

        radioMonthly = view.findViewById(R.id.radio_monthly);
        radioYearly = view.findViewById(R.id.radio_yearly);
        buttonSubscribe = view.findViewById(R.id.button_subscribe);
        MaterialCardView cardMonthly = view.findViewById(R.id.card_monthly);
        MaterialCardView cardYearly = view.findViewById(R.id.card_yearly);

        View closeButton = view.findViewById(R.id.button_close);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                Navigation.findNavController(v).navigateUp();
            });
        }

        if (cardMonthly != null) cardMonthly.setOnClickListener(v -> selectPlan(false));
        if (cardYearly != null) cardYearly.setOnClickListener(v -> selectPlan(true));
        if (radioMonthly != null) radioMonthly.setOnClickListener(v -> selectPlan(false));
        if (radioYearly != null) radioYearly.setOnClickListener(v -> selectPlan(true));

        if (buttonSubscribe != null) {
            buttonSubscribe.setOnClickListener(v -> subscribe());
        }

        View restoreView = view.findViewById(R.id.text_restore);
        if (restoreView != null) {
            restoreView.setOnClickListener(v -> {
                billingManager.refreshPurchases();
                Toast.makeText(requireContext(), "Checking purchases...", Toast.LENGTH_SHORT).show();
            });
        }

        billingManager.getIsProUser().observe(getViewLifecycleOwner(), isPro -> {
            if (isPro) {
                Toast.makeText(requireContext(), "🎉 You're already Pro!", Toast.LENGTH_LONG).show();
                try {
                    Navigation.findNavController(view).navigateUp();
                } catch (Exception e) {
                    // Ignore navigation errors
                }
            }
        });
    }

    private void selectPlan(boolean yearly) {
        isYearlySelected = yearly;
        if (radioYearly != null) radioYearly.setChecked(yearly);
        if (radioMonthly != null) radioMonthly.setChecked(!yearly);
        if (buttonSubscribe != null) {
            buttonSubscribe.setText(yearly ? "Subscribe - $39.99/year" : "Subscribe - $4.99/month");
        }
    }

    private void subscribe() {
        if (buttonSubscribe != null) {
            buttonSubscribe.setEnabled(false);
            buttonSubscribe.setText("Processing...");
        }
        String productId = isYearlySelected ? BillingManager.PRODUCT_YEARLY : BillingManager.PRODUCT_MONTHLY;
        
        // Use demo mode if enabled
        if (BillingManager.isDemoMode()) {
            // Simulate a short delay for realism
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                billingManager.demoPurchase(productId);
            }, 1000);
        } else {
            billingManager.purchaseSubscription(requireActivity(), productId);
        }
    }

    @Override
    public void onPurchaseSuccess(String productId) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "🎉 Welcome to Pro!", Toast.LENGTH_LONG).show();
                try {
                    Navigation.findNavController(requireView()).navigateUp();
                } catch (Exception e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public void onPurchaseFailed(String error) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (buttonSubscribe != null) {
                    buttonSubscribe.setEnabled(true);
                }
                selectPlan(isYearlySelected);
                Toast.makeText(requireContext(), "Purchase failed: " + error, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onBillingReady() {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (billingManager != null) {
            billingManager.clearCallback();
        }
    }
}
