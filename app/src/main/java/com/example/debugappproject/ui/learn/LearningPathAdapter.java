package com.example.debugappproject.ui.learn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugappproject.R;
import com.example.debugappproject.model.LearningPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying learning paths as cards.
 */
public class LearningPathAdapter extends RecyclerView.Adapter<LearningPathAdapter.PathViewHolder> {

    private List<PathWithProgress> paths = new ArrayList<>();
    private OnPathClickListener listener;

    public interface OnPathClickListener {
        void onPathClick(LearningPath path);
    }

    public void setOnPathClickListener(OnPathClickListener listener) {
        this.listener = listener;
    }

    public void setPaths(List<PathWithProgress> paths) {
        this.paths = paths;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PathViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_learning_path, parent, false);
        return new PathViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PathViewHolder holder, int position) {
        PathWithProgress pathWithProgress = paths.get(position);
        holder.bind(pathWithProgress, listener);
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    static class PathViewHolder extends RecyclerView.ViewHolder {
        private final TextView textIcon;
        private final TextView textName;
        private final TextView textDifficulty;
        private final TextView textDescription;
        private final TextView textProgress;
        private final ProgressBar progressBar;

        public PathViewHolder(@NonNull View itemView) {
            super(itemView);
            textIcon = itemView.findViewById(R.id.text_icon);
            textName = itemView.findViewById(R.id.text_name);
            textDifficulty = itemView.findViewById(R.id.text_difficulty);
            textDescription = itemView.findViewById(R.id.text_description);
            textProgress = itemView.findViewById(R.id.text_progress);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }

        public void bind(PathWithProgress pathWithProgress, OnPathClickListener listener) {
            LearningPath path = pathWithProgress.getPath();

            textIcon.setText(path.getIconEmoji());
            textName.setText(path.getName());
            textDifficulty.setText(path.getDifficultyRange());
            textDescription.setText(path.getDescription());

            int completed = pathWithProgress.getCompletedBugs();
            int total = pathWithProgress.getTotalBugs();
            textProgress.setText(completed + " / " + total + " bugs completed");
            progressBar.setProgress(pathWithProgress.getProgressPercentage());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPathClick(path);
                }
            });
        }
    }
}
