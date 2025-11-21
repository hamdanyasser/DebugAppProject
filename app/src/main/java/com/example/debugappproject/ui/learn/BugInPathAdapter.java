package com.example.debugappproject.ui.learn;

import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying bugs within a learning path.
 * Uses DiffUtil for efficient list updates with smooth animations.
 */
public class BugInPathAdapter extends RecyclerView.Adapter<BugInPathAdapter.BugViewHolder> {

    private List<BugInPathWithDetails> bugs = new ArrayList<>();
    private OnBugClickListener listener;

    /**
     * Interface for handling bug item clicks.
     */
    public interface OnBugClickListener {
        void onBugClick(Bug bug);
    }

    public BugInPathAdapter(OnBugClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BugViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_bug_in_path, parent, false);
        return new BugViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BugViewHolder holder, int position) {
        BugInPathWithDetails bugWithDetails = bugs.get(position);
        holder.bind(bugWithDetails, listener);
    }

    @Override
    public int getItemCount() {
        return bugs.size();
    }

    /**
     * Updates the adapter with new bug data using DiffUtil for smooth animations.
     */
    public void setBugs(List<BugInPathWithDetails> newBugs) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new BugDiffCallback(this.bugs, newBugs)
        );
        this.bugs = new ArrayList<>(newBugs);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * DiffUtil callback for calculating list differences efficiently.
     */
    private static class BugDiffCallback extends DiffUtil.Callback {
        private final List<BugInPathWithDetails> oldList;
        private final List<BugInPathWithDetails> newList;

        public BugDiffCallback(List<BugInPathWithDetails> oldList, List<BugInPathWithDetails> newList) {
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
            Bug oldBug = oldList.get(oldItemPosition).getBug();
            Bug newBug = newList.get(newItemPosition).getBug();
            return oldBug.getId() == newBug.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            BugInPathWithDetails oldItem = oldList.get(oldItemPosition);
            BugInPathWithDetails newItem = newList.get(newItemPosition);

            Bug oldBug = oldItem.getBug();
            Bug newBug = newItem.getBug();

            return oldBug.getTitle().equals(newBug.getTitle()) &&
                   oldBug.getDifficulty().equals(newBug.getDifficulty()) &&
                   oldBug.getCategory().equals(newBug.getCategory()) &&
                   oldItem.isCompleted() == newItem.isCompleted();
        }
    }

    /**
     * ViewHolder for bug items.
     */
    static class BugViewHolder extends RecyclerView.ViewHolder {
        private final TextView textCompletion;
        private final TextView textTitle;
        private final TextView textDifficulty;
        private final TextView textCategory;

        public BugViewHolder(@NonNull View itemView) {
            super(itemView);
            textCompletion = itemView.findViewById(R.id.text_completion);
            textTitle = itemView.findViewById(R.id.text_title);
            textDifficulty = itemView.findViewById(R.id.text_difficulty);
            textCategory = itemView.findViewById(R.id.text_category);
        }

        /**
         * Binds bug data to the views.
         */
        public void bind(BugInPathWithDetails bugWithDetails, OnBugClickListener listener) {
            Bug bug = bugWithDetails.getBug();

            // Set completion indicator
            if (bugWithDetails.isCompleted()) {
                textCompletion.setText("✓");
                textCompletion.setTextColor(Color.parseColor("#4CAF50")); // Green
            } else {
                textCompletion.setText("○");
                textCompletion.setTextColor(Color.parseColor("#9E9E9E")); // Gray
            }

            // Set bug details
            textTitle.setText(bug.getTitle());
            textCategory.setText(bug.getCategory());

            // Set difficulty with color
            String difficulty = bug.getDifficulty().toUpperCase();
            textDifficulty.setText(difficulty);

            int difficultyColor;
            switch (bug.getDifficulty().toLowerCase()) {
                case "easy":
                    difficultyColor = Color.parseColor("#4CAF50"); // Green
                    break;
                case "medium":
                    difficultyColor = Color.parseColor("#FF9800"); // Orange
                    break;
                case "hard":
                    difficultyColor = Color.parseColor("#F44336"); // Red
                    break;
                default:
                    difficultyColor = Color.parseColor("#9E9E9E"); // Gray
                    break;
            }
            textDifficulty.setTextColor(difficultyColor);

            // Handle click with animation
            itemView.setOnClickListener(v -> {
                AnimationUtil.animatePress(v, () -> {
                    if (listener != null) {
                        listener.onBugClick(bug);
                    }
                });
            });
        }
    }
}
