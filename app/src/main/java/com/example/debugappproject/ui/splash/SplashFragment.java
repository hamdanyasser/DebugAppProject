package com.example.debugappproject.ui.splash;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.data.seeding.DatabaseSeeder;
import com.example.debugappproject.databinding.FragmentSplashBinding;
import com.example.debugappproject.util.Constants;

/**
 * SplashFragment - Welcome screen that shows app logo and name.
 * Auto-navigates to HomeFragment after a short delay.
 * Also handles database seeding on first launch.
 */
public class SplashFragment extends Fragment {

    private FragmentSplashBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Seed database in background
        new Thread(() -> {
            BugRepository repository = new BugRepository(requireActivity().getApplication());
            DatabaseSeeder.seedDatabase(requireContext(), repository);
        }).start();

        // Navigate to home after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                Navigation.findNavController(view).navigate(
                    R.id.action_splash_to_home
                );
            }
        }, Constants.SPLASH_DELAY_MS);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
