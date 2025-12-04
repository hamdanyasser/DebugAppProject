package com.example.debugappproject.ui.auth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.databinding.FragmentAuthBinding;
import com.example.debugappproject.util.AuthManager;
import com.example.debugappproject.util.SoundManager;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - LOGIN & REGISTER SCREEN                              ║
 * ║              Beautiful Auth Experience with Animations                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class AuthFragment extends Fragment {

    private FragmentAuthBinding binding;
    private AuthManager authManager;
    private SoundManager soundManager;
    private boolean isLoginMode = true;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAuthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = AuthManager.getInstance(requireContext());
        soundManager = SoundManager.getInstance(requireContext());

        // Check if already logged in
        if (authManager.isLoggedInSync()) {
            navigateToHome();
            return;
        }

        setupUI();
        playEntranceAnimations();
    }

    private void setupUI() {
        // Toggle between login and register
        binding.textToggleMode.setOnClickListener(v -> {
            soundManager.playButtonClick();
            toggleMode();
        });

        // Login/Register button
        binding.buttonSubmit.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BUTTON_START);
            if (isLoginMode) {
                performLogin();
            } else {
                performRegister();
            }
        });

        // Guest mode
        binding.buttonGuest.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.POWER_UP);
            authManager.loginAsGuest();
            navigateToHome();
        });

        // Input validation
        setupInputValidation();

        // Password visibility toggle
        binding.buttonTogglePassword.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BLIP);
            togglePasswordVisibility();
        });
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        
        // Animate transition
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(binding.cardAuth, "alpha", 1f, 0f);
        fadeOut.setDuration(150);
        fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                updateUIForMode();
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.cardAuth, "alpha", 0f, 1f);
                fadeIn.setDuration(200);
                fadeIn.start();
            }
        });
        fadeOut.start();
    }

    private void updateUIForMode() {
        if (isLoginMode) {
            binding.textTitle.setText("Welcome Back! 👋");
            binding.textSubtitle.setText("Login to continue your debugging journey");
            binding.layoutUsername.setVisibility(View.GONE);
            binding.layoutConfirmPassword.setVisibility(View.GONE);
            binding.buttonSubmit.setText("🔓 Login");
            binding.textToggleMode.setText("Don't have an account? Register");
        } else {
            binding.textTitle.setText("Join the Squad! 🚀");
            binding.textSubtitle.setText("Create an account to start debugging");
            binding.layoutUsername.setVisibility(View.VISIBLE);
            binding.layoutConfirmPassword.setVisibility(View.VISIBLE);
            binding.buttonSubmit.setText("🎮 Create Account");
            binding.textToggleMode.setText("Already have an account? Login");
        }
        
        // Clear inputs
        binding.inputEmail.setText("");
        binding.inputPassword.setText("");
        binding.inputUsername.setText("");
        binding.inputConfirmPassword.setText("");
        binding.textError.setVisibility(View.GONE);
    }

    private void performLogin() {
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        setLoading(true);
        
        handler.postDelayed(() -> {
            AuthManager.AuthResult result = authManager.login(email, password);
            
            if (result == AuthManager.AuthResult.SUCCESS) {
                soundManager.playSound(SoundManager.Sound.SUCCESS);
                Toast.makeText(requireContext(), "Welcome back! 🎉", Toast.LENGTH_SHORT).show();
                navigateToHome();
            } else {
                soundManager.playSound(SoundManager.Sound.ERROR);
                showError(AuthManager.getErrorMessage(result));
                setLoading(false);
                shakeForm();
            }
        }, 800); // Simulated delay for UX
    }

    private void performRegister() {
        String username = binding.inputUsername.getText().toString().trim();
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString();
        String confirmPassword = binding.inputConfirmPassword.getText().toString();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords don't match");
            return;
        }

        setLoading(true);
        
        handler.postDelayed(() -> {
            AuthManager.AuthResult result = authManager.register(username, email, password, username);
            
            if (result == AuthManager.AuthResult.SUCCESS) {
                soundManager.playSound(SoundManager.Sound.LEVEL_UP);
                Toast.makeText(requireContext(), "Account created! Welcome! 🎊", Toast.LENGTH_SHORT).show();
                navigateToHome();
            } else {
                soundManager.playSound(SoundManager.Sound.ERROR);
                showError(AuthManager.getErrorMessage(result));
                setLoading(false);
                shakeForm();
            }
        }, 1000);
    }

    private void setupInputValidation() {
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.textError.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.inputEmail.addTextChangedListener(clearErrorWatcher);
        binding.inputPassword.addTextChangedListener(clearErrorWatcher);
        binding.inputUsername.addTextChangedListener(clearErrorWatcher);
        binding.inputConfirmPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void togglePasswordVisibility() {
        int inputType = binding.inputPassword.getInputType();
        if (inputType == 129) { // Password hidden
            binding.inputPassword.setInputType(1); // Text visible
            binding.inputConfirmPassword.setInputType(1);
            binding.buttonTogglePassword.setText("🙈");
        } else {
            binding.inputPassword.setInputType(129); // Password hidden
            binding.inputConfirmPassword.setInputType(129);
            binding.buttonTogglePassword.setText("👁️");
        }
        binding.inputPassword.setSelection(binding.inputPassword.length());
    }

    private void showError(String message) {
        binding.textError.setText(message);
        binding.textError.setVisibility(View.VISIBLE);
        
        // Animate error appearance
        binding.textError.setAlpha(0f);
        binding.textError.animate().alpha(1f).setDuration(200).start();
    }

    private void setLoading(boolean loading) {
        binding.buttonSubmit.setEnabled(!loading);
        binding.buttonGuest.setEnabled(!loading);
        binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonSubmit.setText(loading ? "Please wait..." : 
            (isLoginMode ? "🔓 Login" : "🎮 Create Account"));
    }

    private void shakeForm() {
        ObjectAnimator shake = ObjectAnimator.ofFloat(binding.cardAuth, "translationX", 
            0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(500);
        shake.start();
    }

    private void playEntranceAnimations() {
        // Logo animation
        binding.imageLogo.setScaleX(0f);
        binding.imageLogo.setScaleY(0f);
        binding.imageLogo.animate()
            .scaleX(1f).scaleY(1f)
            .setDuration(600)
            .setInterpolator(new OvershootInterpolator(1.5f))
            .start();

        // Title slide in
        binding.textTitle.setTranslationY(-50f);
        binding.textTitle.setAlpha(0f);
        binding.textTitle.animate()
            .translationY(0f).alpha(1f)
            .setDuration(500)
            .setStartDelay(200)
            .start();

        // Card slide up
        binding.cardAuth.setTranslationY(100f);
        binding.cardAuth.setAlpha(0f);
        binding.cardAuth.animate()
            .translationY(0f).alpha(1f)
            .setDuration(500)
            .setStartDelay(350)
            .start();

        // Guest button fade in
        binding.buttonGuest.setAlpha(0f);
        binding.buttonGuest.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(600)
            .start();
    }

    private void navigateToHome() {
        try {
            Navigation.findNavController(requireView()).navigate(R.id.action_auth_to_home);
        } catch (Exception e) {
            // Navigate with popBackStack
            Navigation.findNavController(requireView()).popBackStack();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
