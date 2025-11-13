package com.example.debugappproject.ui.learn;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugappproject.R;
import com.example.debugappproject.model.BugChallenge;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying bugs within a learning path.
 */
public class BugInPathAdapter extends RecyclerView.Adapter<BugInPathAdapter.BugViewHolder> {

    private List<BugInPathWithDetails> bugs = new ArrayList<>();
    private OnBugClickListener listener;

    /**
     * Interface for handling bug item clicks.
     */
    public interface OnBugClickListener {
        void onBugClick(BugChallenge bug);
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
     * Updates the adapter with new bug data.
     */
    public void setBugs(List<BugInPathWithDetails> bugs) {
        this.bugs = bugs;
        notifyDataSetChanged();
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
            BugChallenge bug = bugWithDetails.getBug();

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

            // Handle click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBugClick(bug);
                }
            });
        }
    }
}
