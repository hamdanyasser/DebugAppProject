package com.example.debugappproject.ai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * DEPRECATED - This class is kept for backwards compatibility only.
 * The new AI Mentor game is in com.example.debugappproject.ui.mentor.MentorChatFragment
 * 
 * This class does nothing - all mentor functionality has been moved.
 */
public class MentorChatFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Return empty view
        return new FrameLayout(requireContext());
    }
}
