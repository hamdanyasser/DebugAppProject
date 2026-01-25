package com.example.debugappproject.corporate;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.debugmaster.app.R;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - TEAM ANALYTICS DASHBOARD                             ║
 * ║              Track Team Progress and Performance                             ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TeamAnalyticsFragment extends Fragment {

    private CorporateTierManager corporateManager;
    private SoundManager soundManager;
    
    // Views
    private TextView textTeamName;
    private TextView textTotalBugs;
    private TextView textTotalXP;
    private TextView textActiveMembers;
    private TextView textAvgScore;
    private RecyclerView recyclerLeaderboard;
    private LinearLayout containerSkills;
    private MaterialButton btnExportReport;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_team_analytics, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        corporateManager = CorporateTierManager.getInstance(requireContext());
        soundManager = SoundManager.getInstance(requireContext());
        
        findViews(view);
        loadAnalytics();
    }
    
    private void findViews(View view) {
        textTeamName = view.findViewById(R.id.text_team_name);
        textTotalBugs = view.findViewById(R.id.text_total_bugs);
        textTotalXP = view.findViewById(R.id.text_total_xp);
        textActiveMembers = view.findViewById(R.id.text_active_members);
        textAvgScore = view.findViewById(R.id.text_avg_score);
        recyclerLeaderboard = view.findViewById(R.id.recycler_leaderboard);
        containerSkills = view.findViewById(R.id.container_skills);
        btnExportReport = view.findViewById(R.id.btn_export_report);
        
        View backBtn = view.findViewById(R.id.button_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());
        }
        
        if (btnExportReport != null) {
            btnExportReport.setOnClickListener(v -> exportReport());
        }
    }
    
    private void loadAnalytics() {
        CorporateTierManager.TeamData team = corporateManager.getTeamData();
        CorporateTierManager.TeamAnalytics analytics = corporateManager.getAnalytics();
        
        if (team != null && textTeamName != null) {
            textTeamName.setText(team.teamName + " Analytics");
        }
        
        // Summary stats
        if (textTotalBugs != null) textTotalBugs.setText(String.valueOf(analytics.totalBugsFixed));
        if (textTotalXP != null) textTotalXP.setText(String.valueOf(analytics.totalXPEarned));
        if (textActiveMembers != null) textActiveMembers.setText(String.valueOf(analytics.activeMembers));
        if (textAvgScore != null) textAvgScore.setText(String.valueOf(analytics.averageScore));
        
        // Leaderboard
        if (recyclerLeaderboard != null) {
            recyclerLeaderboard.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerLeaderboard.setAdapter(new LeaderboardAdapter(analytics.topPerformers));
        }
        
        // Skills breakdown
        displaySkillsBreakdown(analytics);
    }
    
    private void displaySkillsBreakdown(CorporateTierManager.TeamAnalytics analytics) {
        if (containerSkills == null) return;
        containerSkills.removeAllViews();
        
        int maxValue = 1;
        for (Integer value : analytics.bugsByCategory.values()) {
            if (value > maxValue) maxValue = value;
        }
        
        for (String skill : analytics.bugsByCategory.keySet()) {
            int value = analytics.bugsByCategory.get(skill);
            addSkillBar(skill, value, maxValue);
        }
    }
    
    private void addSkillBar(String skill, int value, int maxValue) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);
        
        TextView label = new TextView(requireContext());
        label.setText(skill);
        label.setTextColor(Color.WHITE);
        label.setMinWidth(dpToPx(100));
        
        ProgressBar bar = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(maxValue);
        bar.setProgress(value);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(0, dpToPx(20), 1);
        barParams.setMarginStart(dpToPx(8));
        bar.setLayoutParams(barParams);
        
        TextView valueText = new TextView(requireContext());
        valueText.setText(String.valueOf(value));
        valueText.setTextColor(Color.parseColor("#22C55E"));
        valueText.setMinWidth(dpToPx(40));
        valueText.setPadding(dpToPx(8), 0, 0, 0);
        
        row.addView(label);
        row.addView(bar);
        row.addView(valueText);
        containerSkills.addView(row);
    }
    
    private void exportReport() {
        soundManager.playButtonClick();
        String report = corporateManager.generateProgressReport();
        // In production, this would create a PDF or send email
        android.widget.Toast.makeText(getContext(), "Report generated!", android.widget.Toast.LENGTH_SHORT).show();
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    
    // Inner adapter class
    private class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
        private final List<CorporateTierManager.TeamAnalytics.LeaderboardEntry> entries;
        
        LeaderboardAdapter(List<CorporateTierManager.TeamAnalytics.LeaderboardEntry> entries) {
            this.entries = entries;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_leaderboard_entry, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CorporateTierManager.TeamAnalytics.LeaderboardEntry entry = entries.get(position);
            holder.bind(entry, position + 1);
        }
        
        @Override
        public int getItemCount() {
            return entries.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textRank, textName, textScore, textBugs;
            
            ViewHolder(View itemView) {
                super(itemView);
                textRank = itemView.findViewById(R.id.text_rank);
                textName = itemView.findViewById(R.id.text_name);
                textScore = itemView.findViewById(R.id.text_score);
                textBugs = itemView.findViewById(R.id.text_bugs);
            }
            
            void bind(CorporateTierManager.TeamAnalytics.LeaderboardEntry entry, int rank) {
                String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "#" + rank;
                if (textRank != null) textRank.setText(medal);
                if (textName != null) textName.setText(entry.name);
                if (textScore != null) textScore.setText(entry.score + " XP");
                if (textBugs != null) textBugs.setText(entry.bugsFixed + " bugs");
            }
        }
    }
}
