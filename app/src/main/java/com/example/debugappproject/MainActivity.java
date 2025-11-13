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

/**
 * Main activity with bottom navigation for Mimo-style experience.
 * Contains: Learn, Bug of Day, Profile, Settings tabs.
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Define top-level destinations (no back button shown)
        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.learningPathsFragment);
        topLevelDestinations.add(R.id.bugOfTheDayFragment);
        topLevelDestinations.add(R.id.profileFragment);
        topLevelDestinations.add(R.id.settingsFragment);

        appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Setup bottom navigation
        setupBottomNavigation();

        // Handle deep linking from notifications
        handleDeepLink();
    }

    /**
     * Configure bottom navigation to work with Navigation Component.
     */
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_learn) {
                navController.navigate(R.id.learningPathsFragment);
                return true;
            } else if (itemId == R.id.navigation_bug_of_day) {
                navController.navigate(R.id.bugOfTheDayFragment);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                navController.navigate(R.id.profileFragment);
                return true;
            } else if (itemId == R.id.navigation_settings) {
                navController.navigate(R.id.settingsFragment);
                return true;
            }

            return false;
        });

        // Highlight correct bottom nav item when destination changes
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();

            if (destId == R.id.learningPathsFragment) {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_learn);
            } else if (destId == R.id.bugOfTheDayFragment) {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_bug_of_day);
            } else if (destId == R.id.profileFragment) {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_profile);
            } else if (destId == R.id.settingsFragment) {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_settings);
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
