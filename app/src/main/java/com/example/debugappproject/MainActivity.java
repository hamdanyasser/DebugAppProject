package com.example.debugappproject;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.debugappproject.databinding.ActivityMainBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Main activity with bottom navigation.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private boolean isNavigationSetup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        android.util.Log.d("MainActivity", "onCreate completed - waiting for fragments");
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Setup navigation only once, after fragments are ready
        if (!isNavigationSetup) {
            setupNavigationDelayed();
        }
    }

    private void setupNavigationDelayed() {
        // Use a small delay to ensure NavHostFragment is ready
        binding.getRoot().post(() -> {
            try {
                setupNavigation();
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error setting up navigation", e);
            }
        });
    }

    private void setupNavigation() {
        try {
            // Get NavHostFragment
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_content_main);

            if (navHostFragment == null) {
                android.util.Log.e("MainActivity", "NavHostFragment is null!");
                return;
            }

            navController = navHostFragment.getNavController();
            android.util.Log.d("MainActivity", "NavController obtained");

            // Setup bottom navigation with NavigationUI
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
            android.util.Log.d("MainActivity", "Bottom navigation setup complete");

            // Setup destination listener for showing/hiding bottom nav
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                
                // Hide bottom nav for these screens
                boolean hideBottomNav = (destId == R.id.splashFragment ||
                        destId == R.id.bugDetailFragment ||
                        destId == R.id.practiceFragment ||
                        destId == R.id.battleArenaFragment ||
                        destId == R.id.pathDetailFragment ||
                        destId == R.id.settingsFragment);

                if (binding.cardBottomNav != null) {
                    binding.cardBottomNav.setVisibility(hideBottomNav ? View.GONE : View.VISIBLE);
                }
                
                android.util.Log.d("MainActivity", "Navigated to: " + destination.getLabel());
            });

            isNavigationSetup = true;
            android.util.Log.d("MainActivity", "Navigation setup complete!");
            
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Setup error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null) {
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
