package com.example.debugappproject.ui.buglist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    public void submitList(List<Bug> newBugs) {
        this.bugs = newBugs != null ? newBugs : new ArrayList<>();
        notifyDataSetChanged();
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
