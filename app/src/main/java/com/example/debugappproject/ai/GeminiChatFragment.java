package com.example.debugappproject.ai;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.debugmaster.app.R;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Real AI Chat powered by Google Gemini (FREE)
 *
 * Get your free API key at: https://aistudio.google.com/apikey
 */
public class GeminiChatFragment extends Fragment {

    private GeminiAIService geminiService;
    private SoundManager soundManager;

    // Views
    private RecyclerView recyclerChat;
    private TextInputEditText inputMessage;
    private MaterialButton btnSend;
    private View typingIndicator;
    private View bannerApiKey;
    private TextView textStatus;
    private Chip chipHint, chipExplain, chipReview, chipSolution;

    // Data
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private Bug currentBug;

    // Arguments
    private static final String ARG_BUG_ID = "bug_id";
    private static final String ARG_BUG_TITLE = "bug_title";
    private static final String ARG_BUG_CODE = "bug_code";
    private static final String ARG_BUG_FIXED = "bug_fixed";
    private static final String ARG_BUG_EXPLANATION = "bug_explanation";

    public static GeminiChatFragment newInstance(Bug bug) {
        GeminiChatFragment fragment = new GeminiChatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BUG_ID, bug.getId());
        args.putString(ARG_BUG_TITLE, bug.getTitle());
        args.putString(ARG_BUG_CODE, bug.getBrokenCode());
        args.putString(ARG_BUG_FIXED, bug.getFixedCode());
        args.putString(ARG_BUG_EXPLANATION, bug.getExplanation());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gemini_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        geminiService = GeminiAIService.getInstance(requireContext());
        soundManager = SoundManager.getInstance(requireContext());

