package com.example.debugappproject.ui.leaderboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugappproject.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying leaderboard entries with competitive gaming UI.
 * Features:
 * - Rank position with styling for top ranks
 * - Username with badge
 * - XP score with formatting
 * - Bugs fixed count
 * - Click listener for player profiles
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<LeaderboardFragment.LeaderboardEntry> entries = new ArrayList<>();
    private OnItemClickListener clickListener;
    
    public interface OnItemClickListener {
        void onItemClick(LeaderboardFragment.LeaderboardEntry entry, int position);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setEntries(List<LeaderboardFragment.LeaderboardEntry> entries) {
        this.entries = entries;
        notifyDataSetChanged();
    }
    
    public List<LeaderboardFragment.LeaderboardEntry> getEntries() {
        return entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardFragment.LeaderboardEntry entry = entries.get(position);
        holder.bind(entry);
        
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(entry, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textRankPosition;
        private final TextView textAvatarEmoji;
        private final TextView textUsername;
        private final TextView textUserLevel;
        private final TextView textXpScore;
        private final TextView textBugsCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textRankPosition = itemView.findViewById(R.id.text_rank_position);
            textAvatarEmoji = itemView.findViewById(R.id.text_avatar_emoji);
            textUsername = itemView.findViewById(R.id.text_username);
            textUserLevel = itemView.findViewById(R.id.text_user_level);
            textXpScore = itemView.findViewById(R.id.text_xp_score);
            textBugsCount = itemView.findViewById(R.id.text_bugs_count);
        }

        void bind(LeaderboardFragment.LeaderboardEntry entry) {
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
            
            if (textRankPosition != null) {
                textRankPosition.setText("#" + entry.rank);
                
                // Color coding for ranks
                int color;
                if (entry.rank <= 5) {
                    color = 0xFFFFD700; // Gold
                } else if (entry.rank <= 10) {
                    color = 0xFFC0C0C0; // Silver
                } else if (entry.rank <= 20) {
                    color = 0xFFCD7F32; // Bronze
                } else {
                    color = 0xFF94A3B8; // Default
                }
                textRankPosition.setTextColor(color);
            }
            if (textAvatarEmoji != null) {
                textAvatarEmoji.setText(entry.badge != null ? entry.badge : "👨‍💻");
            }
            if (textUsername != null) {
                textUsername.setText(entry.username);
            }
            if (textUserLevel != null) {
                textUserLevel.setText("Lv." + entry.level);
            }
            if (textXpScore != null) {
                textXpScore.setText(numberFormat.format(entry.xp) + " XP");
            }
            if (textBugsCount != null) {
                textBugsCount.setText(entry.bugsFixed + " bugs fixed");
            }
        }
    }
}
