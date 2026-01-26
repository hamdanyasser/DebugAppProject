package com.example.debugappproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.debugmaster.app.R;
import com.debugmaster.app.databinding.ActivityMainBinding;
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
    private ConnectivityManager.NetworkCallback networkCallback;
    private View offlineBanner;

    // Permission launcher for POST_NOTIFICATIONS (Android 13+)
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // User's choice is respected - app works with or without notifications
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        ThemeManager.getInstance(this).applyTheme();
        
        super.onCreate(savedInstanceState);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup offline banner
        offlineBanner = findViewById(R.id.offline_banner);
        setupNetworkListener();

        // Request notification permission for Android 13+ (required for push notifications)
        requestNotificationPermissionIfNeeded();

        android.util.Log.d("MainActivity", "onCreate completed");
    }

    /**
     * Setup network connectivity listener to show/hide offline banner
     */
    private void setupNetworkListener() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> {
                    if (offlineBanner != null) {
                        offlineBanner.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> {
                    if (offlineBanner != null) {
                        offlineBanner.setVisibility(View.VISIBLE);
                    }
                });
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        cm.registerNetworkCallback(request, networkCallback);

        // Check initial state
        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork == null && offlineBanner != null) {
            offlineBanner.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Request POST_NOTIFICATIONS permission on Android 13+ (API 33+).
     * This is required for the app to show notifications.
     */
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
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
                
                // Hide bottom nav for immersive game screens
                boolean hideBottomNav = (destId == R.id.splashFragment ||
                        destId == R.id.bugDetailFragment ||
                        destId == R.id.practiceFragment ||
                        destId == R.id.battleArenaFragment ||
                        destId == R.id.pathDetailFragment ||
                        destId == R.id.settingsFragment ||
                        destId == R.id.beginnerTutorialFragment ||
                        destId == R.id.gameSessionFragment ||
                        destId == R.id.interactiveLessonFragment ||
                        destId == R.id.coopFragment ||
                        destId == R.id.debuggerFragment ||
                        destId == R.id.multiFileBugFragment ||
                        destId == R.id.githubImportFragment ||
                        destId == R.id.gameModesFragment ||
                        destId == R.id.mentorChatFragment);

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

        // Unregister network callback
        if (networkCallback != null) {
            try {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                cm.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error unregistering network callback", e);
            }
        }

        binding = null;
        super.onDestroy();
    }
}
