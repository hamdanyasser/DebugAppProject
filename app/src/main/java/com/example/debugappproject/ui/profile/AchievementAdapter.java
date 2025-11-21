package com.example.debugappproject.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugappproject.R;
import com.example.debugappproject.model.AchievementDefinition;
import com.example.debugappproject.util.AnimationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying achievements in a grid.
 * Shows both locked and unlocked achievements.
 * Uses DiffUtil for animated unlock transitions.
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
     * Uses DiffUtil for smooth animations when achievements are unlocked.
     */
    public void setAchievements(List<AchievementWithStatus> newAchievements) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
            new AchievementDiffCallback(this.achievements, newAchievements)
        );
        this.achievements = new ArrayList<>(newAchievements);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * DiffUtil callback for calculating differences between achievement lists.
     * Enables smooth animations when achievements are unlocked.
     */
    private static class AchievementDiffCallback extends DiffUtil.Callback {
        private final List<AchievementWithStatus> oldList;
        private final List<AchievementWithStatus> newList;

        public AchievementDiffCallback(List<AchievementWithStatus> oldList,
                                        List<AchievementWithStatus> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // Same achievement if IDs match
            return oldList.get(oldItemPosition).getDefinition().getId() ==
                   newList.get(newItemPosition).getDefinition().getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            AchievementWithStatus oldItem = oldList.get(oldItemPosition);
            AchievementWithStatus newItem = newList.get(newItemPosition);

            // Compare unlock status and achievement properties
            return oldItem.isUnlocked() == newItem.isUnlocked() &&
                   oldItem.getDefinition().getName().equals(newItem.getDefinition().getName()) &&
                   oldItem.getDefinition().getDescription().equals(newItem.getDefinition().getDescription()) &&
                   oldItem.getDefinition().getXpReward() == newItem.getDefinition().getXpReward();
        }
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
