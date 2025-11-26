package com.example.debugappproject.ui.learn;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugappproject.R;
import com.example.debugappproject.model.LearningPath;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying learning paths as cards.
 * 
 * IMPORTANT: View IDs must match item_learning_path.xml:
 * - text_path_emoji
 * - text_path_name  
 * - text_path_description
 * - text_path_progress_percent
 * - progress_path
 * - text_lessons_count
 * - button_continue_path
 */
public class LearningPathAdapter extends RecyclerView.Adapter<LearningPathAdapter.PathViewHolder> {

    private static final String TAG = "LearningPathAdapter";
    private final List<PathWithProgress> paths = new ArrayList<>();
    private OnPathClickListener listener;

    public interface OnPathClickListener {
        void onPathClick(LearningPath path);
    }

    public void setOnPathClickListener(OnPathClickListener listener) {
        this.listener = listener;
    }

    public void setPaths(List<PathWithProgress> newPaths) {
        Log.d(TAG, "setPaths called with " + (newPaths != null ? newPaths.size() : 0) + " paths");
        paths.clear();
        if (newPaths != null) {
            paths.addAll(newPaths);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PathViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_learning_path, parent, false);
        return new PathViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PathViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder position=" + position);
        if (position >= 0 && position < paths.size()) {
            PathWithProgress item = paths.get(position);
            if (item != null) {
                holder.bind(item, listener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    static class PathViewHolder extends RecyclerView.ViewHolder {
        // Declare views as nullable to handle potential layout mismatches
        private final TextView textEmoji;
        private final TextView textName;
        private final TextView textDescription;
        private final TextView textPercent;
        private final ProgressBar progressBar;
        private final TextView textLessons;
        private final MaterialButton buttonContinue;

        PathViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Find all views with explicit IDs from item_learning_path.xml
            textEmoji = itemView.findViewById(R.id.text_path_emoji);
            textName = itemView.findViewById(R.id.text_path_name);
            textDescription = itemView.findViewById(R.id.text_path_description);
            textPercent = itemView.findViewById(R.id.text_path_progress_percent);
            progressBar = itemView.findViewById(R.id.progress_path);
            textLessons = itemView.findViewById(R.id.text_lessons_count);
            buttonContinue = itemView.findViewById(R.id.button_continue_path);

            // Debug logging
            Log.d(TAG, "ViewHolder created - emoji:" + (textEmoji != null) + 
                    " name:" + (textName != null) + 
                    " desc:" + (textDescription != null) +
                    " percent:" + (textPercent != null) +
                    " progress:" + (progressBar != null) +
                    " lessons:" + (textLessons != null) +
                    " button:" + (buttonContinue != null));
        }

        void bind(PathWithProgress item, OnPathClickListener listener) {
            Log.d(TAG, "bind() called");
            
            try {
                LearningPath path = item.getPath();
                if (path == null) {
                    Log.e(TAG, "Path is null in bind()");
                    return;
                }

                // Safely set emoji
                if (textEmoji != null) {
                    String emoji = path.getIconEmoji();
                    textEmoji.setText(emoji != null && !emoji.isEmpty() ? emoji : "📚");
                }

                // Safely set name
                if (textName != null) {
                    String name = path.getName();
                    textName.setText(name != null && !name.isEmpty() ? name : "Learning Path");
                }

                // Safely set description
                if (textDescription != null) {
                    String desc = path.getDescription();
                    textDescription.setText(desc != null ? desc : "");
                }

                // Get progress values
                int completed = item.getCompletedBugs();
                int total = item.getTotalBugs();
                int percent = item.getProgressPercentage();

                // Safely set percentage
                if (textPercent != null) {
                    textPercent.setText(percent + "%");
                }

                // Safely set progress bar
                if (progressBar != null) {
                    progressBar.setMax(100);
                    progressBar.setProgress(percent);
                }

                // Safely set lessons count
                if (textLessons != null) {
                    if (total > 0) {
                        textLessons.setText(completed + " / " + total + " bugs");
                    } else {
                        textLessons.setText("Start learning");
                    }
                }

                // Safely set button
                if (buttonContinue != null) {
                    String btnText;
                    if (percent == 0) {
                        btnText = "Start";
                    } else if (percent >= 100) {
                        btnText = "Review";
                    } else {
                        btnText = "Continue";
                    }
                    buttonContinue.setText(btnText);
                    
                    buttonContinue.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onPathClick(path);
                        }
                    });
                }

                // Card click
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onPathClick(path);
                    }
                });

                Log.d(TAG, "bind() completed for: " + path.getName());

            } catch (Exception e) {
                Log.e(TAG, "Error in bind()", e);
            }
        }
    }
}
