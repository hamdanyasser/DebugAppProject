package com.example.debugappproject.ui.leaderboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        // Setup tab selection (Weekly/Monthly/All Time)
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
        
        // Update button states visually
        int selectedColor = getResources().getColor(R.color.white, null);
        int unselectedColor = android.graphics.Color.TRANSPARENT;
        
        if (binding.btnWeekly != null) {
            binding.btnWeekly.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                "weekly".equals(filter) ? selectedColor : unselectedColor));
            binding.btnWeekly.setTextColor("weekly".equals(filter) ? 
                getResources().getColor(R.color.primary, null) : 
                getResources().getColor(R.color.white, null));
        }
        if (binding.btnMonthly != null) {
            binding.btnMonthly.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                "monthly".equals(filter) ? selectedColor : unselectedColor));
            binding.btnMonthly.setTextColor("monthly".equals(filter) ? 
                getResources().getColor(R.color.primary, null) : 
                getResources().getColor(R.color.white, null));
        }
        if (binding.btnAllTime != null) {
            binding.btnAllTime.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                "alltime".equals(filter) ? selectedColor : unselectedColor));
            binding.btnAllTime.setTextColor("alltime".equals(filter) ? 
                getResources().getColor(R.color.primary, null) : 
                getResources().getColor(R.color.white, null));
        }
        
        // Reload data with new filter
        loadLeaderboardData();
    }

    private void loadLeaderboardData() {
        // Sample data - in production, this would come from a server
        List<LeaderboardEntry> entries = new ArrayList<>();
        
        switch (currentFilter) {
            case "weekly":
                entries.add(new LeaderboardEntry(4, "JavaMaster", 4250, 15, "🔥"));
                entries.add(new LeaderboardEntry(5, "ByteHunter", 4100, 22, "⚡"));
                entries.add(new LeaderboardEntry(6, "CodeWizard", 3890, 18, "🎯"));
                entries.add(new LeaderboardEntry(7, "BugCatcher", 3750, 12, "🐛"));
                entries.add(new LeaderboardEntry(8, "DebugPro", 3620, 20, "💎"));
                entries.add(new LeaderboardEntry(9, "FixerUpper", 3500, 14, "🔧"));
                entries.add(new LeaderboardEntry(10, "ErrorSeeker", 3380, 16, "🔍"));
                entries.add(new LeaderboardEntry(11, "CrashFixer", 3200, 11, "💥"));
                entries.add(new LeaderboardEntry(12, "NullPointer", 3050, 19, "⭐"));
                entries.add(new LeaderboardEntry(13, "LogicLord", 2900, 13, "🧠"));
                break;
            case "monthly":
                entries.add(new LeaderboardEntry(4, "ByteHunter", 15200, 22, "⚡"));
                entries.add(new LeaderboardEntry(5, "JavaMaster", 14800, 15, "🔥"));
                entries.add(new LeaderboardEntry(6, "DebugPro", 13500, 20, "💎"));
                entries.add(new LeaderboardEntry(7, "CodeWizard", 12900, 18, "🎯"));
                entries.add(new LeaderboardEntry(8, "CrashFixer", 11800, 11, "💥"));
                entries.add(new LeaderboardEntry(9, "NullPointer", 11200, 19, "⭐"));
                entries.add(new LeaderboardEntry(10, "BugCatcher", 10750, 12, "🐛"));
                entries.add(new LeaderboardEntry(11, "FixerUpper", 10500, 14, "🔧"));
                entries.add(new LeaderboardEntry(12, "LogicLord", 9900, 13, "🧠"));
                entries.add(new LeaderboardEntry(13, "ErrorSeeker", 9380, 16, "🔍"));
                break;
            case "alltime":
                entries.add(new LeaderboardEntry(4, "DebugPro", 125000, 35, "💎"));
                entries.add(new LeaderboardEntry(5, "ByteHunter", 118000, 32, "⚡"));
                entries.add(new LeaderboardEntry(6, "JavaMaster", 112000, 30, "🔥"));
                entries.add(new LeaderboardEntry(7, "NullPointer", 98000, 28, "⭐"));
                entries.add(new LeaderboardEntry(8, "CrashFixer", 89000, 26, "💥"));
                entries.add(new LeaderboardEntry(9, "CodeWizard", 85000, 25, "🎯"));
                entries.add(new LeaderboardEntry(10, "LogicLord", 78000, 24, "🧠"));
                entries.add(new LeaderboardEntry(11, "BugCatcher", 72000, 22, "🐛"));
                entries.add(new LeaderboardEntry(12, "FixerUpper", 68000, 21, "🔧"));
                entries.add(new LeaderboardEntry(13, "ErrorSeeker", 62000, 20, "🔍"));
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

        public LeaderboardEntry(int rank, String username, int xp, int level, String badge) {
            this.rank = rank;
            this.username = username;
            this.xp = xp;
            this.level = level;
            this.badge = badge;
        }
    }
}
