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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugappproject.R;
import com.example.debugappproject.ui.shop.ShopFragment;
import com.example.debugappproject.util.SoundManager;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - PREMIUM TITLE SELECTOR DIALOG                        ║
 * ║              Beautiful UI for exclusive custom titles                        ║
 * ║              Enhanced with stunning animations & effects                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class PremiumTitleSelectorDialog extends DialogFragment {

    public interface OnPremiumTitleSelectedListener {
        void onTitleSelected(String title);
    }

    private OnPremiumTitleSelectedListener listener;
    private SoundManager soundManager;
    private String currentTitle;
    private String[] titles;
    private LinearLayout noTitleContainer;
    private TextView noTitleCheck;
    private RecyclerView recyclerView;
    private PremiumTitleAdapter adapter;

    public static PremiumTitleSelectorDialog newInstance(String currentTitle) {
        PremiumTitleSelectorDialog dialog = new PremiumTitleSelectorDialog();
        Bundle args = new Bundle();
        args.putString("current_title", currentTitle != null ? currentTitle : "");
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
        
        titles = ShopFragment.getPremiumTitles();
        
        if (getArguments() != null) {
            currentTitle = getArguments().getString("current_title", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_premium_title_selector, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_premium_titles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PremiumTitleAdapter();
        recyclerView.setAdapter(adapter);
        
        // Animate recycler entrance
        recyclerView.setAlpha(0f);
        recyclerView.setTranslationX(-30f);
        recyclerView.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(500)
            .setStartDelay(100)
            .setInterpolator(new OvershootInterpolator(0.8f))
            .start();
        
        // No Title option
        noTitleContainer = view.findViewById(R.id.container_no_title);
        noTitleCheck = view.findViewById(R.id.check_no_title);
        
        boolean isNoTitle = currentTitle == null || currentTitle.isEmpty();
        if (noTitleCheck != null) {
            noTitleCheck.setVisibility(isNoTitle ? View.VISIBLE : View.GONE);
        }
        if (noTitleContainer != null) {
            noTitleContainer.setBackgroundResource(isNoTitle ? 
                R.drawable.bg_title_selected : R.drawable.bg_title_default);
            
            // Animate no-title option entrance
            noTitleContainer.setAlpha(0f);
            noTitleContainer.setTranslationY(20f);
            noTitleContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(300)
                .setInterpolator(new OvershootInterpolator(0.8f))
                .start();
            
            noTitleContainer.setOnClickListener(v -> {
                // Bounce animation
                v.animate()
                    .scaleX(0.95f).scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> v.animate()
                        .scaleX(1.02f).scaleY(1.02f)
                        .setDuration(150)
                        .setInterpolator(new OvershootInterpolator(2f))
                        .withEndAction(() -> v.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(100)
                            .start())
                        .start())
                    .start();
                
                soundManager.playSound(SoundManager.Sound.COIN_COLLECT);
                currentTitle = "";
                ShopFragment.setSelectedTitle(requireContext(), "");
                
                if (listener != null) {
                    listener.onTitleSelected("");
                }
                
                v.postDelayed(this::dismiss, 350);
            });
        }
        
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

    public void setOnPremiumTitleSelectedListener(OnPremiumTitleSelectedListener listener) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear all view references to prevent memory leaks
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
            recyclerView = null;
        }
        adapter = null;
        noTitleContainer = null;
        noTitleCheck = null;
        listener = null;
        soundManager = null;
    }

    private class PremiumTitleAdapter extends RecyclerView.Adapter<PremiumTitleAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_premium_title, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String title = titles[position];
            holder.textTitle.setText(title);
            
            // Highlight current selection
            boolean isSelected = title.equals(currentTitle);
            holder.itemView.setBackgroundResource(isSelected ? 
                R.drawable.bg_title_selected : R.drawable.bg_title_default);
            holder.iconCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            
            // Staggered entrance animation
            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationX(-40f);
            holder.itemView.setScaleX(0.9f);
            holder.itemView.setScaleY(0.9f);
            
            holder.itemView.animate()
                .alpha(1f)
                .translationX(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(350)
                .setStartDelay(position * 60L)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
            
            // Golden text for selected title with shimmer effect
            if (isSelected) {
                holder.textTitle.setTextColor(0xFFFFD700); // Golden color
                startTextShimmer(holder.textTitle);
            } else {
                holder.textTitle.setTextColor(0xFFFFFFFF); // White
            }
            
            holder.itemView.setOnClickListener(v -> {
                // Elegant tap animation
                v.animate()
                    .scaleX(0.97f).scaleY(0.97f)
                    .setDuration(80)
                    .withEndAction(() -> v.animate()
                        .scaleX(1.03f).scaleY(1.03f)
                        .setDuration(150)
                        .setInterpolator(new OvershootInterpolator(3f))
                        .withEndAction(() -> v.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(100)
                            .start())
                        .start())
                    .start();
                
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                
                // Update selection
                currentTitle = title;
                ShopFragment.setSelectedTitle(requireContext(), title);
                
                // Notify listener
                if (listener != null) {
                    listener.onTitleSelected(title);
                }
                
                // Refresh the list
                notifyDataSetChanged();
                
                // Update no-title option
                if (noTitleCheck != null) {
                    noTitleCheck.setVisibility(View.GONE);
                }
                if (noTitleContainer != null) {
                    noTitleContainer.setBackgroundResource(R.drawable.bg_title_default);
                }
                
                // Auto-dismiss after animation
                v.postDelayed(() -> dismiss(), 450);
            });
        }
        
        private void startTextShimmer(TextView textView) {
            ObjectAnimator alpha = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0.7f, 1f);
            alpha.setDuration(1500);
            alpha.setRepeatCount(ValueAnimator.INFINITE);
            alpha.setInterpolator(new AccelerateDecelerateInterpolator());
            alpha.start();
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textTitle;
            TextView iconCheck;

            ViewHolder(View itemView) {
                super(itemView);
                textTitle = itemView.findViewById(R.id.text_title);
                iconCheck = itemView.findViewById(R.id.icon_check);
            }
        }
    }
}