        findViews(view);
        setupRecyclerView();
        setupListeners();
        checkApiKey();
        loadBugContext();
        showWelcomeMessage();
    }

    private void findViews(View view) {
        recyclerChat = view.findViewById(R.id.recycler_chat);
        inputMessage = view.findViewById(R.id.input_message);
        btnSend = view.findViewById(R.id.btn_send);
        typingIndicator = view.findViewById(R.id.typing_indicator);
        bannerApiKey = view.findViewById(R.id.banner_api_key);
        textStatus = view.findViewById(R.id.text_status);

        chipHint = view.findViewById(R.id.chip_hint);
        chipExplain = view.findViewById(R.id.chip_explain);
        chipReview = view.findViewById(R.id.chip_review);
        chipSolution = view.findViewById(R.id.chip_solution);

        ImageButton btnBack = view.findViewById(R.id.button_back);
        ImageButton btnSettings = view.findViewById(R.id.button_settings);
        MaterialButton btnAddKey = view.findViewById(R.id.btn_add_key);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                soundManager.playButtonClick();
                Navigation.findNavController(v).navigateUp();
            });
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                soundManager.playButtonClick();
                showApiKeyDialog();
            });
        }

        if (btnAddKey != null) {
            btnAddKey.setOnClickListener(v -> showApiKeyDialog());
        }
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(messages);
        recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChat.setAdapter(adapter);
    }

    private void setupListeners() {
        // Send button
        btnSend.setOnClickListener(v -> sendMessage());

        // Enter key sends
        inputMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Enable/disable send button
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                btnSend.setEnabled(s.length() > 0);
            }
        });

        // Quick action chips
        chipHint.setOnClickListener(v -> sendQuickMessage("Give me a hint to find the bug. Use the Socratic method - ask me guiding questions."));
        chipExplain.setOnClickListener(v -> sendQuickMessage("Explain what error or bug is in this code. What should I look for?"));
        chipReview.setOnClickListener(v -> sendQuickMessage("I think I fixed it. Can you review my understanding of what was wrong?"));
        chipSolution.setOnClickListener(v -> sendQuickMessage("I give up. Please show me the solution and explain why it works."));
    }

    private void checkApiKey() {
        if (geminiService.hasApiKey()) {
            bannerApiKey.setVisibility(View.GONE);
            textStatus.setText("Powered by Gemini AI - Ready");
            textStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green));
        } else {
            bannerApiKey.setVisibility(View.VISIBLE);
            textStatus.setText("API key required - tap settings");
            textStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_yellow));
        }
    }

    private void loadBugContext() {
        Bundle args = getArguments();
        if (args != null) {
            String bugTitle = args.getString(ARG_BUG_TITLE, "Unknown Bug");
            String bugCode = args.getString(ARG_BUG_CODE, "");
            String fixedCode = args.getString(ARG_BUG_FIXED, "");
            String explanation = args.getString(ARG_BUG_EXPLANATION, "");

            String systemContext = "You are a Socratic debugging mentor in the DebugMaster app. " +
                    "Your goal is to help students learn debugging by asking guiding questions, not giving direct answers.\n\n" +
                    "RULES:\n" +
                    "1. Use the SOCRATIC METHOD - ask questions that lead students to discover bugs themselves\n" +
                    "2. Be encouraging, patient, and educational\n" +
                    "3. Only reveal solutions if explicitly asked to 'show solution' or 'give up'\n" +
                    "4. Use emojis to be friendly\n" +
                    "5. Keep responses concise (under 200 words)\n\n" +
                    "CURRENT BUG:\n" +
                    "Title: " + bugTitle + "\n\n" +
                    "BUGGY CODE:\n```\n" + bugCode + "\n```\n\n" +
                    "(For your reference only - guide student to find this):\n" +
                    "FIXED CODE:\n```\n" + fixedCode + "\n```\n" +
                    "EXPLANATION: " + explanation;

            geminiService.setSystemContext(systemContext);
        }
    }

    private void showWelcomeMessage() {
        String welcome = "Hey! I'm your AI Debug Mentor powered by Google Gemini.\n\n" +
                "I'll help you find and fix bugs using the Socratic method - " +
                "I'll ask guiding questions instead of just giving you answers.\n\n" +
                "Ready to debug? Ask me anything about the code!";

        addMessage(welcome, false);
    }

    private void sendMessage() {
        String text = inputMessage.getText() != null ? inputMessage.getText().toString().trim() : "";
        if (text.isEmpty()) return;

        inputMessage.setText("");
        sendQuickMessage(text);
    }

    private void sendQuickMessage(String message) {
        if (!geminiService.hasApiKey()) {
            showApiKeyDialog();
            return;
        }

        soundManager.playButtonClick();
        addMessage(message, true);

        geminiService.sendMessage(message, new GeminiAIService.AICallback() {
            @Override
            public void onResponse(String response) {
                if (!isAdded()) return;
                soundManager.playSound(SoundManager.Sound.NOTIFICATION);
                addMessage(response, false);
                typingIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                addMessage("Sorry, I encountered an error: " + error, false);
                typingIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onTyping() {
                if (!isAdded()) return;
                typingIndicator.setVisibility(View.VISIBLE);
                recyclerChat.smoothScrollToPosition(messages.size());
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        ChatMessage message = new ChatMessage(text, isUser);
        messages.add(message);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerChat.smoothScrollToPosition(messages.size() - 1);
    }

    private void showApiKeyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);
        builder.setTitle("Setup Free AI (Gemini)");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        TextView instructions = new TextView(requireContext());
        instructions.setText("Get your FREE API key:\n\n" +
                "1. Go to: aistudio.google.com/apikey\n" +
                "2. Sign in with Google\n" +
                "3. Click 'Create API Key'\n" +
                "4. Copy and paste it below\n\n" +
                "It's 100% FREE with generous limits!");
        instructions.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        instructions.setTextSize(13);
        layout.addView(instructions);

        EditText input = new EditText(requireContext());
        input.setHint("Paste your API key here");
        input.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        input.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary));
        input.setBackgroundResource(R.drawable.bg_input_field);
        input.setPadding(32, 24, 32, 24);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 32;
        input.setLayoutParams(params);

        String existingKey = geminiService.getApiKey();
        if (existingKey != null && !existingKey.isEmpty()) {
            input.setText(existingKey);
        }
        layout.addView(input);

        // Copy URL button
        MaterialButton btnCopyUrl = new MaterialButton(requireContext());
        btnCopyUrl.setText("Copy URL to Clipboard");
        btnCopyUrl.setTextSize(12);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.topMargin = 24;
        btnCopyUrl.setLayoutParams(btnParams);
        btnCopyUrl.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("URL", "https://aistudio.google.com/apikey");
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "URL copied!", Toast.LENGTH_SHORT).show();
        });
        layout.addView(btnCopyUrl);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String key = input.getText().toString().trim();
            if (!key.isEmpty()) {
                geminiService.setApiKey(key);
                checkApiKey();
                Toast.makeText(requireContext(), "API key saved! AI is ready.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);

        if (geminiService.hasApiKey()) {
            builder.setNeutralButton("Remove Key", (dialog, which) -> {
                geminiService.setApiKey("");
                checkApiKey();
                Toast.makeText(requireContext(), "API key removed", Toast.LENGTH_SHORT).show();
            });
        }

        builder.show();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                           CHAT MESSAGE MODEL
    // ═══════════════════════════════════════════════════════════════════════════

    public static class ChatMessage {
        public String text;
        public boolean isUser;
        public long timestamp;

        public ChatMessage(String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
            this.timestamp = System.currentTimeMillis();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                           CHAT ADAPTER
    // ═══════════════════════════════════════════════════════════════════════════

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

        private final List<ChatMessage> messages;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            holder.bind(message);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            CardView cardMessage;
            TextView textMessage, textTime, textSender;

            MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                cardMessage = itemView.findViewById(R.id.card_message);
                textMessage = itemView.findViewById(R.id.text_message);
                textTime = itemView.findViewById(R.id.text_time);
                textSender = itemView.findViewById(R.id.text_sender);
            }

            void bind(ChatMessage message) {
                textMessage.setText(message.text);
                textTime.setText(timeFormat.format(new Date(message.timestamp)));

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardMessage.getLayoutParams();

                if (message.isUser) {
                    // User message - right aligned, purple
                    params.gravity = Gravity.END;
                    params.setMarginStart(dpToPx(48));
                    params.setMarginEnd(0);
                    cardMessage.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_700));
                    textSender.setVisibility(View.GONE);
                } else {
                    // AI message - left aligned, dark
                    params.gravity = Gravity.START;
                    params.setMarginStart(0);
                    params.setMarginEnd(dpToPx(48));
                    cardMessage.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_elevated));
                    textSender.setVisibility(View.VISIBLE);
                    textSender.setText("AI Mentor");
                }

                cardMessage.setLayoutParams(params);

                // Long press to copy
                cardMessage.setOnLongClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Message", message.text);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(requireContext(), "Copied!", Toast.LENGTH_SHORT).show();
                    return true;
                });
            }

            private int dpToPx(int dp) {
                return (int) (dp * itemView.getResources().getDisplayMetrics().density);
            }
        }
    }
}
