package com.example.debugappproject.ui.learn;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.debugmaster.app.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.LearningPath;
import com.example.debugappproject.model.UserProgress;
import com.example.debugappproject.util.DailyBugHuntManager;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - REDESIGNED LEARN TAB                                 ║
 * ║        Premium game-like learning experience with                            ║
 * ║        filtering, search, Daily Bug Hunt, and more                          ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class LearningPathsFragmentV2 extends Fragment {

    private static final String TAG = "LearningPathsFragmentV2";

    // Views
    private View rootView;
    private TextView textLevel;
    private TextView textXp;
    private TextView textStreak;
    private EditText editSearch;
    private TextView buttonClearSearch;
    private TextView tabAll;
    private TextView tabFree;
    private TextView tabPro;
    private ChipGroup chipGroupCategories;
    private MaterialCardView cardContinue;
    private TextView textContinueEmoji;
    private TextView textContinuePathName;
    private ProgressBar progressContinue;
    private TextView textContinueProgress;
    private MaterialButton buttonContinue;
    private MaterialCardView cardDailyHunt;
    private TextView textDailyEmoji;
    private TextView textDailyTitle;
    private TextView textDailyDescription;
    private TextView textDailyStreak;
    private MaterialButton buttonStartDaily;
    private LinearLayout layoutDailyCompleted;
    private MaterialCardView cardProBanner;
    private MaterialButton buttonGoPro;
    private RecyclerView recyclerPaths;
    private LinearLayout layoutEmpty;
    private TextView textEmptyEmoji;
    private TextView textEmptyTitle;
    private TextView textEmptyMessage;
    private MaterialButton buttonClearFilters;
    private FrameLayout layoutLoading;

    // Data
    private LearningPathsViewModelV2 viewModel;
    private LearningPathAdapterV2 adapter;
    private BillingManager billingManager;
    private SoundManager soundManager;
    private DailyBugHuntManager dailyBugHuntManager;
    
    // State
    private int continuePathId = -1;
    private boolean isProUser = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_learning_paths_v2, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            initViews(view);
            initDependencies();
            setupRecyclerView();
            setupSearch();
            setupTabs();
            setupDailyBugHunt();
            setupProBanner();
            setupEmptyState();
            observeData();
            playEntranceAnimations();
            
            soundManager.playSound(SoundManager.Sound.TRANSITION);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in onViewCreated", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════

    private void initViews(View view) {
        textLevel = view.findViewById(R.id.text_level);
        textXp = view.findViewById(R.id.text_xp);
        textStreak = view.findViewById(R.id.text_streak);
        editSearch = view.findViewById(R.id.edit_search);
        buttonClearSearch = view.findViewById(R.id.button_clear_search);
        tabAll = view.findViewById(R.id.tab_all);
        tabFree = view.findViewById(R.id.tab_free);
        tabPro = view.findViewById(R.id.tab_pro);
        chipGroupCategories = view.findViewById(R.id.chip_group_categories);
        cardContinue = view.findViewById(R.id.card_continue);
        textContinueEmoji = view.findViewById(R.id.text_continue_emoji);
        textContinuePathName = view.findViewById(R.id.text_continue_path_name);
        progressContinue = view.findViewById(R.id.progress_continue);
        textContinueProgress = view.findViewById(R.id.text_continue_progress);
        buttonContinue = view.findViewById(R.id.button_continue);
        cardDailyHunt = view.findViewById(R.id.card_daily_hunt);
        textDailyEmoji = view.findViewById(R.id.text_daily_emoji);
        textDailyTitle = view.findViewById(R.id.text_daily_title);
        textDailyDescription = view.findViewById(R.id.text_daily_description);
        textDailyStreak = view.findViewById(R.id.text_daily_streak);
        buttonStartDaily = view.findViewById(R.id.button_start_daily);
        layoutDailyCompleted = view.findViewById(R.id.layout_daily_completed);
        cardProBanner = view.findViewById(R.id.card_pro_banner);
        buttonGoPro = view.findViewById(R.id.button_go_pro);
        recyclerPaths = view.findViewById(R.id.recycler_paths);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        textEmptyEmoji = view.findViewById(R.id.text_empty_emoji);
        textEmptyTitle = view.findViewById(R.id.text_empty_title);
        textEmptyMessage = view.findViewById(R.id.text_empty_message);
        buttonClearFilters = view.findViewById(R.id.button_clear_filters);
        layoutLoading = view.findViewById(R.id.layout_loading);
    }

    private void initDependencies() {
        viewModel = new ViewModelProvider(this).get(LearningPathsViewModelV2.class);
        billingManager = BillingManager.getInstance(requireContext());
        soundManager = SoundManager.getInstance(requireContext());
        dailyBugHuntManager = viewModel.getDailyBugHuntManager();
        
        isProUser = billingManager.isProUserSync();
        viewModel.setProStatus(isProUser);
    }

    private void setupRecyclerView() {
        adapter = new LearningPathAdapterV2();
        adapter.setProStatus(isProUser);
        recyclerPaths.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPaths.setAdapter(adapter);

        adapter.setOnPathClickListener(path -> {
            soundManager.playButtonClick();
            
            if (path.isLocked() && !isProUser) {
                showProUpgradeDialog(path);
            } else {
                soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
                navigateToPathDetail(path.getId());
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SEARCH
    // ═══════════════════════════════════════════════════════════════════════

    private void setupSearch() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                viewModel.setSearchQuery(query);
                buttonClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });

        buttonClearSearch.setOnClickListener(v -> {
            editSearch.setText("");
            viewModel.setSearchQuery("");
            hideKeyboard();
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TABS (All / Free / Pro)
    // ═══════════════════════════════════════════════════════════════════════

    private void setupTabs() {
        tabAll.setOnClickListener(v -> {
            soundManager.playButtonClick();
            selectTab(LearningPathsViewModelV2.FilterType.ALL);
        });

        tabFree.setOnClickListener(v -> {
            soundManager.playButtonClick();
            selectTab(LearningPathsViewModelV2.FilterType.FREE);
        });

        tabPro.setOnClickListener(v -> {
            soundManager.playButtonClick();
            selectTab(LearningPathsViewModelV2.FilterType.PRO);
        });

        // Initial state
        updateTabUI(LearningPathsViewModelV2.FilterType.ALL);
    }

    private void selectTab(LearningPathsViewModelV2.FilterType filter) {
        viewModel.setFilter(filter);
        updateTabUI(filter);
        
        // Show Pro banner when Pro tab is selected and user is not Pro
        if (filter == LearningPathsViewModelV2.FilterType.PRO && !isProUser) {
            cardProBanner.setVisibility(View.VISIBLE);
        } else {
            cardProBanner.setVisibility(View.GONE);
        }
    }

    private void updateTabUI(LearningPathsViewModelV2.FilterType filter) {
        // Reset all tabs to unselected state
        tabAll.setBackgroundResource(android.R.color.transparent);
        tabFree.setBackgroundResource(android.R.color.transparent);
        tabPro.setBackgroundResource(android.R.color.transparent);
        tabAll.setTextColor(Color.parseColor("#94A3B8"));
        tabFree.setTextColor(Color.parseColor("#94A3B8"));
        tabPro.setTextColor(Color.parseColor("#94A3B8"));
        tabAll.setTypeface(tabAll.getTypeface(), android.graphics.Typeface.NORMAL);
        tabFree.setTypeface(tabFree.getTypeface(), android.graphics.Typeface.NORMAL);
        tabPro.setTypeface(tabPro.getTypeface(), android.graphics.Typeface.NORMAL);

        // Highlight selected tab with gradient background
        TextView selectedTab;
        switch (filter) {
            case FREE:
                selectedTab = tabFree;
                break;
            case PRO:
                selectedTab = tabPro;
                break;
            default:
                selectedTab = tabAll;
                break;
        }
        selectedTab.setBackgroundResource(R.drawable.bg_tab_selected);
        selectedTab.setTextColor(Color.WHITE);
        selectedTab.setTypeface(selectedTab.getTypeface(), android.graphics.Typeface.BOLD);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CATEGORY CHIPS
    // ═══════════════════════════════════════════════════════════════════════

    private void setupCategoryChips(List<String> categories) {
        chipGroupCategories.removeAllViews();
        
        for (String category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            chip.setTextColor(Color.WHITE);
            chip.setTextSize(13);
            chip.setChipStrokeColorResource(R.color.chip_stroke_selector);
            chip.setChipStrokeWidth(1);
            
            if (category.equals(LearningPathsViewModelV2.CATEGORY_ALL)) {
                chip.setChecked(true);
            }
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    soundManager.playButtonClick();
                    viewModel.setCategory(category);
                }
            });
            
            chipGroupCategories.addView(chip);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DAILY BUG HUNT
    // ═══════════════════════════════════════════════════════════════════════

    private void setupDailyBugHunt() {
        updateDailyHuntUI();

        buttonStartDaily.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
            animateButton(v);
            startDailyChallenge();
        });
    }

    private void updateDailyHuntUI() {
        boolean completed = dailyBugHuntManager.isTodayCompleted();
        int streak = dailyBugHuntManager.getCurrentStreak();
        int xp = dailyBugHuntManager.calculateXpReward();
        int minutes = dailyBugHuntManager.getEstimatedMinutes();
        String title = dailyBugHuntManager.getTodayChallengeTitle();

        textDailyTitle.setText(title);
        textDailyDescription.setText("Quick " + minutes + "-min challenge • +" + xp + " XP");
        
        if (streak > 0) {
            textDailyStreak.setText("🔥 " + streak + " day streak");
            textDailyStreak.setVisibility(View.VISIBLE);
        } else {
            textDailyStreak.setVisibility(View.GONE);
        }

        if (completed) {
            buttonStartDaily.setVisibility(View.GONE);
            layoutDailyCompleted.setVisibility(View.VISIBLE);
        } else {
            buttonStartDaily.setVisibility(View.VISIBLE);
            layoutDailyCompleted.setVisibility(View.GONE);
        }
    }

    private void startDailyChallenge() {
        int bugId = dailyBugHuntManager.getTodayBugId();
        
        try {
            Bundle args = new Bundle();
            args.putInt("bugId", bugId);
            args.putBoolean("isDailyChallenge", true);
            
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_paths_to_bugDetail, args);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Navigation error", e);
            Toast.makeText(getContext(), "Could not start challenge", Toast.LENGTH_SHORT).show();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CONTINUE CARD
    // ═══════════════════════════════════════════════════════════════════════

    private void setupContinueCard(PathWithProgress pathWithProgress) {
        if (pathWithProgress == null) {
            cardContinue.setVisibility(View.GONE);
            return;
        }

        LearningPath path = pathWithProgress.getPath();
        if (path == null) {
            cardContinue.setVisibility(View.GONE);
            return;
        }

        cardContinue.setVisibility(View.VISIBLE);
        continuePathId = path.getId();

        textContinueEmoji.setText(path.getIconEmoji() != null ? path.getIconEmoji() : "▶️");
        textContinuePathName.setText(path.getName());
        
        int progress = pathWithProgress.getProgressPercentage();
        progressContinue.setProgress(progress);
        textContinueProgress.setText(progress + "%");

        buttonContinue.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
            animateButton(v);
            navigateToPathDetail(continuePathId);
        });

        cardContinue.setOnClickListener(v -> {
            soundManager.playButtonClick();
            navigateToPathDetail(continuePathId);
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PRO BANNER
    // ═══════════════════════════════════════════════════════════════════════

    private void setupProBanner() {
        cardProBanner.setVisibility(View.GONE);
        
        buttonGoPro.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.POWER_UP);
            animateButton(v);
            navigateToSubscription();
        });

        cardProBanner.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.POWER_UP);
            navigateToSubscription();
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EMPTY STATE
    // ═══════════════════════════════════════════════════════════════════════

    private void setupEmptyState() {
        buttonClearFilters.setOnClickListener(v -> {
            soundManager.playButtonClick();
            viewModel.clearFilters();
            editSearch.setText("");
            selectTab(LearningPathsViewModelV2.FilterType.ALL);
            
            // Reset category chips
            if (chipGroupCategories.getChildCount() > 0) {
                Chip firstChip = (Chip) chipGroupCategories.getChildAt(0);
                firstChip.setChecked(true);
            }
        });
    }

    private void showEmptyState(boolean show, String type) {
        if (show) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerPaths.setVisibility(View.GONE);
            
            if ("search".equals(type)) {
                textEmptyEmoji.setText("🔍");
                textEmptyTitle.setText("No results found");
                textEmptyMessage.setText("Try a different search term");
                buttonClearFilters.setVisibility(View.VISIBLE);
            } else if ("filter".equals(type)) {
                textEmptyEmoji.setText("📭");
                textEmptyTitle.setText("No paths in this category");
                textEmptyMessage.setText("Try selecting a different filter");
                buttonClearFilters.setVisibility(View.VISIBLE);
            } else {
                textEmptyEmoji.setText("📚");
                textEmptyTitle.setText("No learning paths yet");
                textEmptyMessage.setText("Content is loading...");
                buttonClearFilters.setVisibility(View.GONE);
            }
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerPaths.setVisibility(View.VISIBLE);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OBSERVERS
    // ═══════════════════════════════════════════════════════════════════════

    private void observeData() {
        // Observe filtered paths
        viewModel.getFilteredPaths().observe(getViewLifecycleOwner(), paths -> {
            if (paths == null || paths.isEmpty()) {
                String query = editSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    showEmptyState(true, "search");
                } else {
                    showEmptyState(true, "filter");
                }
            } else {
                showEmptyState(false, null);
                adapter.setPaths(paths);
            }
        });

        // Observe continue path
        viewModel.getContinuePath().observe(getViewLifecycleOwner(), this::setupContinueCard);

        // Observe available categories
        viewModel.getAvailableCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                setupCategoryChips(categories);
            }
        });

        // Observe user progress for header
        viewModel.getUserProgress().observe(getViewLifecycleOwner(), this::updateHeaderStats);

        // Observe pro status
        billingManager.getIsProUser().observe(getViewLifecycleOwner(), isPro -> {
            isProUser = isPro;
            viewModel.setProStatus(isPro);
            adapter.setProStatus(isPro);
            
            // Update Pro banner visibility
            LearningPathsViewModelV2.FilterType currentFilter = viewModel.getCurrentFilter().getValue();
            if (currentFilter == LearningPathsViewModelV2.FilterType.PRO && !isPro) {
                cardProBanner.setVisibility(View.VISIBLE);
            } else {
                cardProBanner.setVisibility(View.GONE);
            }
        });

        // Load path progress
        viewModel.getAllPaths().observe(getViewLifecycleOwner(), paths -> {
            if (paths != null) {
                for (LearningPath path : paths) {
                    loadPathProgress(path.getId());
                }
            }
        });
    }

    private void loadPathProgress(int pathId) {
        viewModel.getBugCountInPath(pathId).observe(getViewLifecycleOwner(), total -> {
            if (total != null && total > 0) {
                viewModel.getCompletedBugCountInPath(pathId).observe(getViewLifecycleOwner(), completed -> {
                    if (completed != null) {
                        viewModel.updatePathProgress(pathId, total, completed);
                    }
                });
            }
        });
    }

    private void updateHeaderStats(UserProgress progress) {
        if (progress != null) {
            int level = progress.getLevel();
            int xp = progress.getTotalXp();
            
            textLevel.setText("Lv. " + level);
            textXp.setText("⭐ " + formatNumber(xp));
        }
        
        // Update streak from daily manager
        int streak = dailyBugHuntManager.getCurrentStreak();
        textStreak.setText("🔥 " + streak);
    }

    private String formatNumber(int num) {
        if (num >= 1000) {
            return String.format("%.1fK", num / 1000.0);
        }
        return String.valueOf(num);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════════════

    private void navigateToPathDetail(int pathId) {
        try {
            Bundle args = new Bundle();
            args.putInt("pathId", pathId);
            Navigation.findNavController(requireView()).navigate(R.id.action_paths_to_pathDetail, args);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Navigation error", e);
            Toast.makeText(getContext(), "Could not open path", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToSubscription() {
        try {
            Navigation.findNavController(requireView()).navigate(R.id.action_paths_to_subscription);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Navigation to subscription failed", e);
            Toast.makeText(getContext(), "Opening Pro subscription...", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProUpgradeDialog(LearningPath path) {
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        String pathName = path.getName() != null ? path.getName() : "this course";
        int xp = path.getXpReward() > 0 ? path.getXpReward() : 100;
        int lessons = path.getTotalLessons() > 0 ? path.getTotalLessons() : 10;
        
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("👑 Unlock " + pathName)
            .setMessage(
                "This premium course includes:\n\n" +
                "📚 " + lessons + " interactive lessons\n" +
                "⭐ " + xp + " XP reward\n" +
                "🏆 Completion certificate\n" +
                "🔓 Lifetime access\n\n" +
                "Upgrade to Pro to unlock ALL premium courses!")
            .setPositiveButton("🚀 Go Pro", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                navigateToSubscription();
            })
            .setNegativeButton("Maybe Later", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
            })
            .show();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ANIMATIONS
    // ═══════════════════════════════════════════════════════════════════════

    private void playEntranceAnimations() {
        // Header stats fade in
        if (textLevel != null) {
            textLevel.setAlpha(0f);
            textLevel.animate().alpha(1f).setDuration(400).setStartDelay(100).start();
        }
        if (textXp != null) {
            textXp.setAlpha(0f);
            textXp.animate().alpha(1f).setDuration(400).setStartDelay(150).start();
        }
        if (textStreak != null) {
            textStreak.setAlpha(0f);
            textStreak.animate().alpha(1f).setDuration(400).setStartDelay(200).start();
        }

        // Daily hunt card
        if (cardDailyHunt != null) {
            cardDailyHunt.setAlpha(0f);
            cardDailyHunt.setScaleX(0.95f);
            cardDailyHunt.setScaleY(0.95f);
            cardDailyHunt.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
        }

        // Recycler view
        if (recyclerPaths != null) {
            recyclerPaths.setAlpha(0f);
            recyclerPaths.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(400)
                .start();
        }
    }

    private void animateButton(View button) {
        button.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction(() -> button.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(100)
                .start())
            .start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITIES
    // ═══════════════════════════════════════════════════════════════════════

    private void hideKeyboard() {
        if (editSearch != null) {
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager) requireContext()
                    .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public void onResume() {
        super.onResume();
        if (billingManager != null) {
            billingManager.refreshPurchases();
        }
        if (soundManager != null) {
            soundManager.resumeAll();
        }
        // Update daily hunt UI in case day changed
        updateDailyHuntUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (soundManager != null) {
            soundManager.pauseAll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (recyclerPaths != null) {
            recyclerPaths.setAdapter(null);
        }
        if (billingManager != null) {
            billingManager.clearCallback();
        }
        adapter = null;
        rootView = null;
    }
}
