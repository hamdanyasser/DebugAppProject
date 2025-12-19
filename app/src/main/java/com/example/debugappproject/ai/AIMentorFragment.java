package com.example.debugappproject.ai;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.debugappproject.R;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - AI MENTOR CHAT INTERFACE                             ║
 * ║              Conversational Debugging with Socratic Questioning              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class AIMentorFragment extends Fragment implements AIMentorManager.MentorCallback {

    private static final String ARG_BUG_ID = "bug_id";
    
    private AIMentorManager mentorManager;
    private SoundManager soundManager;
    private Bug currentBug;
    
    // Views
    private LinearLayout chatContainer;
    private ScrollView scrollView;
    private EditText inputField;
    private ImageButton sendButton;
    private ProgressBar typingIndicator;
    private ChipGroup quickActionsGroup;
    private TextView bugTitleText;
    private TextView hintLevelText;
    
    public static AIMentorFragment newInstance(int bugId) {
        AIMentorFragment fragment = new AIMentorFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BUG_ID, bugId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_mentor, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mentorManager = AIMentorManager.getInstance(requireContext());
        soundManager = SoundManager.getInstance(requireContext());
        
        initViews(view);
        setupListeners();
        setupQuickActions();
        loadBug();
        
        // Welcome message
        addAssistantMessage("👋 Hi! I'm your AI Debugging Mentor.\n\n" +
                "I'll help you find and fix bugs using Socratic questioning - " +
                "I'll ask you questions to guide your thinking rather than just giving answers.\n\n" +
                "You can ask me anything or use the quick actions below!");
    }
    
    private void initViews(View view) {
        chatContainer = view.findViewById(R.id.chat_container);
        scrollView = view.findViewById(R.id.scroll_view);
        inputField = view.findViewById(R.id.input_field);
        sendButton = view.findViewById(R.id.send_button);
        typingIndicator = view.findViewById(R.id.typing_indicator);
        quickActionsGroup = view.findViewById(R.id.quick_actions_group);
        bugTitleText = view.findViewById(R.id.bug_title);
        hintLevelText = view.findViewById(R.id.hint_level);
    }
    
    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        
        inputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(s.length() > 0);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Back button
        View backButton = requireView().findViewById(R.id.button_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                soundManager.playButtonClick();
                requireActivity().onBackPressed();
            });
        }
    }
    
    private void setupQuickActions() {
        // Add quick action chips
        String[] actions = {
                "🤔 Ask Socratic Questions",
                "💡 Get Next Hint",
                "📚 Explain the Error",
                "🔍 What's Wrong?",
                "✅ Review My Fix"
        };
        
        for (String action : actions) {
            Chip chip = new Chip(requireContext());
            chip.setText(action);
            chip.setClickable(true);
            chip.setCheckable(false);
            chip.setOnClickListener(v -> handleQuickAction(action));
            quickActionsGroup.addView(chip);
        }
    }
    
    private void handleQuickAction(String action) {
        soundManager.playButtonClick();
        
        if (action.contains("Socratic")) {
            addUserMessage("Help me think through this bug with questions");
            mentorManager.getSocraticQuestions(this);
        } else if (action.contains("Hint")) {
            addUserMessage("Give me a hint");
            mentorManager.getNextHint(this);
            updateHintLevel();
        } else if (action.contains("Explain")) {
            addUserMessage("Explain the root cause of this bug");
            mentorManager.explainRootCause(this);
        } else if (action.contains("Wrong")) {
            addUserMessage("What's wrong with this code?");
            mentorManager.askQuestion("Analyze the bug in this code and explain what's happening, but use Socratic questioning to help me discover it myself.", this);
        } else if (action.contains("Review")) {
            // TODO: Get user's current code and review it
            Toast.makeText(getContext(), "Submit your fix in the code editor first!", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadBug() {
        if (getArguments() != null) {
            int bugId = getArguments().getInt(ARG_BUG_ID, -1);
            if (bugId > 0) {
                // Load bug from database
                new Thread(() -> {
                    try {
                        // TODO: Load actual bug from repository
                        // For now, create a placeholder
                        Bug bug = new Bug(bugId, "Sample Bug", "Java", "Easy", "Loops",
                                "Fix the loop error", "for(i=0; i<10; i++)", "0-9", "Error",
                                "Missing int declaration", "for(int i=0; i<10; i++)", false);
                        
                        requireActivity().runOnUiThread(() -> {
                            currentBug = bug;
                            mentorManager.startSession(bug);
                            bugTitleText.setText("🐛 " + bug.getTitle());
                            updateHintLevel();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }
    
    private void sendMessage() {
        String message = inputField.getText().toString().trim();
        if (message.isEmpty()) return;
        
        soundManager.playButtonClick();
        addUserMessage(message);
        inputField.setText("");
        
        mentorManager.askQuestion(message, this);
    }
    
    private void addUserMessage(String message) {
        View messageView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_chat_user, chatContainer, false);
        
        TextView textView = messageView.findViewById(R.id.message_text);
        textView.setText(message);
        
        chatContainer.addView(messageView);
        scrollToBottom();
    }
    
    private void addAssistantMessage(String message) {
        View messageView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_chat_assistant, chatContainer, false);
        
        TextView textView = messageView.findViewById(R.id.message_text);
        textView.setText(message);
        
        chatContainer.addView(messageView);
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }
    
    private void updateHintLevel() {
        int level = mentorManager.getCurrentHintLevel();
        hintLevelText.setText("Hints: " + level + "/5");
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // MentorCallback Implementation
    // ═══════════════════════════════════════════════════════════════════════
    
    @Override
    public void onResponse(String response) {
        typingIndicator.setVisibility(View.GONE);
        addAssistantMessage(response);
        soundManager.playSound(SoundManager.Sound.SUCCESS);
        updateHintLevel();
    }
    
    @Override
    public void onError(String error) {
        typingIndicator.setVisibility(View.GONE);
        addAssistantMessage("⚠️ " + error + "\n\nTry again or use the local hints!");
    }
    
    @Override
    public void onTyping() {
        typingIndicator.setVisibility(View.VISIBLE);
    }
}
