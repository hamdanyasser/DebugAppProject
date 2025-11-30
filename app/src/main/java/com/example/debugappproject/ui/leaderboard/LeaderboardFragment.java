package com.example.debugappproject.ui.leaderboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.debugappproject.R;
import com.example.debugappproject.databinding.FragmentLeaderboardBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * LeaderboardFragment - Shows global rankings and competition.
 * Mimo-style competitive leaderboard with podium display.
 */
public class LeaderboardFragment extends Fragment {

    private static final String TAG = "LeaderboardFragment";
    private FragmentLeaderboardBinding binding;
    private LeaderboardAdapter adapter;
    private String currentFilter = "weekly";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupUI();
        setupRecyclerView();
        loadLeaderboardData();
    }

    private void setupUI() {
        // Setup tab selection (Weekly/Monthly/All Time) using TextViews
        if (binding.btnWeekly != null) {
            binding.btnWeekly.setOnClickListener(v -> selectTimeFilter("weekly"));
        }
        if (binding.btnMonthly != null) {
            binding.btnMonthly.setOnClickListener(v -> selectTimeFilter("monthly"));
        }
        if (binding.btnAllTime != null) {
            binding.btnAllTime.setOnClickListener(v -> selectTimeFilter("alltime"));
        }
        
        // Initial selection
        selectTimeFilter("weekly");
    }

    private void setupRecyclerView() {
        if (binding.recyclerLeaderboard != null) {
            adapter = new LeaderboardAdapter();
            binding.recyclerLeaderboard.setAdapter(adapter);
            binding.recyclerLeaderboard.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    private void selectTimeFilter(String filter) {
        currentFilter = filter;
        
        // Get colors
        int selectedTextColor = ContextCompat.getColor(requireContext(), R.color.primary);
        int unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.white);
        
        // Weekly button
        if (binding.btnWeekly != null) {
            boolean isSelected = "weekly".equals(filter);
            binding.btnWeekly.setBackgroundResource(isSelected ? R.drawable.bg_tab_selected : android.R.color.transparent);
            binding.btnWeekly.setTextColor(isSelected ? selectedTextColor : unselectedTextColor);
        }
        
        // Monthly button
        if (binding.btnMonthly != null) {
            boolean isSelected = "monthly".equals(filter);
            binding.btnMonthly.setBackgroundResource(isSelected ? R.drawable.bg_tab_selected : android.R.color.transparent);
            binding.btnMonthly.setTextColor(isSelected ? selectedTextColor : unselectedTextColor);
        }
        
        // All Time button
        if (binding.btnAllTime != null) {
            boolean isSelected = "alltime".equals(filter);
            binding.btnAllTime.setBackgroundResource(isSelected ? R.drawable.bg_tab_selected : android.R.color.transparent);
            binding.btnAllTime.setTextColor(isSelected ? selectedTextColor : unselectedTextColor);
        }
        
        // Reload data with new filter
        loadLeaderboardData();
    }

    private void loadLeaderboardData() {
        // Sample data - in production, this would come from a server
        List<LeaderboardEntry> entries = new ArrayList<>();
        
        switch (currentFilter) {
            case "weekly":
                entries.add(new LeaderboardEntry(4, "JavaMaster", 4250, 15, "🔥", 85));
                entries.add(new LeaderboardEntry(5, "ByteHunter", 4100, 22, "⚡", 82));
                entries.add(new LeaderboardEntry(6, "CodeWizard", 3890, 18, "🎯", 75));
                entries.add(new LeaderboardEntry(7, "BugCatcher", 3750, 12, "🐛", 68));
                entries.add(new LeaderboardEntry(8, "DebugPro", 3620, 20, "💎", 62));
                entries.add(new LeaderboardEntry(9, "FixerUpper", 3500, 14, "🔧", 55));
                entries.add(new LeaderboardEntry(10, "ErrorSeeker", 3380, 16, "🔍", 48));
                entries.add(new LeaderboardEntry(11, "CrashFixer", 3200, 11, "💥", 42));
                entries.add(new LeaderboardEntry(12, "NullPointer", 3050, 19, "⭐", 38));
                entries.add(new LeaderboardEntry(13, "LogicLord", 2900, 13, "🧠", 35));
                break;
            case "monthly":
                entries.add(new LeaderboardEntry(4, "ByteHunter", 15200, 22, "⚡", 304));
                entries.add(new LeaderboardEntry(5, "JavaMaster", 14800, 15, "🔥", 296));
                entries.add(new LeaderboardEntry(6, "DebugPro", 13500, 20, "💎", 258));
                entries.add(new LeaderboardEntry(7, "CodeWizard", 12900, 18, "🎯", 245));
                entries.add(new LeaderboardEntry(8, "CrashFixer", 11800, 11, "💥", 220));
                entries.add(new LeaderboardEntry(9, "NullPointer", 11200, 19, "⭐", 198));
                entries.add(new LeaderboardEntry(10, "BugCatcher", 10750, 12, "🐛", 185));
                entries.add(new LeaderboardEntry(11, "FixerUpper", 10500, 14, "🔧", 175));
                entries.add(new LeaderboardEntry(12, "LogicLord", 9900, 13, "🧠", 162));
                entries.add(new LeaderboardEntry(13, "ErrorSeeker", 9380, 16, "🔍", 148));
                break;
            case "alltime":
                entries.add(new LeaderboardEntry(4, "DebugPro", 125000, 35, "💎", 2580));
                entries.add(new LeaderboardEntry(5, "ByteHunter", 118000, 32, "⚡", 2450));
                entries.add(new LeaderboardEntry(6, "JavaMaster", 112000, 30, "🔥", 2320));
                entries.add(new LeaderboardEntry(7, "NullPointer", 98000, 28, "⭐", 1980));
                entries.add(new LeaderboardEntry(8, "CrashFixer", 89000, 26, "💥", 1850));
                entries.add(new LeaderboardEntry(9, "CodeWizard", 85000, 25, "🎯", 1720));
                entries.add(new LeaderboardEntry(10, "LogicLord", 78000, 24, "🧠", 1580));
                entries.add(new LeaderboardEntry(11, "BugCatcher", 72000, 22, "🐛", 1450));
                entries.add(new LeaderboardEntry(12, "FixerUpper", 68000, 21, "🔧", 1380));
                entries.add(new LeaderboardEntry(13, "ErrorSeeker", 62000, 20, "🔍", 1250));
                break;
        }
        
        if (adapter != null) {
            adapter.setEntries(entries);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Data class for leaderboard entries
    public static class LeaderboardEntry {
        public int rank;
        public String username;
        public int xp;
        public int level;
        public String badge;
        public int bugsFixed;

        public LeaderboardEntry(int rank, String username, int xp, int level, String badge) {
            this.rank = rank;
            this.username = username;
            this.xp = xp;
            this.level = level;
            this.badge = badge;
            this.bugsFixed = xp / 50; // Estimate bugs based on XP
        }
        
        public LeaderboardEntry(int rank, String username, int xp, int level, String badge, int bugsFixed) {
            this.rank = rank;
            this.username = username;
            this.xp = xp;
            this.level = level;
            this.badge = badge;
            this.bugsFixed = bugsFixed;
        }
    }
}
