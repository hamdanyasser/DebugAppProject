package com.example.debugappproject.ui.onboarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugappproject.R;

/**
 * ViewPager2 adapter for onboarding screens.
 */
public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {

    private final Context context;
    private final int[] layouts = {
        R.layout.onboarding_screen_1,
        R.layout.onboarding_screen_2,
        R.layout.onboarding_screen_3,
        R.layout.onboarding_screen_4
    };

    public OnboardingPagerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layouts[viewType], parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        // Nothing to bind - layouts are static
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
