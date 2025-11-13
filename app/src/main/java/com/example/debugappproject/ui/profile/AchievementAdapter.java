package com.example.debugappproject.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugappproject.R;
import com.example.debugappproject.model.AchievementDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying achievements in a grid.
 * Shows both locked and unlocked achievements.
 */
public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private List<AchievementWithStatus> achievements = new ArrayList<>();

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        AchievementWithStatus achievement = achievements.get(position);
        holder.bind(achievement);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    /**
     * Updates the adapter with new achievement data.
     */
    public void setAchievements(List<AchievementWithStatus> achievements) {
        this.achievements = achievements;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for achievement items.
     */
    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        private final TextView textIcon;
        private final TextView textName;
        private final TextView textDescription;
        private final TextView textXp;
        private final View viewLockedOverlay;
        private final TextView textLock;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            textIcon = itemView.findViewById(R.id.text_icon);
            textName = itemView.findViewById(R.id.text_name);
            textDescription = itemView.findViewById(R.id.text_description);
            textXp = itemView.findViewById(R.id.text_xp);
            viewLockedOverlay = itemView.findViewById(R.id.view_locked_overlay);
            textLock = itemView.findViewById(R.id.text_lock);
        }

        /**
         * Binds achievement data to the views.
         */
        public void bind(AchievementWithStatus achievementWithStatus) {
            AchievementDefinition achievement = achievementWithStatus.getDefinition();

            textIcon.setText(achievement.getIconEmoji());
            textName.setText(achievement.getName());
            textDescription.setText(achievement.getDescription());
            textXp.setText("+" + achievement.getXpReward() + " XP");

            // Show locked state for locked achievements
            if (achievementWithStatus.isUnlocked()) {
                viewLockedOverlay.setVisibility(View.GONE);
                textLock.setVisibility(View.GONE);
                textIcon.setAlpha(1.0f);
                textName.setAlpha(1.0f);
                textDescription.setAlpha(1.0f);
            } else {
                viewLockedOverlay.setVisibility(View.VISIBLE);
                textLock.setVisibility(View.VISIBLE);
                textIcon.setAlpha(0.3f);
                textName.setAlpha(0.5f);
                textDescription.setAlpha(0.5f);
            }
        }
    }
}
