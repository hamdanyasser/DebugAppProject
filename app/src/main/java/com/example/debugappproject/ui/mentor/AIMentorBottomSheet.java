package com.example.debugappproject.ui.mentor;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.debugappproject.R;
import com.example.debugappproject.databinding.BottomSheetAiMentorBinding;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.util.AIMentor;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * AI Debug Mentor Bottom Sheet - Interactive chat interface for debugging help.
 *
 * Features:
 * - Contextual debugging assistance based on current bug
 * - Chat-style conversation UI
 * - Quick action chips for common questions
 * - Session-based access (free daily + purchasable)
 */
public class AIMentorBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "AIMentorBottomSheet";
    private static final String ARG_BUG = "arg_bug";

    private BottomSheetAiMentorBinding binding;
    private AIMentor mentor;
    private SoundManager soundManager;
    private Bug currentBug;
    private String currentUserCode;
    private OnGetMoreSessionsListener getMoreSessionsListener;
    private Handler handler;

    public interface OnGetMoreSessionsListener {
        void onGetMoreSessions();
    }

    public static AIMentorBottomSheet newInstance(Bug bug) {
        AIMentorBottomSheet fragment = new AIMentorBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BUG, bug);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_DebugMaster_BottomSheet);

        if (getArguments() != null) {
            currentBug = (Bug) getArguments().getSerializable(ARG_BUG);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);

                // Set peek height to 70% of screen
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                behavior.setPeekHeight((int) (screenHeight * 0.7));
            }
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAiMentorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        handler = new Handler(Looper.getMainLooper());
        mentor = new AIMentor(requireContext());
        soundManager = SoundManager.getInstance(requireContext());

        if (currentBug != null) {
            mentor.setBugContext(currentBug);
        }

        setupUI();
        setupClickListeners();
        updateSessionsDisplay();

        // Show greeting
        handler.postDelayed(() -> {
            addMentorMessage(mentor.getGreeting());
        }, 300);
    }

    private void setupUI() {
        // Setup input field
        binding.editMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Update status based on bug context
        if (currentBug != null) {
            binding.textMentorStatus.setText("Helping with: " + currentBug.getTitle());
        }
    }

    private void setupClickListeners() {
        // Send button
        binding.buttonSend.setOnClickListener(v -> sendMessage());

        // Quick action chips
        binding.chipExplain.setOnClickListener(v -> sendQuickAction("Explain the bug"));
        binding.chipHint.setOnClickListener(v -> sendQuickAction("Give me a hint"));
        binding.chipCheckCode.setOnClickListener(v -> sendQuickAction("Check my code"));
        binding.chipStrategy.setOnClickListener(v -> sendQuickAction("How do I start?"));
        binding.chipStuck.setOnClickListener(v -> sendQuickAction("I'm stuck!"));

        // Get more sessions button
        binding.buttonGetSessions.setOnClickListener(v -> {
            soundManager.playButtonClick();
            if (getMoreSessionsListener != null) {
                dismiss();
                getMoreSessionsListener.onGetMoreSessions();
            }
        });

        // Sessions counter click (for info)
        binding.layoutSessions.setOnClickListener(v -> {
            soundManager.playButtonClick();
            int free = mentor.getFreeSessions();
            int purchased = mentor.getPurchasedSessions();
            boolean hasPro = AIMentor.hasUnlimitedAccess(requireContext());

            String message;
            if (hasPro) {
                message = "You have unlimited mentor access with Pro!";
            } else {
                message = "Free sessions today: " + free + "\nPurchased sessions: " + purchased;
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        });
    }

    private void sendMessage() {
        String message = binding.editMessage.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }

        // Check if user has sessions available
        if (!hasSessionsAvailable()) {
            showNoSessionsMessage();
            return;
        }

        // Consume a session
        boolean hasUnlimited = AIMentor.hasUnlimitedAccess(requireContext());
        if (!hasUnlimited) {
            mentor.useSession();
            updateSessionsDisplay();
        }

        soundManager.playSound(SoundManager.Sound.BUTTON_CLICK);
        binding.editMessage.setText("");

        // Add user message
        addUserMessage(message);

        // Show typing indicator
        showTypingIndicator();

        // Get response (with slight delay for natural feel)
        handler.postDelayed(() -> {
            hideTypingIndicator();
            String response = mentor.getResponse(message);
            addMentorMessage(response);
            soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        }, 800 + (int)(Math.random() * 500));
    }

    private void sendQuickAction(String action) {
        binding.editMessage.setText(action);
        sendMessage();
    }

    private void addUserMessage(String message) {
        View messageView = createMessageView(message, true);
        binding.layoutChatMessages.addView(messageView);
        scrollToBottom();
    }

    private void addMentorMessage(String message) {
        View messageView = createMessageView(message, false);
        binding.layoutChatMessages.addView(messageView);
        scrollToBottom();
    }

    private View createMessageView(String message, boolean isUser) {
        Context context = requireContext();
        float density = context.getResources().getDisplayMetrics().density;

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout.LayoutParams marginParams = (LinearLayout.LayoutParams) container.getLayoutParams();
        marginParams.bottomMargin = (int) (12 * density);
        container.setLayoutParams(marginParams);

        TextView textView = new TextView(context);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );

        if (isUser) {
            textParams.gravity = android.view.Gravity.END;
            textView.setBackgroundResource(R.drawable.bg_chat_message_user);
            textParams.leftMargin = (int) (48 * density);
        } else {
            textParams.gravity = android.view.Gravity.START;
            textView.setBackgroundResource(R.drawable.bg_chat_message_mentor);
            textParams.rightMargin = (int) (48 * density);
        }

        textView.setLayoutParams(textParams);
        textView.setText(message);
        textView.setTextColor(getResources().getColor(R.color.white, null));
        textView.setTextSize(15f);
        textView.setPadding(
            (int) (14 * density),
            (int) (10 * density),
            (int) (14 * density),
            (int) (10 * density)
        );

        // Support basic markdown-style formatting
        textView.setLineSpacing(0, 1.2f);

        container.addView(textView);

        // Add timestamp for mentor messages
        if (!isUser) {
            TextView timestamp = new TextView(context);
            LinearLayout.LayoutParams timestampParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            timestampParams.topMargin = (int) (4 * density);
            timestamp.setLayoutParams(timestampParams);
            timestamp.setText("AI Mentor");
            timestamp.setTextColor(0xFF888899);
            timestamp.setTextSize(11f);
            container.addView(timestamp);
        }

        // Animate entry
        container.setAlpha(0f);
        container.setTranslationY(20f);
        container.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(200)
            .start();

        return container;
    }

    private void showTypingIndicator() {
        binding.textMentorStatus.setText("Typing...");
        binding.textMentorStatus.setTextColor(getResources().getColor(R.color.difficulty_medium, null));
    }

    private void hideTypingIndicator() {
        if (currentBug != null) {
            binding.textMentorStatus.setText("Helping with: " + currentBug.getTitle());
        } else {
            binding.textMentorStatus.setText("Ready to help");
        }
        binding.textMentorStatus.setTextColor(0xFF10B981);
    }

    private void scrollToBottom() {
        handler.post(() -> {
            binding.scrollChat.fullScroll(View.FOCUS_DOWN);
        });
    }

    private boolean hasSessionsAvailable() {
        if (AIMentor.hasUnlimitedAccess(requireContext())) {
            return true;
        }
        return mentor.getTotalSessions() > 0;
    }

    private void showNoSessionsMessage() {
        soundManager.playSound(SoundManager.Sound.ERROR);

        addMentorMessage("You've used all your mentor sessions for today!\n\n" +
            "Options:\n" +
            "1. Wait until tomorrow for 3 free sessions\n" +
            "2. Purchase more sessions from the shop\n" +
            "3. Upgrade to Pro for unlimited access!");

        binding.buttonGetSessions.setVisibility(View.VISIBLE);
    }

    private void updateSessionsDisplay() {
        boolean hasUnlimited = AIMentor.hasUnlimitedAccess(requireContext());

        if (hasUnlimited) {
            binding.textSessionsCount.setText("Unlimited");
            binding.buttonGetSessions.setVisibility(View.GONE);
        } else {
            int total = mentor.getTotalSessions();
            binding.textSessionsCount.setText(String.valueOf(total));

            if (total <= 0) {
                binding.buttonGetSessions.setVisibility(View.VISIBLE);
            } else {
                binding.buttonGetSessions.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Update the user's code so mentor can analyze it
     */
    public void updateUserCode(String code) {
        this.currentUserCode = code;
        if (mentor != null) {
            mentor.setUserCode(code);
        }
    }

    /**
     * Set listener for when user wants to get more sessions
     */
    public void setOnGetMoreSessionsListener(OnGetMoreSessionsListener listener) {
        this.getMoreSessionsListener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        binding = null;
    }
}
