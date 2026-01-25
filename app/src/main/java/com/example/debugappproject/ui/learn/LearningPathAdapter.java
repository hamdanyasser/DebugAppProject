package com.example.debugappproject.ui.learn;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.debugmaster.app.R;
import com.example.debugappproject.model.LearningPath;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - LEARNING PATH ADAPTER                                ║
 * ║         Duolingo/Mimo-Inspired Path Cards with Pro Features                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class LearningPathAdapter extends RecyclerView.Adapter<LearningPathAdapter.PathViewHolder> {

    private static final String TAG = "LearningPathAdapter";
    private final List<PathWithProgress> paths = new ArrayList<>();
    private OnPathClickListener listener;
    private boolean isProUser = false;

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

    public void setProStatus(boolean isPro) {
        if (this.isProUser != isPro) {
            Log.d(TAG, "setProStatus: isPro=" + isPro);
            this.isProUser = isPro;
            notifyDataSetChanged();
        }
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
        if (position >= 0 && position < paths.size()) {
            PathWithProgress item = paths.get(position);
            if (item != null) {
                holder.bind(item, listener, isProUser);
            }
        }
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    static class PathViewHolder extends RecyclerView.ViewHolder {
        private final TextView textEmoji;
        private final TextView textName;
        private final TextView textDescription;
        private final TextView textPercent;
        private final ProgressBar progressBar;
        private final TextView textLessons;
        private final TextView textTimeEstimate;
        private final TextView textXpReward;
        private final TextView textDifficulty;
        private final TextView textCategory;
        private final TextView badgeNew;
        private final TextView badgePro;
        private final MaterialButton buttonContinue;

        PathViewHolder(@NonNull View itemView) {
            super(itemView);
            
            textEmoji = itemView.findViewById(R.id.text_path_emoji);
            textName = itemView.findViewById(R.id.text_path_name);
            textDescription = itemView.findViewById(R.id.text_path_description);
            textPercent = itemView.findViewById(R.id.text_path_progress_percent);
            progressBar = itemView.findViewById(R.id.progress_path);
            textLessons = itemView.findViewById(R.id.text_lessons_count);
            textTimeEstimate = itemView.findViewById(R.id.text_time_estimate);
            textXpReward = itemView.findViewById(R.id.text_xp_reward);
            textDifficulty = itemView.findViewById(R.id.text_difficulty);
            textCategory = itemView.findViewById(R.id.text_category);
            badgeNew = itemView.findViewById(R.id.badge_new);
            badgePro = itemView.findViewById(R.id.badge_pro);
            buttonContinue = itemView.findViewById(R.id.button_continue_path);
        }

        void bind(PathWithProgress item, OnPathClickListener listener, boolean isProUser) {
            try {
                LearningPath path = item.getPath();
                if (path == null) {
                    Log.e(TAG, "Path is null in bind()");
                    return;
                }

                // Check if path is locked (requires Pro)
                boolean isLocked = path.isLocked() && !isProUser;

                // Emoji
                if (textEmoji != null) {
                    String emoji = path.getIconEmoji();
                    textEmoji.setText(emoji != null && !emoji.isEmpty() ? emoji : "📚");
                }

                // Name
                if (textName != null) {
                    String name = path.getName();
                    textName.setText(name != null && !name.isEmpty() ? name : "Learning Path");
                }

                // Description
                if (textDescription != null) {
                    String desc = path.getDescription();
                    textDescription.setText(desc != null ? desc : "");
                }

                // Progress
                int completed = item.getCompletedBugs();
                int total = item.getTotalBugs();
                int percent = item.getProgressPercentage();

                if (textPercent != null) {
                    textPercent.setText(percent + "%");
                }

                if (progressBar != null) {
                    progressBar.setMax(100);
                    progressBar.setProgress(percent);
                }

                // Lessons count
                if (textLessons != null) {
                    int lessons = path.getTotalLessons();
                    if (lessons > 0) {
                        textLessons.setText(lessons + " lessons");
                    } else if (total > 0) {
                        textLessons.setText(completed + "/" + total + " bugs");
                    } else {
                        textLessons.setText("Start learning");
                    }
                }

                // Time estimate
                if (textTimeEstimate != null) {
                    int minutes = path.getEstimatedMinutes();
                    if (minutes > 0) {
                        if (minutes >= 60) {
                            textTimeEstimate.setText("⏱️ " + (minutes / 60) + "h " + (minutes % 60) + "m");
                        } else {
                            textTimeEstimate.setText("⏱️ " + minutes + " min");
                        }
                    } else {
                        textTimeEstimate.setText("⏱️ ~30 min");
                    }
                }

                // XP Reward
                if (textXpReward != null) {
                    int xp = path.getXpReward();
                    textXpReward.setText("⭐ " + (xp > 0 ? xp : 100) + " XP");
                }

                // Difficulty
                if (textDifficulty != null) {
                    String diff = path.getDifficultyRange();
                    textDifficulty.setText(diff != null ? diff : "Beginner");

                    // Color based on difficulty - with safe fallbacks
                    if (diff != null) {
                        int bgColor = 0x2210B981; // Default Beginner green
                        try {
                            switch (diff) {
                                case "Beginner":
                                    bgColor = Color.parseColor("#2210B981");
                                    break;
                                case "Intermediate":
                                    bgColor = Color.parseColor("#22F59E0B");
                                    break;
                                case "Advanced":
                                    bgColor = Color.parseColor("#22EF4444");
                                    break;
                                case "Expert":
                                    bgColor = Color.parseColor("#228B5CF6");
                                    break;
                            }
                        } catch (IllegalArgumentException e) {
                            Log.w(TAG, "Failed to parse difficulty color", e);
                        }
                        textDifficulty.setBackgroundColor(bgColor);
                    }
                }

                // Category
                if (textCategory != null) {
                    String category = path.getCategory();
                    textCategory.setText(category != null ? category : "Programming");
                }

                // NEW badge
                if (badgeNew != null) {
                    badgeNew.setVisibility(path.isNew() ? View.VISIBLE : View.GONE);
                }

                // PRO/FREE badge
                if (badgePro != null) {
                    if (path.isLocked()) {
                        badgePro.setText("PRO");
                        badgePro.setBackgroundResource(R.drawable.bg_badge_pro);
                        badgePro.setVisibility(View.VISIBLE);
                    } else {
                        // Show FREE badge for unlocked paths
                        badgePro.setText("FREE");
                        badgePro.setBackgroundResource(R.drawable.bg_badge_free);
                        badgePro.setVisibility(View.VISIBLE);
                    }
                }

                // Button styling based on state - using safe color constants
                if (buttonContinue != null) {
                    // Pre-defined safe colors
                    final int COLOR_GOLD = 0xFFFFD54F;
                    final int COLOR_DARK = 0xFF1A1A2E;
                    final int COLOR_PURPLE = 0xFF7C4DFF;
                    final int COLOR_GREEN = 0xFF00E676;

                    if (isLocked) {
                        // Locked - show unlock prompt
                        buttonContinue.setText("🔒 Unlock");
                        buttonContinue.setIconResource(0);
                        buttonContinue.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(COLOR_GOLD));
                        buttonContinue.setTextColor(COLOR_DARK);
                    } else if (percent == 0) {
                        // Not started
                        buttonContinue.setText("Start");
                        buttonContinue.setIconResource(R.drawable.ic_arrow_right);
                        buttonContinue.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(COLOR_PURPLE));
                        buttonContinue.setTextColor(Color.WHITE);
                    } else if (percent >= 100) {
                        // Completed
                        buttonContinue.setText("✅ Done");
                        buttonContinue.setIconResource(0);
                        buttonContinue.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(COLOR_GREEN));
                        buttonContinue.setTextColor(COLOR_DARK);
                    } else {
                        // In progress
                        buttonContinue.setText("Continue");
                        buttonContinue.setIconResource(R.drawable.ic_arrow_right);
                        buttonContinue.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(COLOR_PURPLE));
                        buttonContinue.setTextColor(Color.WHITE);
                    }

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

                // Visual state for locked vs unlocked - using safe color constants
                final int COLOR_GOLD_TEXT = 0xFFFFD54F;
                final int COLOR_PURPLE_TEXT = 0xFF7C4DFF;

                if (isLocked) {
                    // Dim but still attractive to encourage upgrade
                    itemView.setAlpha(0.85f);
                    if (progressBar != null) {
                        progressBar.setProgress(0);
                    }
                    if (textPercent != null) {
                        textPercent.setText("PRO");
                        textPercent.setTextColor(COLOR_GOLD_TEXT);
                    }
                } else {
                    itemView.setAlpha(1.0f);
                    if (textPercent != null) {
                        textPercent.setTextColor(COLOR_PURPLE_TEXT);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error in bind()", e);
            }
        }
    }
}
