package com.example.debugappproject.ui.leaderboard;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.debugmaster.app.R;
import com.debugmaster.app.databinding.FragmentLeaderboardBinding;
import com.example.debugappproject.util.SoundManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * LeaderboardFragment - World-Class Competitive Rankings
 * 
 * Features:
 * - Weekly/Monthly/All-Time filters
 * - Top 3 podium with animations
 * - User's current rank with league info
 * - Real-time updates simulation
 */
public class LeaderboardFragment extends Fragment {

    private static final String TAG = "LeaderboardFragment";
    private static final String PREFS_NAME = "leaderboard_prefs";
    
    private FragmentLeaderboardBinding binding;
    private LeaderboardAdapter adapter;
    private SoundManager soundManager;
    private SharedPreferences prefs;
    private String currentFilter = "weekly";
    private Handler refreshHandler = new Handler(Looper.getMainLooper());
    
    // User data
    private int userRank = 47;
    private int userXp = 1250;
    private int userTrophies = 385;
    private String userLeague = "Silver";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        soundManager = SoundManager.getInstance(requireContext());
        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        
        loadUserData();
        setupUI();
        setupRecyclerView();
        loadLeaderboardData();
        animatePodium();
        startLiveUpdates();
    }
    
    private void loadUserData() {
        userRank = prefs.getInt("user_rank", 47);
        userXp = prefs.getInt("user_xp", 1250);
        userTrophies = prefs.getInt("user_trophies", 385);
        userLeague = getLeagueForTrophies(userTrophies);
    }
    
    private String getLeagueForTrophies(int trophies) {
        if (trophies >= 5000) return "Diamond";
        if (trophies >= 2500) return "Platinum";
        if (trophies >= 1000) return "Gold";
        if (trophies >= 300) return "Silver";
        return "Bronze";
    }
    
    private String getLeagueEmoji(String league) {
        switch (league) {
            case "Diamond": return "💎";
            case "Platinum": return "💿";
            case "Gold": return "🥇";
            case "Silver": return "🥈";
            default: return "🥉";
        }
    }

    private void setupUI() {
        // Setup tab selection
        if (binding.btnWeekly != null) {
            binding.btnWeekly.setOnClickListener(v -> {
                soundManager.playButtonClick();
                selectTimeFilter("weekly");
            });
        }
        if (binding.btnMonthly != null) {
            binding.btnMonthly.setOnClickListener(v -> {
                soundManager.playButtonClick();
                selectTimeFilter("monthly");
            });
        }
        if (binding.btnAllTime != null) {
            binding.btnAllTime.setOnClickListener(v -> {
                soundManager.playButtonClick();
                selectTimeFilter("alltime");
            });
        }
        
        // Initial selection
        selectTimeFilter("weekly");
        
        // Update user stats card
        updateUserStatsCard();
    }
    
    private void updateUserStatsCard() {
        if (binding.textUserRank != null) {
            binding.textUserRank.setText("#" + userRank);
        }
        if (binding.textUserXp != null) {
            binding.textUserXp.setText(NumberFormat.getNumberInstance(Locale.US).format(userXp) + " XP");
        }
        if (binding.textUserTrophies != null) {
            binding.textUserTrophies.setText(userTrophies + " trophies");
        }
        if (binding.textUserLeague != null) {
            binding.textUserLeague.setText(getLeagueEmoji(userLeague) + " " + userLeague + " League");
        }
    }

    private void setupRecyclerView() {
        if (binding.recyclerLeaderboard != null) {
            // IMPORTANT: Set LayoutManager BEFORE Adapter to avoid "No adapter attached" warning
            binding.recyclerLeaderboard.setLayoutManager(new LinearLayoutManager(requireContext()));
            
            adapter = new LeaderboardAdapter();
            adapter.setOnItemClickListener((entry, position) -> {
                soundManager.playButtonClick();
                showPlayerProfile(entry);
            });
            binding.recyclerLeaderboard.setAdapter(adapter);
        }
    }

    private void selectTimeFilter(String filter) {
        currentFilter = filter;
        
        int selectedBg = R.drawable.bg_tab_selected;
        int selectedTextColor = Color.WHITE;
        int unselectedTextColor = Color.parseColor("#B0B0C0");
        
        // Weekly button
        if (binding.btnWeekly != null) {
            boolean isSelected = "weekly".equals(filter);
            binding.btnWeekly.setBackgroundResource(isSelected ? selectedBg : android.R.color.transparent);
            binding.btnWeekly.setTextColor(isSelected ? selectedTextColor : unselectedTextColor);
        }
        
        // Monthly button
        if (binding.btnMonthly != null) {
            boolean isSelected = "monthly".equals(filter);
            binding.btnMonthly.setBackgroundResource(isSelected ? selectedBg : android.R.color.transparent);
            binding.btnMonthly.setTextColor(isSelected ? selectedTextColor : unselectedTextColor);
        }
        
        // All Time button
        if (binding.btnAllTime != null) {
            boolean isSelected = "alltime".equals(filter);
            binding.btnAllTime.setBackgroundResource(isSelected ? selectedBg : android.R.color.transparent);
            binding.btnAllTime.setTextColor(isSelected ? selectedTextColor : unselectedTextColor);
        }
        
        loadLeaderboardData();
    }
    
    private void animatePodium() {
        // Animate podium entries with stagger
        View[] podiumViews = {binding.layoutFirst, binding.layoutSecond, binding.layoutThird};
        
        for (int i = 0; i < podiumViews.length; i++) {
            if (podiumViews[i] != null) {
                View view = podiumViews[i];
                view.setScaleX(0f);
                view.setScaleY(0f);
                view.setAlpha(0f);
                
                int delay = i * 150;
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view,
                            PropertyValuesHolder.ofFloat("scaleX", 0f, 1f),
                            PropertyValuesHolder.ofFloat("scaleY", 0f, 1f),
                            PropertyValuesHolder.ofFloat("alpha", 0f, 1f));
                    animator.setDuration(500);
                    animator.setInterpolator(new OvershootInterpolator(1.5f));
                    animator.start();
                }, delay);
            }
        }
    }

    private void loadLeaderboardData() {
        // Podium data (Top 3)
        updatePodium();
        
        // Rest of the leaderboard
        List<LeaderboardEntry> entries = generateLeaderboardEntries();
        
        if (adapter != null) {
            adapter.setEntries(entries);
        }
    }
    
    private void updatePodium() {
        // Different data based on filter
        String[][] podiumData;
        
        switch (currentFilter) {
            case "monthly":
                podiumData = new String[][] {
                    {"CodeNinja", "32,450", "35", "💎"},
                    {"AlgoKing", "31,200", "33", "🏆"},
                    {"DebugLord", "29,800", "32", "⚡"}
                };
                break;
            case "alltime":
                podiumData = new String[][] {
                    {"LegendCoder", "458,200", "50", "👑"},
                    {"BugSlayer99", "425,100", "48", "💎"},
                    {"MasterDebug", "398,500", "47", "🔥"}
                };
                break;
            default: // weekly
                podiumData = new String[][] {
                    {"SpeedCoder", "8,750", "28", "🔥"},
                    {"BugHunterX", "8,420", "26", "⚡"},
                    {"FixerPro", "7,980", "25", "🎯"}
                };
                break;
        }
        
        // First place
        if (binding.textFirstName != null) binding.textFirstName.setText(podiumData[0][0]);
        if (binding.textFirstXp != null) binding.textFirstXp.setText(podiumData[0][1] + " XP");
        if (binding.textFirstLevel != null) binding.textFirstLevel.setText("Lv." + podiumData[0][2]);
        if (binding.textFirstBadge != null) binding.textFirstBadge.setText(podiumData[0][3]);
        
        // Second place
        if (binding.textSecondName != null) binding.textSecondName.setText(podiumData[1][0]);
        if (binding.textSecondXp != null) binding.textSecondXp.setText(podiumData[1][1] + " XP");
        if (binding.textSecondLevel != null) binding.textSecondLevel.setText("Lv." + podiumData[1][2]);
        if (binding.textSecondBadge != null) binding.textSecondBadge.setText(podiumData[1][3]);
        
        // Third place  
        if (binding.textThirdName != null) binding.textThirdName.setText(podiumData[2][0]);
        if (binding.textThirdXp != null) binding.textThirdXp.setText(podiumData[2][1] + " XP");
        if (binding.textThirdLevel != null) binding.textThirdLevel.setText("Lv." + podiumData[2][2]);
        if (binding.textThirdBadge != null) binding.textThirdBadge.setText(podiumData[2][3]);
    }
    
    private List<LeaderboardEntry> generateLeaderboardEntries() {
        List<LeaderboardEntry> entries = new ArrayList<>();
        
        // Realistic usernames from around the world
        String[][] players = {
            {"ByteHunter", "USA", "💎"},
            {"CodeWizard", "UK", "🎯"},
            {"DebugPro", "Germany", "💎"},
            {"JavaMaster", "Japan", "🔥"},
            {"PythonKing", "India", "⚡"},
            {"NullPointer", "Brazil", "⭐"},
            {"CrashFixer", "Canada", "💥"},
            {"LogicLord", "France", "🧠"},
            {"ErrorSeeker", "Australia", "🔍"},
            {"BugCatcher", "Korea", "🐛"},
            {"StackOverflow", "USA", "📚"},
            {"RecursionMan", "China", "🔄"},
            {"MemoryLeak", "Russia", "💾"},
            {"AsyncAwait", "Israel", "⏱️"},
            {"TypeScript", "Norway", "📝"}
        };
        
        Random random = new Random(42);
        int baseXp;
        int xpDecrement;
        
        switch (currentFilter) {
            case "monthly":
                baseXp = 28000;
                xpDecrement = 800;
                break;
            case "alltime":
                baseXp = 380000;
                xpDecrement = 12000;
                break;
            default: // weekly
                baseXp = 7500;
                xpDecrement = 250;
                break;
        }
        
        for (int i = 0; i < players.length; i++) {
            int rank = i + 4; // Start from 4 (after podium)
            int xp = baseXp - (i * xpDecrement) + random.nextInt(200) - 100;
            int level = Math.max(10, 30 - i + random.nextInt(5));
            int bugsFixed = xp / (currentFilter.equals("alltime") ? 500 : 50);
            int trophies = xp / 20;
            
            entries.add(new LeaderboardEntry(
                rank,
                players[i][0],
                xp,
                level,
                players[i][2],
                bugsFixed,
                trophies,
                players[i][1]
            ));
        }
        
        return entries;
    }
    
    private void startLiveUpdates() {
        // Simulate live ranking updates every 30 seconds
        refreshHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (binding != null) {
                    simulateLiveUpdate();
                    refreshHandler.postDelayed(this, 30000);
                }
            }
        }, 30000);
    }
    
    private void simulateLiveUpdate() {
        Random random = new Random();
        
        if (adapter != null) {
            List<LeaderboardEntry> entries = adapter.getEntries();
            if (entries != null && !entries.isEmpty()) {
                int index = random.nextInt(entries.size());
                LeaderboardEntry entry = entries.get(index);
                
                int xpGain = 10 + random.nextInt(50);
                entry.xp += xpGain;
                
                adapter.notifyItemChanged(index);
            }
        }
    }
    
    private void showPlayerProfile(LeaderboardEntry entry) {
        String message = String.format(Locale.US,
            "Rank: #%d\nXP: %s\nLevel: %d\nBugs Fixed: %d\nTrophies: %d\nCountry: %s",
            entry.rank,
            NumberFormat.getNumberInstance(Locale.US).format(entry.xp),
            entry.level,
            entry.bugsFixed,
            entry.trophies,
            entry.country
        );
        
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle(entry.badge + " " + entry.username)
            .setMessage(message)
            .setPositiveButton("Challenge", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
            })
            .setNegativeButton("Close", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks
        refreshHandler.removeCallbacksAndMessages(null);
        if (binding != null && binding.recyclerLeaderboard != null) {
            binding.recyclerLeaderboard.setAdapter(null);
        }
        adapter = null;
        binding = null;
    }

    /**
     * Data class for leaderboard entries
     */
    public static class LeaderboardEntry {
        public int rank;
        public String username;
        public int xp;
        public int level;
        public String badge;
        public int bugsFixed;
        public int trophies;
        public String country;

        public LeaderboardEntry(int rank, String username, int xp, int level, String badge) {
            this.rank = rank;
            this.username = username;
            this.xp = xp;
            this.level = level;
            this.badge = badge;
            this.bugsFixed = xp / 50;
            this.trophies = xp / 20;
            this.country = "World";
        }
        
        public LeaderboardEntry(int rank, String username, int xp, int level, String badge, 
                                int bugsFixed, int trophies, String country) {
            this.rank = rank;
            this.username = username;
            this.xp = xp;
            this.level = level;
            this.badge = badge;
            this.bugsFixed = bugsFixed;
            this.trophies = trophies;
            this.country = country;
        }
    }
}
