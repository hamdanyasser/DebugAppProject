package com.example.debugappproject.ui.buglist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugappproject.R;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.util.AnimationUtil;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying list of bugs.
 *
 * Features:
 * - Material 3 chip-based design for difficulty and category
 * - Dynamic difficulty chip coloring (green/orange/red)
 * - Checkmark indicator for completed bugs
 * - Card-based layout with ripple effects
 */
public class BugAdapter extends RecyclerView.Adapter<BugAdapter.BugViewHolder> {

    private List<Bug> bugs = new ArrayList<>();
    private OnBugClickListener clickListener;

    public interface OnBugClickListener {
        void onBugClick(Bug bug);
    }

    public BugAdapter(OnBugClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public BugViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bug, parent, false);
        return new BugViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BugViewHolder holder, int position) {
        Bug bug = bugs.get(position);
        holder.bind(bug, clickListener);
    }

    @Override
    public int getItemCount() {
        return bugs.size();
    }

    /**
     * Updates the list using DiffUtil for smooth animations.
     */
    public void submitList(List<Bug> newBugs) {
        List<Bug> newList = newBugs != null ? newBugs : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new BugDiffCallback(this.bugs, newList)
        );
        this.bugs = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * DiffUtil callback for efficient bug list updates.
     */
    private static class BugDiffCallback extends DiffUtil.Callback {
        private final List<Bug> oldList;
        private final List<Bug> newList;

        public BugDiffCallback(List<Bug> oldList, List<Bug> newList) {
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
            // Same bug if IDs match
            return oldList.get(oldItemPosition).getId() ==
                   newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Bug oldBug = oldList.get(oldItemPosition);
            Bug newBug = newList.get(newItemPosition);

            // Compare all displayed properties
            return oldBug.getTitle().equals(newBug.getTitle()) &&
                   oldBug.getDifficulty().equals(newBug.getDifficulty()) &&
                   oldBug.getCategory().equals(newBug.getCategory()) &&
                   oldBug.isCompleted() == newBug.isCompleted();
        }
    }

    static class BugViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final Chip difficultyChip;
        private final Chip categoryChip;
        private final TextView completedIcon;

        public BugViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_bug_title);
            difficultyChip = itemView.findViewById(R.id.chip_difficulty);
            categoryChip = itemView.findViewById(R.id.chip_category);
            completedIcon = itemView.findViewById(R.id.icon_completed);
        }

        /**
         * Binds bug data to the view holder.
         * Sets chip colors based on difficulty level.
         */
        public void bind(Bug bug, OnBugClickListener listener) {
            titleText.setText(bug.getTitle());

            // Set difficulty chip
            difficultyChip.setText(bug.getDifficulty());
            setDifficultyChipColor(bug.getDifficulty());

            // Set category chip
            categoryChip.setText(bug.getCategory());

            // Show/hide completed indicator
            completedIcon.setVisibility(bug.isCompleted() ? View.VISIBLE : View.GONE);

            // Set click listener with animation
            itemView.setOnClickListener(v -> {
                AnimationUtil.animatePress(v, () -> listener.onBugClick(bug));
            });
        }

        /**
         * Sets difficulty chip background color based on difficulty level.
         */
        private void setDifficultyChipColor(String difficulty) {
            int colorRes;
            switch (difficulty.toLowerCase()) {
                case "easy":
                    colorRes = R.color.difficulty_easy;
                    break;
                case "medium":
                    colorRes = R.color.difficulty_medium;
                    break;
                case "hard":
                    colorRes = R.color.difficulty_hard;
                    break;
                default:
                    colorRes = R.color.difficulty_easy;
            }
            difficultyChip.setChipBackgroundColorResource(colorRes);
        }
    }
}
