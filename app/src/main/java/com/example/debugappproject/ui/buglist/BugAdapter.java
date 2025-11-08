package com.example.debugappproject.ui.buglist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugappproject.R;
import com.example.debugappproject.model.Bug;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying list of bugs.
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
        private final TextView difficultyText;
        private final TextView categoryText;
        private final View completedIndicator;

        public BugViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_bug_title);
            difficultyText = itemView.findViewById(R.id.text_bug_difficulty);
            categoryText = itemView.findViewById(R.id.text_bug_category);
            completedIndicator = itemView.findViewById(R.id.completed_indicator);
        }

        public void bind(Bug bug, OnBugClickListener listener) {
            titleText.setText(bug.getTitle());
            difficultyText.setText(bug.getDifficulty());
            categoryText.setText(bug.getCategory());

            // Show/hide completed indicator
            completedIndicator.setVisibility(bug.isCompleted() ? View.VISIBLE : View.GONE);

            // Set difficulty color
            int color;
            switch (bug.getDifficulty()) {
                case "Easy":
                    color = 0xFF4CAF50; // Green
                    break;
                case "Medium":
                    color = 0xFFFF9800; // Orange
                    break;
                case "Hard":
                    color = 0xFFF44336; // Red
                    break;
                default:
                    color = 0xFF9E9E9E; // Grey
            }
            difficultyText.setTextColor(color);

            itemView.setOnClickListener(v -> listener.onBugClick(bug));
        }
    }
}
