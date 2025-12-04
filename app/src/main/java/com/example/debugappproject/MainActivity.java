package com.example.debugappproject;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.debugappproject.databinding.ActivityMainBinding;
import com.example.debugappproject.util.ThemeManager;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Main activity with bottom navigation.
 * Handles navigation between main screens and theme application.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private boolean isNavigationSetup = false;
    private NavController.OnDestinationChangedListener destinationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        ThemeManager.getInstance(this).applyTheme();
        
        super.onCreate(savedInstanceState);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        android.util.Log.d("MainActivity", "onCreate completed");
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
        binding.getRoot().post(this::setupNavigation);
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

            // Setup bottom navigation with NavigationUI
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

            // Create destination listener (store reference to remove later)
            destinationListener = (controller, destination, arguments) -> {
                int destId = destination.getId();
                
                // Hide bottom nav for these screens
                boolean hideBottomNav = (destId == R.id.splashFragment ||
                        destId == R.id.bugDetailFragment ||
                        destId == R.id.practiceFragment ||
                        destId == R.id.battleArenaFragment ||
                        destId == R.id.pathDetailFragment ||
                        destId == R.id.settingsFragment);

                if (binding != null && binding.cardBottomNav != null) {
                    binding.cardBottomNav.setVisibility(hideBottomNav ? View.GONE : View.VISIBLE);
                }
            };
            
            navController.addOnDestinationChangedListener(destinationListener);

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
    
    @Override
    protected void onDestroy() {
        // Remove listener to prevent memory leaks
        if (navController != null && destinationListener != null) {
            navController.removeOnDestinationChangedListener(destinationListener);
        }
        binding = null;
        super.onDestroy();
    }
}
