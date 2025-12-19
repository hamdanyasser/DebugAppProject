package com.example.debugappproject.ui.multifile;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.model.MultiFileBug;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Multi-File Debug Challenge - Hunt Bugs Across Multiple Files!
 */
public class MultiFileBugFragment extends Fragment {

    private SoundManager soundManager;
    private Handler handler;
    private MultiFileBug currentBug;
    private int currentFileIndex = 0;
    private int selectedLine = -1;
    private int selectedFileIndex = -1;
    private int hintLevel = 0;
    private int combo = 0;
    private int totalXpEarned = 0;
    private int bugsFound = 0;
    private int currentBugIndex = 0;
    
    // Views
    private TabLayout tabFiles;
    private LinearLayout codeContainer;
    private ScrollView codeScrollView;
    private TextView textBugTitle, textDescription, textTimer, textXp, textCombo;
    private TextView textSelectionInfo;
    private MaterialButton btnHint, btnSubmit, btnNextBug;
    private CountDownTimer timer;
    private long timeRemaining = 0;
    
    private List<MultiFileBug> bugLibrary;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_multi_file_bug, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        soundManager = SoundManager.getInstance(requireContext());
        handler = new Handler(Looper.getMainLooper());
        
        findViews(view);
        setupUI();
        initBugLibrary();
        loadNextBug();
        
        soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
    }
    
    private void findViews(View view) {
        tabFiles = view.findViewById(R.id.tab_files);
        codeContainer = view.findViewById(R.id.container_code);
        codeScrollView = view.findViewById(R.id.scroll_code);
        textBugTitle = view.findViewById(R.id.text_bug_title);
        textDescription = view.findViewById(R.id.text_description);
        textTimer = view.findViewById(R.id.text_timer);
        textXp = view.findViewById(R.id.text_xp);
        textCombo = view.findViewById(R.id.text_combo);
        textSelectionInfo = view.findViewById(R.id.text_selection_info);
        btnHint = view.findViewById(R.id.btn_hint);
        btnSubmit = view.findViewById(R.id.btn_submit);
        btnNextBug = view.findViewById(R.id.btn_next_bug);
        
        View backBtn = view.findViewById(R.id.button_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                showExitConfirmation();
            });
        }
    }
    
    private void setupUI() {
        if (tabFiles != null) {
            tabFiles.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override 
                public void onTabSelected(TabLayout.Tab tab) {
                    soundManager.playSound(SoundManager.Sound.TICK);
                    currentFileIndex = tab.getPosition();
                    displayCurrentFile();
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
        
        if (btnHint != null) {
            btnHint.setOnClickListener(v -> {
                soundManager.playButtonClick();
                showHint();
            });
        }
        
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                soundManager.playButtonClick();
                submitAnswer();
            });
        }
        
        if (btnNextBug != null) {
            btnNextBug.setVisibility(View.GONE);
            btnNextBug.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.BUTTON_START);
                loadNextBug();
            });
        }
        
        updateXpDisplay();
    }
    
    private void initBugLibrary() {
        bugLibrary = new ArrayList<>();
        
        // Bug 1: NullPointer in Service Layer
        MultiFileBug bug1 = new MultiFileBug();
        bug1.setTitle("🎯 NullPointer in Service Layer");
        bug1.setDescription("App crashes when fetching user data. Bug spans Controller → Service → Repository!");
        bug1.setDifficulty("Medium");
        bug1.setXpReward(100);
        bug1.setTimeLimit(180);
        
        bug1.addFile(new MultiFileBug.BugFile("UserController.java", "controller/",
            "@RestController\npublic class UserController {\n    @Autowired\n    private UserService userService;\n    \n    @GetMapping(\"/user/{id}\")\n    public User getUser(@PathVariable Long id) {\n        return userService.findById(id);\n    }\n}",
            "", "java", false));
        
        bug1.addFile(new MultiFileBug.BugFile("UserService.java", "service/",
            "@Service\npublic class UserService {\n    @Autowired\n    private UserRepository repo;\n    \n    public User findById(Long id) {\n        User user = repo.findById(id);\n        return user.getName().toUpperCase(); // BUG!\n    }\n}",
            "", "java", true));
        
        bug1.addFile(new MultiFileBug.BugFile("UserRepository.java", "repository/",
            "@Repository\npublic interface UserRepository {\n    User findById(Long id); // Returns null if not found\n    User save(User user);\n}",
            "", "java", false));
        
        bug1.setBugFileIndex(1);
        bug1.setBugLineNumber(8);
        bug1.getHints().add("💡 Check how returned data is handled");
        bug1.getHints().add("💡 What if findById returns null?");
        bug1.getHints().add("💡 Look at UserService line 8");
        bugLibrary.add(bug1);
        
        // Bug 2: Off-by-One Error
        MultiFileBug bug2 = new MultiFileBug();
        bug2.setTitle("🎯 Missing Last Page Item");
        bug2.setDescription("Users report the last item is missing from paginated results!");
        bug2.setDifficulty("Hard");
        bug2.setXpReward(150);
        bug2.setTimeLimit(200);
        
        bug2.addFile(new MultiFileBug.BugFile("PaginationUtil.java", "util/",
            "public class PaginationUtil {\n    \n    public int getTotalPages(int totalItems, int pageSize) {\n        return totalItems / pageSize; // BUG: Should round up!\n    }\n    \n    public int getOffset(int page, int size) {\n        return page * size;\n    }\n}",
            "", "java", true));
        
        bug2.addFile(new MultiFileBug.BugFile("ProductService.java", "service/",
            "@Service\npublic class ProductService {\n    @Autowired\n    private PaginationUtil pagination;\n    \n    public Page<Product> getProducts(int page, int size) {\n        int totalPages = pagination.getTotalPages(count(), size);\n        List<Product> items = fetchPage(page, size);\n        return new Page<>(items, page, totalPages);\n    }\n}",
            "", "java", false));
        
        bug2.setBugFileIndex(0);
        bug2.setBugLineNumber(4);
        bug2.getHints().add("💡 Think about integer division");
        bug2.getHints().add("💡 25 items / 10 per page = ?");
        bug2.getHints().add("💡 PaginationUtil line 4 - use Math.ceil");
        bugLibrary.add(bug2);
        
        // Bug 3: Memory Leak
        MultiFileBug bug3 = new MultiFileBug();
        bug3.setTitle("🎯 Android Memory Leak");
        bug3.setDescription("LeakCanary reports Fragment view leak after navigation!");
        bug3.setDifficulty("Medium");
        bug3.setXpReward(120);
        bug3.setTimeLimit(150);
        
        bug3.addFile(new MultiFileBug.BugFile("ProfileFragment.java", "ui/",
            "public class ProfileFragment extends Fragment {\n    private FragmentProfileBinding binding;\n    \n    @Override\n    public View onCreateView(LayoutInflater inflater, ViewGroup c, Bundle b) {\n        binding = FragmentProfileBinding.inflate(inflater, c, false);\n        return binding.getRoot();\n    }\n    \n    @Override\n    public void onDestroyView() {\n        super.onDestroyView();\n        // BUG: binding = null missing!\n    }\n}",
            "", "java", true));
        
        bug3.setBugFileIndex(0);
        bug3.setBugLineNumber(12);
        bug3.getHints().add("💡 What happens to binding after view is destroyed?");
        bug3.getHints().add("💡 binding holds references to views");
        bug3.getHints().add("💡 Add: binding = null in onDestroyView");
        bugLibrary.add(bug3);
        
        // Bug 4: String Comparison
        MultiFileBug bug4 = new MultiFileBug();
        bug4.setTitle("🎯 Login Always Fails");
        bug4.setDescription("Users can't login even with correct credentials!");
        bug4.setDifficulty("Easy");
        bug4.setXpReward(80);
        bug4.setTimeLimit(120);
        
        bug4.addFile(new MultiFileBug.BugFile("AuthService.java", "auth/",
            "public class AuthService {\n    \n    public boolean validatePassword(String input, String stored) {\n        return input == stored; // BUG: Use .equals()!\n    }\n    \n    public User login(String email, String password) {\n        User user = userRepo.findByEmail(email);\n        if (user != null && validatePassword(password, user.getPassword())) {\n            return user;\n        }\n        return null;\n    }\n}",
            "", "java", true));
        
        bug4.setBugFileIndex(0);
        bug4.setBugLineNumber(4);
        bug4.getHints().add("💡 How do you compare Strings in Java?");
        bug4.getHints().add("💡 == compares references, not content");
        bug4.getHints().add("💡 Use .equals() for String comparison");
        bugLibrary.add(bug4);
        
        java.util.Collections.shuffle(bugLibrary, new Random());
    }
    
    private void loadNextBug() {
        if (currentBugIndex >= bugLibrary.size()) {
            showCompletionScreen();
            return;
        }
        
        selectedLine = -1;
        selectedFileIndex = -1;
        hintLevel = 0;
        currentFileIndex = 0;
        
        if (btnNextBug != null) btnNextBug.setVisibility(View.GONE);
        if (btnSubmit != null) btnSubmit.setEnabled(true);
        if (textSelectionInfo != null) textSelectionInfo.setText("👆 Tap a line to select it");
        
        currentBug = bugLibrary.get(currentBugIndex);
        displayBug();
        startTimer();
    }
    
    private void displayBug() {
        if (textBugTitle != null) textBugTitle.setText(currentBug.getTitle());
        if (textDescription != null) textDescription.setText(currentBug.getDescription());
        
        if (tabFiles != null) {
            tabFiles.removeAllTabs();
            for (MultiFileBug.BugFile file : currentBug.getFiles()) {
                tabFiles.addTab(tabFiles.newTab().setText(file.fileName));
            }
        }
        displayCurrentFile();
    }
    
    private void displayCurrentFile() {
        if (codeContainer == null || currentBug == null || !isAdded()) return;
        codeContainer.removeAllViews();
        
        if (currentBug.getFiles().isEmpty()) return;
        
        MultiFileBug.BugFile file = currentBug.getFiles().get(currentFileIndex);
        String[] lines = file.brokenCode.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            final int lineNum = i;
            final int fileIdx = currentFileIndex;
            
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(12, 8, 12, 8);
            
            TextView lineNumView = new TextView(requireContext());
            lineNumView.setText(String.format("%3d", i + 1));
            lineNumView.setTextColor(Color.parseColor("#64748B"));
            lineNumView.setTypeface(Typeface.MONOSPACE);
            lineNumView.setTextSize(12);
            lineNumView.setMinWidth(dpToPx(36));
            
            TextView indicator = new TextView(requireContext());
            indicator.setText("○");
            indicator.setTextColor(Color.parseColor("#374151"));
            indicator.setTextSize(12);
            indicator.setPadding(0, 0, 8, 0);
            
            TextView codeView = new TextView(requireContext());
            applySyntaxHighlighting(codeView, lines[i]);
            codeView.setTypeface(Typeface.MONOSPACE);
            codeView.setTextSize(12);
            codeView.setPadding(4, 0, 0, 0);
            
            if (lineNum == selectedLine && fileIdx == selectedFileIndex) {
                row.setBackgroundColor(Color.parseColor("#3B82F6"));
                indicator.setText("●");
                indicator.setTextColor(Color.parseColor("#FBBF24"));
            }
            
            row.setOnClickListener(v -> selectLine(lineNum, fileIdx, row, indicator));
            
            row.addView(indicator);
            row.addView(lineNumView);
            row.addView(codeView);
            codeContainer.addView(row);
        }
    }
    
    private void selectLine(int lineNum, int fileIdx, LinearLayout row, TextView indicator) {
        // Clear previous
        for (int i = 0; i < codeContainer.getChildCount(); i++) {
            View child = codeContainer.getChildAt(i);
            child.setBackgroundColor(Color.TRANSPARENT);
            if (child instanceof LinearLayout && ((LinearLayout) child).getChildCount() > 0) {
                View first = ((LinearLayout) child).getChildAt(0);
                if (first instanceof TextView) {
                    ((TextView) first).setText("○");
                    ((TextView) first).setTextColor(Color.parseColor("#374151"));
                }
            }
        }
        
        selectedLine = lineNum;
        selectedFileIndex = fileIdx;
        row.setBackgroundColor(Color.parseColor("#3B82F6"));
        indicator.setText("●");
        indicator.setTextColor(Color.parseColor("#FBBF24"));
        
        if (textSelectionInfo != null) {
            String fileName = currentBug.getFiles().get(fileIdx).fileName;
            textSelectionInfo.setText("📍 " + fileName + " : Line " + (lineNum + 1));
        }
        
        soundManager.playSound(SoundManager.Sound.TICK);
    }
    
    private void applySyntaxHighlighting(TextView view, String line) {
        SpannableStringBuilder builder = new SpannableStringBuilder(line);
        
        String[] keywords = {"public", "private", "class", "void", "int", "String", 
                "return", "if", "else", "for", "while", "new", "null", "@"};
        
        for (String kw : keywords) {
            int idx = 0;
            while ((idx = line.indexOf(kw, idx)) != -1) {
                builder.setSpan(new ForegroundColorSpan(Color.parseColor("#C084FC")),
                        idx, Math.min(idx + kw.length(), line.length()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                idx += kw.length();
            }
        }
        
        if (line.contains("// BUG") || line.contains("//BUG")) {
            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#EF4444")),
                    0, line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        view.setText(builder);
        view.setTextColor(Color.WHITE);
    }
    
    private void showHint() {
        if (currentBug == null || getContext() == null) return;
        hintLevel++;
        
        if (hintLevel <= currentBug.getHints().size()) {
            String hint = currentBug.getHint(hintLevel);
            Toast.makeText(getContext(), hint, Toast.LENGTH_LONG).show();
            soundManager.playSound(SoundManager.Sound.HINT_REVEAL);
            
            totalXpEarned = Math.max(0, totalXpEarned - 10);
            updateXpDisplay();
        } else {
            Toast.makeText(getContext(), "No more hints!", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void submitAnswer() {
        if (getContext() == null || currentBug == null) return;
        
        if (selectedLine == -1) {
            Toast.makeText(getContext(), "👆 Select the buggy line first!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        boolean correctFile = selectedFileIndex == currentBug.getBugFileIndex();
        boolean correctLine = selectedLine == currentBug.getBugLineNumber() - 1;
        
        if (correctFile && correctLine) {
            onCorrectAnswer();
        } else {
            onWrongAnswer();
        }
    }
    
    private void onCorrectAnswer() {
        if (timer != null) timer.cancel();
        
        combo++;
        bugsFound++;
        int xp = currentBug.getXpReward() + (combo * 10);
        totalXpEarned += xp;
        
        soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);
        
        if (textCombo != null && combo > 1) {
            textCombo.setText("🔥 " + combo + "x COMBO!");
            textCombo.setVisibility(View.VISIBLE);
            animateView(textCombo);
        }
        
        updateXpDisplay();
        
        Toast.makeText(getContext(), "🎉 CORRECT! +" + xp + " XP", Toast.LENGTH_LONG).show();
        
        if (btnSubmit != null) btnSubmit.setEnabled(false);
        if (btnNextBug != null) {
            btnNextBug.setVisibility(View.VISIBLE);
            animateView(btnNextBug);
        }
        
        currentBugIndex++;
    }
    
    private void onWrongAnswer() {
        combo = 0;
        if (textCombo != null) textCombo.setVisibility(View.GONE);
        
        soundManager.playSound(SoundManager.Sound.ERROR);
        Toast.makeText(getContext(), "❌ Not quite! Try again or use a hint.", Toast.LENGTH_SHORT).show();
    }
    
    private void showCompletionScreen() {
        if (timer != null) timer.cancel();
        
        if (getContext() == null) return;
        
        new AlertDialog.Builder(requireContext())
                .setTitle("🏆 Challenge Complete!")
                .setMessage("Bugs Found: " + bugsFound + "/" + bugLibrary.size() + "\n" +
                        "Total XP: " + totalXpEarned + "\n\n" +
                        "Great debugging skills!")
                .setPositiveButton("Play Again", (d, w) -> {
                    currentBugIndex = 0;
                    bugsFound = 0;
                    totalXpEarned = 0;
                    combo = 0;
                    java.util.Collections.shuffle(bugLibrary);
                    loadNextBug();
                })
                .setNegativeButton("Exit", (d, w) -> {
                    if (getView() != null) {
                        Navigation.findNavController(getView()).navigateUp();
                    }
                })
                .setCancelable(false)
                .show();
    }
    
    private void showExitConfirmation() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Exit Challenge?")
                .setMessage("You'll lose your progress. XP earned: " + totalXpEarned)
                .setPositiveButton("Exit", (d, w) -> {
                    if (getView() != null) {
                        Navigation.findNavController(getView()).navigateUp();
                    }
                })
                .setNegativeButton("Continue", null)
                .show();
    }
    
    private void updateXpDisplay() {
        if (textXp != null) {
            textXp.setText("⭐ " + totalXpEarned + " XP");
        }
    }
    
    private void animateView(View view) {
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1.1f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1.1f, 1f));
        anim.setDuration(400);
        anim.setInterpolator(new OvershootInterpolator(2f));
        anim.start();
    }
    
    private void startTimer() {
        if (currentBug == null || currentBug.getTimeLimit() <= 0) return;
        
        if (timer != null) timer.cancel();
        
        timer = new CountDownTimer(currentBug.getTimeLimit() * 1000L, 1000) {
            @Override 
            public void onTick(long ms) {
                if (!isAdded()) { cancel(); return; }
                timeRemaining = ms;
                if (textTimer != null) {
                    textTimer.setText(String.format("⏱️ %d:%02d", ms/60000, (ms%60000)/1000));
                }
            }
            @Override 
            public void onFinish() { 
                if (!isAdded()) return;
                if (textTimer != null) textTimer.setText("⏱️ Time's up!");
                combo = 0;
                currentBugIndex++;
                handler.postDelayed(() -> loadNextBug(), 1500);
            }
        }.start();
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) { timer.cancel(); timer = null; }
        if (handler != null) handler.removeCallbacksAndMessages(null);
        tabFiles = null;
        codeContainer = null;
        codeScrollView = null;
        textBugTitle = null;
        textDescription = null;
        textTimer = null;
        textXp = null;
        textCombo = null;
        textSelectionInfo = null;
        btnHint = null;
        btnSubmit = null;
        btnNextBug = null;
    }
}
