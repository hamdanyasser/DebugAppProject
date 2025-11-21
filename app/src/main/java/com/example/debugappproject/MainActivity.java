package com.example.debugappproject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.debugappproject.databinding.ActivityMainBinding;
import com.example.debugappproject.util.NotificationHelper;

import java.util.HashSet;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Main activity with bottom navigation for Mimo-style experience.
 * Contains: Learn, Bug of Day, Profile, Settings tabs.
 * Uses Hilt for dependency injection.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            android.util.Log.d("MainActivity", "onCreate started");

            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            android.util.Log.d("MainActivity", "Content view set");

            setSupportActionBar(binding.toolbar);
            android.util.Log.d("MainActivity", "Toolbar set");

            navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            android.util.Log.d("MainActivity", "NavController found");

            // Define top-level destinations (no back button shown)
            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.learningPathsFragment);
            topLevelDestinations.add(R.id.bugOfTheDayFragment);
            topLevelDestinations.add(R.id.profileFragment);
            topLevelDestinations.add(R.id.settingsFragment);

            appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            android.util.Log.d("MainActivity", "Navigation UI configured");

            // Setup bottom navigation
            setupBottomNavigation();
            android.util.Log.d("MainActivity", "Bottom navigation setup complete");

            // Handle deep linking from notifications
            handleDeepLink();
            android.util.Log.d("MainActivity", "onCreate completed successfully");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error in onCreate", e);
            e.printStackTrace();
        }
    }

    /**
     * Configure bottom navigation to work with Navigation Component.
     */
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            int targetDestination = -1;

            if (itemId == R.id.navigation_learn) {
                targetDestination = R.id.learningPathsFragment;
            } else if (itemId == R.id.navigation_bug_of_day) {
                targetDestination = R.id.bugOfTheDayFragment;
            } else if (itemId == R.id.navigation_profile) {
                targetDestination = R.id.profileFragment;
            } else if (itemId == R.id.navigation_settings) {
                targetDestination = R.id.settingsFragment;
            }

            // Only navigate if we're not already at the destination (prevents infinite loop)
            if (targetDestination != -1 && navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() != targetDestination) {
                navController.navigate(targetDestination);
                return true;
            }

            return targetDestination != -1;
        });

        // Highlight correct bottom nav item when destination changes
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            try {
                int destId = destination.getId();
                android.util.Log.d("MainActivity", "Navigated to destination: " + destination.getLabel());

                // Hide bottom navigation and toolbar for splash screen
                if (destId == R.id.splashFragment) {
                    binding.bottomNavigation.setVisibility(android.view.View.GONE);
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().hide();
                    }
                    return;
                }

                // Show bottom navigation and toolbar for main screens
                binding.bottomNavigation.setVisibility(android.view.View.VISIBLE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                }

                // Update selected item - only if it's not already selected to prevent infinite loop
                int targetItemId = -1;
                if (destId == R.id.learningPathsFragment) {
                    targetItemId = R.id.navigation_learn;
                } else if (destId == R.id.bugOfTheDayFragment) {
                    targetItemId = R.id.navigation_bug_of_day;
                } else if (destId == R.id.profileFragment) {
                    targetItemId = R.id.navigation_profile;
                } else if (destId == R.id.settingsFragment) {
                    targetItemId = R.id.navigation_settings;
                }

                // Only update if different to prevent triggering the listener again
                if (targetItemId != -1 && binding.bottomNavigation.getSelectedItemId() != targetItemId) {
                    binding.bottomNavigation.setSelectedItemId(targetItemId);
                }
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error in destination listener", e);
            }
        });
    }

    /**
     * Handles deep linking from notifications.
     * Navigates to the appropriate screen based on intent extras.
     */
    private void handleDeepLink() {
        if (getIntent() != null && getIntent().hasExtra(NotificationHelper.EXTRA_NAVIGATE_TO)) {
            String destination = getIntent().getStringExtra(NotificationHelper.EXTRA_NAVIGATE_TO);

            if (NotificationHelper.DESTINATION_BUG_OF_DAY.equals(destination)) {
                // Navigate to Bug of the Day screen
                navController.navigate(R.id.bugOfTheDayFragment);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
