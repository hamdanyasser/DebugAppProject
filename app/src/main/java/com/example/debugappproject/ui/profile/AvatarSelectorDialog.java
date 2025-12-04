package com.example.debugappproject.ui.profile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugappproject.R;
import com.example.debugappproject.util.AuthManager;
import com.example.debugappproject.util.SoundManager;

import java.util.Arrays;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - AVATAR SELECTOR DIALOG                               ║
 * ║              Fun emoji avatars for user profiles                             ║
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
        
        RecyclerView recyclerView = view.findViewById(R.id.recycler_avatars);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        recyclerView.setAdapter(new AvatarAdapter());
        
        view.findViewById(R.id.button_close).setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
            dismiss();
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
            
            holder.itemView.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                
                if (listener != null) {
                    listener.onAvatarSelected(emoji);
                }
                
                // Update AuthManager
                AuthManager.getInstance(requireContext()).updateAvatar(emoji);
                
                dismiss();
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
