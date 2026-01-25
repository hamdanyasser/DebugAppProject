package com.example.debugappproject.ui.learn;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.debugmaster.app.R;
import com.example.debugappproject.model.LearningPath;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - LEARNING PATH ADAPTER V2                             ║
 * ║        Enhanced adapter with better visuals and animations                   ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class LearningPathAdapterV2 extends RecyclerView.Adapter<LearningPathAdapterV2.PathViewHolder> {

    private static final String TAG = "LearningPathAdapterV2";
    
    private final List<PathWithProgress> paths = new ArrayList<>();
    private OnPathClickListener listener;
    private boolean isProUser = false;
    private int lastAnimatedPosition = -1;

    public interface OnPathClickListener {
        void onPathClick(LearningPath path);
    }

    public void setOnPathClickListener(OnPathClickListener listener) {
        this.listener = listener;
    }

    public void setPaths(List<PathWithProgress> newPaths) {
        Log.d(TAG, "setPaths: " + (newPaths != null ? newPaths.size() : 0) + " paths");
        
        // Use DiffUtil for smooth updates
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PathDiffCallback(paths, newPaths));
        paths.clear();
        if (newPaths != null) {
            paths.addAll(newPaths);
        }
        diffResult.dispatchUpdatesTo(this);
        lastAnimatedPosition = -1;
    }

    public void setProStatus(boolean isPro) {
        if (this.isProUser != isPro) {
            Log.d(TAG, "setProStatus: " + isPro);
            this.isProUser = isPro;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public PathViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_learning_path_v2, parent, false);
        return new PathViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PathViewHolder holder, int position) {
        if (position >= 0 && position < paths.size()) {
            PathWithProgress item = paths.get(position);
            if (item != null) {
                holder.bind(item, listener, isProUser);
                
                // Animate item entrance
                if (position > lastAnimatedPosition) {
                    animateItem(holder.itemView, position);
                    lastAnimatedPosition = position;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    private void animateItem(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setStartDelay(position * 50L)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VIEW HOLDER
    // ═══════════════════════════════════════════════════════════════════════

    static class PathViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView textEmoji;
        private final TextView textName;
        private final TextView textDescription;
        private final TextView badgeNew;
        private final TextView badgeType;
        private final TextView textDifficulty;
        private final TextView textTimeEstimate;
        private final TextView textXpReward;
        private final ProgressBar progressBar;
        private final TextView textPercent;
        private final MaterialButton buttonAction;
        private final View overlayLocked;

        PathViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = (MaterialCardView) itemView;
            textEmoji = itemView.findViewById(R.id.text_path_emoji);
            textName = itemView.findViewById(R.id.text_path_name);
            textDescription = itemView.findViewById(R.id.text_path_description);
            badgeNew = itemView.findViewById(R.id.badge_new);
            badgeType = itemView.findViewById(R.id.badge_type);
            textDifficulty = itemView.findViewById(R.id.text_difficulty);
            textTimeEstimate = itemView.findViewById(R.id.text_time_estimate);
            textXpReward = itemView.findViewById(R.id.text_xp_reward);
            progressBar = itemView.findViewById(R.id.progress_path);
            textPercent = itemView.findViewById(R.id.text_path_progress_percent);
            buttonAction = itemView.findViewById(R.id.button_action);
            overlayLocked = itemView.findViewById(R.id.overlay_locked);
        }

        void bind(PathWithProgress item, OnPathClickListener listener, boolean isProUser) {
            try {
                LearningPath path = item.getPath();
                if (path == null) return;

                boolean isLocked = path.isLocked() && !isProUser;
                int progress = item.getProgressPercentage();

                // ─────────────────────────────────────────────────────────────
                // EMOJI & ICON
                // ─────────────────────────────────────────────────────────────
                if (textEmoji != null) {
                    String emoji = path.getIconEmoji();
                    textEmoji.setText(emoji != null && !emoji.isEmpty() ? emoji : "📚");
                }

                // ─────────────────────────────────────────────────────────────
                // NAME
                // ─────────────────────────────────────────────────────────────
                if (textName != null) {
                    textName.setText(path.getName() != null ? path.getName() : "Learning Path");
                }

                // ─────────────────────────────────────────────────────────────
                // DESCRIPTION
                // ─────────────────────────────────────────────────────────────
                if (textDescription != null) {
                    textDescription.setText(path.getDescription() != null ? path.getDescription() : "");
                }

                // ─────────────────────────────────────────────────────────────
                // NEW BADGE
                // ─────────────────────────────────────────────────────────────
                if (badgeNew != null) {
                    badgeNew.setVisibility(path.isNew() ? View.VISIBLE : View.GONE);
                }

                // ─────────────────────────────────────────────────────────────
                // TYPE BADGE (FREE / PRO)
                // ─────────────────────────────────────────────────────────────
                if (badgeType != null) {
                    if (path.isLocked()) {
                        badgeType.setText("👑 PRO");
                        badgeType.setTextColor(Color.parseColor("#FFD54F"));
                        badgeType.setBackgroundResource(R.drawable.bg_badge_pro);
                    } else {
                        badgeType.setText("FREE");
                        badgeType.setTextColor(Color.parseColor("#10B981"));
                        badgeType.setBackgroundResource(R.drawable.bg_badge_free);
                    }
                    badgeType.setVisibility(View.VISIBLE);
                }

                // ─────────────────────────────────────────────────────────────
                // DIFFICULTY
                // ─────────────────────────────────────────────────────────────
                if (textDifficulty != null) {
                    String diff = path.getDifficultyRange();
                    textDifficulty.setText(diff != null ? diff : "Beginner");
                    
                    int diffColor;
                    switch (diff != null ? diff : "Beginner") {
                        case "Intermediate":
                            diffColor = Color.parseColor("#F59E0B");
                            break;
                        case "Advanced":
                            diffColor = Color.parseColor("#EF4444");
                            break;
                        case "Expert":
                            diffColor = Color.parseColor("#8B5CF6");
                            break;
                        default: // Beginner
                            diffColor = Color.parseColor("#10B981");
                            break;
                    }
                    textDifficulty.setTextColor(diffColor);
                }

                // ─────────────────────────────────────────────────────────────
                // TIME ESTIMATE
                // ─────────────────────────────────────────────────────────────
                if (textTimeEstimate != null) {
                    int minutes = path.getEstimatedMinutes();
                    if (minutes > 0) {
                        if (minutes >= 60) {
                            textTimeEstimate.setText("⏱ " + (minutes / 60) + "h " + (minutes % 60) + "m");
                        } else {
                            textTimeEstimate.setText("⏱ " + minutes + " min");
                        }
                    } else {
                        textTimeEstimate.setText("⏱ ~30 min");
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // XP REWARD
                // ─────────────────────────────────────────────────────────────
                if (textXpReward != null) {
                    int xp = path.getXpReward();
                    textXpReward.setText("⭐ " + (xp > 0 ? xp : 100) + " XP");
                }

                // ─────────────────────────────────────────────────────────────
                // PROGRESS
                // ─────────────────────────────────────────────────────────────
                if (progressBar != null) {
                    progressBar.setMax(100);
                    progressBar.setProgress(isLocked ? 0 : progress);
                }

                if (textPercent != null) {
                    if (isLocked) {
                        textPercent.setText("🔒");
                        textPercent.setTextColor(Color.parseColor("#FFD54F"));
                    } else {
                        textPercent.setText(progress + "%");
                        textPercent.setTextColor(Color.parseColor("#7C4DFF"));
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // ACTION BUTTON
                // ─────────────────────────────────────────────────────────────
                if (buttonAction != null) {
                    if (isLocked) {
                        buttonAction.setText("Unlock");
                        buttonAction.setIcon(null);
                        buttonAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFD54F")));
                        buttonAction.setTextColor(Color.parseColor("#1A1A2E"));
                    } else if (progress >= 100) {
                        buttonAction.setText("✓ Done");
                        buttonAction.setIcon(null);
                        buttonAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#10B981")));
                        buttonAction.setTextColor(Color.WHITE);
                    } else if (progress > 0) {
                        buttonAction.setText("Continue");
                        buttonAction.setIconResource(R.drawable.ic_arrow_right);
                        buttonAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#7C4DFF")));
                        buttonAction.setTextColor(Color.WHITE);
                    } else {
                        buttonAction.setText("Start");
                        buttonAction.setIconResource(R.drawable.ic_arrow_right);
                        buttonAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#7C4DFF")));
                        buttonAction.setTextColor(Color.WHITE);
                    }

                    buttonAction.setOnClickListener(v -> {
                        animateButtonClick(v);
                        if (listener != null) {
                            listener.onPathClick(path);
                        }
                    });
                }

                // ─────────────────────────────────────────────────────────────
                // CARD STYLING
                // ─────────────────────────────────────────────────────────────
                if (isLocked) {
                    itemView.setAlpha(0.9f);
                    if (cardView != null) {
                        cardView.setStrokeColor(Color.parseColor("#3D3D5C"));
                    }
                } else {
                    itemView.setAlpha(1f);
                    if (cardView != null) {
                        cardView.setStrokeColor(Color.parseColor("#2D2D44"));
                    }
                }

                // Card click
                itemView.setOnClickListener(v -> {
                    animateCardClick(v);
                    if (listener != null) {
                        listener.onPathClick(path);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error in bind()", e);
            }
        }

        private void animateButtonClick(View view) {
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start())
                .start();
        }

        private void animateCardClick(View view) {
            view.animate()
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(80)
                .withEndAction(() -> view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start())
                .start();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DIFF CALLBACK
    // ═══════════════════════════════════════════════════════════════════════

    private static class PathDiffCallback extends DiffUtil.Callback {
        private final List<PathWithProgress> oldList;
        private final List<PathWithProgress> newList;

        PathDiffCallback(List<PathWithProgress> oldList, List<PathWithProgress> newList) {
            this.oldList = oldList != null ? oldList : new ArrayList<>();
            this.newList = newList != null ? newList : new ArrayList<>();
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
        public boolean areItemsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).getPath().getId() == newList.get(newPos).getPath().getId();
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            PathWithProgress oldItem = oldList.get(oldPos);
            PathWithProgress newItem = newList.get(newPos);
            return oldItem.getProgressPercentage() == newItem.getProgressPercentage() &&
                   oldItem.getTotalBugs() == newItem.getTotalBugs() &&
                   oldItem.getCompletedBugs() == newItem.getCompletedBugs();
        }
    }
}
