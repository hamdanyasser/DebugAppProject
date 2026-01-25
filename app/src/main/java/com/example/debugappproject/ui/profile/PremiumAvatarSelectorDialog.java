package com.example.debugappproject.ui.profile;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.debugmaster.app.R;
import com.example.debugappproject.ui.shop.ShopFragment;
import com.example.debugappproject.util.SoundManager;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - PREMIUM AVATAR SELECTOR DIALOG                       ║
 * ║              Beautiful UI for exclusive premium avatars                      ║
 * ║              Enhanced with stunning animations & effects                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class PremiumAvatarSelectorDialog extends DialogFragment {

    public interface OnPremiumAvatarSelectedListener {
        void onAvatarSelected(String emoji);
    }

    private OnPremiumAvatarSelectedListener listener;
    private SoundManager soundManager;
    private String currentAvatar;
    private String[] avatars;
    private TextView selectedAvatarDisplay;
    private RecyclerView recyclerView;
    private PremiumAvatarAdapter adapter;

    public static PremiumAvatarSelectorDialog newInstance(String currentAvatar) {
        PremiumAvatarSelectorDialog dialog = new PremiumAvatarSelectorDialog();
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
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.PremiumDialogTheme);
        
        avatars = ShopFragment.getPremiumAvatars();
        
        if (getArguments() != null) {
            currentAvatar = getArguments().getString("current_avatar", avatars.length > 0 ? avatars[0] : "🦊");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_premium_avatar_selector, container, false);
        
        selectedAvatarDisplay = view.findViewById(R.id.text_selected_avatar);
        if (selectedAvatarDisplay != null && currentAvatar != null) {
            selectedAvatarDisplay.setText(currentAvatar);
            // Add pulse animation to selected avatar display
            startSelectedAvatarPulse();
        }
        
        recyclerView = view.findViewById(R.id.recycler_premium_avatars);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5));
        adapter = new PremiumAvatarAdapter();
        recyclerView.setAdapter(adapter);
        
        // Animate recycler entrance
        recyclerView.setAlpha(0f);
        recyclerView.setTranslationY(30f);
        recyclerView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(150)
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
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    public void setOnPremiumAvatarSelectedListener(OnPremiumAvatarSelectedListener listener) {
        this.listener = listener;
    }
    
    private void startSelectedAvatarPulse() {
        if (selectedAvatarDisplay == null) return;
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(selectedAvatarDisplay, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(selectedAvatarDisplay, "scaleY", 1f, 1.1f, 1f);
        
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        scaleX.start();
        scaleY.start();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear all view references to prevent memory leaks
        if (selectedAvatarDisplay != null) {
            selectedAvatarDisplay.clearAnimation();
            selectedAvatarDisplay = null;
        }
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
            recyclerView = null;
        }
        adapter = null;
        listener = null;
        soundManager = null;
    }

    private class PremiumAvatarAdapter extends RecyclerView.Adapter<PremiumAvatarAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_premium_avatar, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String emoji = avatars[position];
            holder.textAvatar.setText(emoji);
            
            // Highlight current selection with golden glow
            boolean isSelected = emoji.equals(currentAvatar);
            holder.containerAvatar.setBackgroundResource(isSelected ? 
                R.drawable.bg_avatar_premium_selected : R.drawable.bg_avatar_premium_default);
            holder.iconCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            
            // Staggered entrance with golden shimmer effect
            holder.containerAvatar.setAlpha(0f);
            holder.containerAvatar.setScaleX(0.5f);
            holder.containerAvatar.setScaleY(0.5f);
            holder.containerAvatar.setRotation(-10f);
            
            holder.containerAvatar.animate()
                .alpha(1f)
                .scaleX(isSelected ? 1.08f : 1f)
                .scaleY(isSelected ? 1.08f : 1f)
                .rotation(0f)
                .setDuration(400)
                .setStartDelay(position * 50L)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
            
            // Apply continuous pulse to selected item
            if (isSelected) {
                holder.containerAvatar.postDelayed(() -> {
                    startItemPulse(holder.containerAvatar);
                }, 400 + (position * 50L));
            }
            
            holder.containerAvatar.setOnClickListener(v -> {
                // Premium tap animation - more dramatic
                v.animate()
                    .scaleX(0.75f).scaleY(0.75f)
                    .rotation(5f)
                    .setDuration(100)
                    .withEndAction(() -> v.animate()
                        .scaleX(1.2f).scaleY(1.2f)
                        .rotation(0f)
                        .setDuration(200)
                        .setInterpolator(new OvershootInterpolator(4f))
                        .withEndAction(() -> v.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(100)
                            .start())
                        .start())
                    .start();
                
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                
                // Update selection
                currentAvatar = emoji;
                ShopFragment.setSelectedAvatar(requireContext(), emoji);
                
                // Update display with bounce
                if (selectedAvatarDisplay != null) {
                    selectedAvatarDisplay.setText(emoji);
                    selectedAvatarDisplay.animate()
                        .scaleX(1.3f).scaleY(1.3f)
                        .setDuration(200)
                        .withEndAction(() -> selectedAvatarDisplay.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(200)
                            .start())
                        .start();
                }
                
                // Notify listener
                if (listener != null) {
                    listener.onAvatarSelected(emoji);
                }
                
                // Refresh the grid to update selection state
                notifyDataSetChanged();
                
                // Auto-dismiss after animation
                v.postDelayed(() -> dismiss(), 500);
            });
        }
        
        private void startItemPulse(View view) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.08f, 1.12f, 1.08f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.08f, 1.12f, 1.08f);
            
            scaleX.setDuration(1200);
            scaleY.setDuration(1200);
            scaleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
            
            scaleX.start();
            scaleY.start();
        }

        @Override
        public int getItemCount() {
            return avatars.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textAvatar;
            FrameLayout containerAvatar;
            TextView iconCheck;

            ViewHolder(View itemView) {
                super(itemView);
                textAvatar = itemView.findViewById(R.id.text_avatar);
                containerAvatar = itemView.findViewById(R.id.container_avatar);
                iconCheck = itemView.findViewById(R.id.icon_check);
            }
        }
    }
}
