package com.example.debugappproject.ui.profile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.debugmaster.app.R;
import com.example.debugappproject.util.AuthManager;
import com.example.debugappproject.util.SoundManager;
import com.example.debugappproject.ui.shop.ShopFragment;

import java.util.Arrays;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - AVATAR SELECTOR DIALOG                               ║
 * ║              Fun emoji avatars for user profiles                             ║
 * ║              Enhanced with beautiful animations                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class AvatarSelectorDialog extends DialogFragment {

    private static final List<String> AVATARS = Arrays.asList(
        // Bugs & Insects
        "🐛", "🐞", "🦗", "🐜", "🦟", "🪲", "🦠",
        // Tech & Coding
        "👨‍💻", "👩‍💻", "🤖", "💻", "🖥️", "⌨️", "🔧",
        // Gaming
        "🎮", "🕹️", "👾", "🎯", "🏆", "🥇", "⭐",
        // Cool Characters
        "😎", "🤓", "🧙‍♂️", "🦸", "🦹", "🥷", "🧑‍🚀",
        // Animals
        "🐱", "🐶", "🦊", "🐼", "🦁", "🐯", "🦄",
        // Fun
        "🚀", "⚡", "🔥", "💎", "🌟", "✨", "💫"
    );

    public interface OnAvatarSelectedListener {
        void onAvatarSelected(String emoji);
    }

    private OnAvatarSelectedListener listener;
    private SoundManager soundManager;
    private String currentAvatar;
    private RecyclerView recyclerView;
    private AvatarAdapter adapter;

    public static AvatarSelectorDialog newInstance(String currentAvatar) {
        AvatarSelectorDialog dialog = new AvatarSelectorDialog();
        Bundle args = new Bundle();
        args.putString("current_avatar", currentAvatar);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        soundManager = SoundManager.getInstance(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogTheme);
        
        if (getArguments() != null) {
            currentAvatar = getArguments().getString("current_avatar", "🐛");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_avatar_selector, container, false);

        recyclerView = view.findViewById(R.id.recycler_avatars);
        // Use requireContext() in try-catch for safety
        try {
            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        } catch (IllegalStateException e) {
            // Fragment not attached - dismiss dialog
            dismiss();
            return view;
        }
        adapter = new AvatarAdapter();
        recyclerView.setAdapter(adapter);
        
        // Animate the recycler entrance
        recyclerView.setAlpha(0f);
        recyclerView.setTranslationY(50f);
        recyclerView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(100)
            .setInterpolator(new OvershootInterpolator(0.8f))
            .start();
        
        view.findViewById(R.id.button_close).setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
            animateDismiss();
        });
        
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void setOnAvatarSelectedListener(OnAvatarSelectedListener listener) {
        this.listener = listener;
    }
    
    private void animateDismiss() {
        View root = getView();
        if (root != null) {
            root.animate()
                .alpha(0f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(200)
                .withEndAction(this::dismiss)
                .start();
        } else {
            dismiss();
        }
    }

    private class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_avatar, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String emoji = AVATARS.get(position);
            holder.textEmoji.setText(emoji);
            
            // Highlight current selection
            boolean isSelected = emoji.equals(currentAvatar);
            holder.itemView.setBackgroundResource(isSelected ? 
                R.drawable.bg_avatar_selected : R.drawable.bg_avatar_default);
            
            // Add subtle pulse to selected item
            if (isSelected) {
                holder.itemView.animate()
                    .scaleX(1.05f).scaleY(1.05f)
                    .setDuration(600)
                    .withEndAction(() -> {
                        if (holder.itemView != null) {
                            holder.itemView.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(600)
                                .start();
                        }
                    })
                    .start();
            } else {
                holder.itemView.setScaleX(1f);
                holder.itemView.setScaleY(1f);
            }
            
            // Staggered entrance animation
            holder.itemView.setAlpha(0f);
            holder.itemView.setScaleX(0.6f);
            holder.itemView.setScaleY(0.6f);
            holder.itemView.animate()
                .alpha(1f)
                .scaleX(isSelected ? 1.05f : 1f)
                .scaleY(isSelected ? 1.05f : 1f)
                .setDuration(300)
                .setStartDelay(position * 15L)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();
            
            holder.itemView.setOnClickListener(v -> {
                // Bounce animation on tap
                v.animate()
                    .scaleX(0.8f).scaleY(0.8f)
                    .setDuration(80)
                    .withEndAction(() -> {
                        v.animate()
                            .scaleX(1.15f).scaleY(1.15f)
                            .setDuration(150)
                            .setInterpolator(new OvershootInterpolator(3f))
                            .withEndAction(() -> {
                                v.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(100)
                                    .start();
                            })
                            .start();
                    })
                    .start();
                
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                
                // Update current selection
                String oldAvatar = currentAvatar;
                currentAvatar = emoji;
                
                if (listener != null) {
                    listener.onAvatarSelected(emoji);
                }
                
                // Update AuthManager - this is for free avatars
                AuthManager.getInstance(requireContext()).updateAvatar(emoji);
                
                // Clear premium avatar selection if user chooses a free one
                if (ShopFragment.hasUnlockedAvatars(requireContext())) {
                    ShopFragment.setSelectedAvatar(requireContext(), "");
                }
                
                // Refresh display to update selection state
                notifyDataSetChanged();
                
                // Dismiss after short delay
                v.postDelayed(() -> dismiss(), 350);
            });
        }

        @Override
        public int getItemCount() {
            return AVATARS.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textEmoji;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textEmoji = itemView.findViewById(R.id.text_emoji);
            }
        }
    }
}
