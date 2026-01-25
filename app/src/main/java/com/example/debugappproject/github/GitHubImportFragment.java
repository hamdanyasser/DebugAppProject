package com.example.debugappproject.github;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.debugmaster.app.R;
import com.example.debugappproject.data.local.BugDao;
import com.example.debugappproject.data.local.DebugMasterDatabase;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              🐙 BUG LIBRARY - REAL-WORLD DEBUGGING CHALLENGES               ║
 * ║                    Import and Play Instantly - No API Needed!               ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class GitHubImportFragment extends Fragment {

    private SoundManager soundManager;
    private ExecutorService executor;
    private Handler handler;
    private Random random;
    
    // Views
    private View rootView;
    private LinearLayout bugsContainer;
    private ChipGroup filterChips;
    private TextView textTotal, textImported;
    private MaterialButton btnImportAll, btnPlayNow;
    
    // Data
    private List<LibraryBug> allBugs;
    private List<LibraryBug> filteredBugs;
    private String currentFilter = "All";
    private int importedCount = 0;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bug_library, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rootView = view;
        soundManager = SoundManager.getInstance(requireContext());
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
        random = new Random();
        
        initBugLibrary();
        findViews();
        setupUI();
        displayBugs();
        
        soundManager.playSound(SoundManager.Sound.TRANSITION);
    }
    
    private void findViews() {
        bugsContainer = rootView.findViewById(R.id.container_bugs);
        filterChips = rootView.findViewById(R.id.chip_group_filters);
        textTotal = rootView.findViewById(R.id.text_total);
        textImported = rootView.findViewById(R.id.text_imported);
        btnImportAll = rootView.findViewById(R.id.btn_import_all);
        btnPlayNow = rootView.findViewById(R.id.btn_play_now);
        
        View backBtn = rootView.findViewById(R.id.button_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                soundManager.playButtonClick();
                if (getView() != null) Navigation.findNavController(getView()).navigateUp();
            });
        }
    }
    
    private void setupUI() {
        // Filter chips
        setupFilterChips();
        
        // Import all button
        if (btnImportAll != null) {
            btnImportAll.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                importAllBugs();
            });
        }
        
        // Play now button
        if (btnPlayNow != null) {
            btnPlayNow.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.BUTTON_START);
                playRandomBug();
            });
        }
        
        updateStats();
    }
    
    private void setupFilterChips() {
        if (filterChips == null) return;
        
        String[] filters = {"All", "Java", "Python", "JavaScript", "Easy", "Medium", "Hard"};
        
        for (String filter : filters) {
            Chip chip = new Chip(requireContext());
            chip.setText(filter);
            chip.setCheckable(true);
            chip.setChecked(filter.equals("All"));
            chip.setChipBackgroundColorResource(R.color.surface_dark);
            chip.setTextColor(Color.WHITE);
            chip.setChipStrokeColorResource(R.color.purple_500);
            chip.setChipStrokeWidth(2f);
            
            chip.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.TICK);
                currentFilter = filter;
                filterBugs();
                displayBugs();
                
                // Update chip states
                for (int i = 0; i < filterChips.getChildCount(); i++) {
                    Chip c = (Chip) filterChips.getChildAt(i);
                    c.setChecked(c.getText().toString().equals(filter));
                }
            });
            
            filterChips.addView(chip);
        }
    }
    
    private void filterBugs() {
        filteredBugs = new ArrayList<>();
        
        for (LibraryBug bug : allBugs) {
            if (currentFilter.equals("All")) {
                filteredBugs.add(bug);
            } else if (currentFilter.equalsIgnoreCase(bug.language)) {
                filteredBugs.add(bug);
            } else if (currentFilter.equalsIgnoreCase(bug.difficulty)) {
                filteredBugs.add(bug);
            }
        }
    }
    
    private void displayBugs() {
        if (bugsContainer == null) return;
        bugsContainer.removeAllViews();
        
        for (int i = 0; i < filteredBugs.size(); i++) {
            LibraryBug bug = filteredBugs.get(i);
            View card = createBugCard(bug, i);
            bugsContainer.addView(card);
        }
        
        updateStats();
    }
    
    private View createBugCard(LibraryBug bug, int index) {
        CardView card = new CardView(requireContext());
        card.setCardBackgroundColor(Color.parseColor("#1E293B"));
        card.setRadius(dpToPx(12));
        card.setCardElevation(dpToPx(4));
        
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, dpToPx(8), 0, dpToPx(8));
        card.setLayoutParams(cardParams);
        
        // Content layout
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));
        
        // Top row: Title + XP
        LinearLayout topRow = new LinearLayout(requireContext());
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        // Difficulty emoji
        TextView emoji = new TextView(requireContext());
        emoji.setText(getDifficultyEmoji(bug.difficulty));
        emoji.setTextSize(20);
        emoji.setPadding(0, 0, dpToPx(8), 0);
        topRow.addView(emoji);
        
        // Title
        TextView title = new TextView(requireContext());
        title.setText(bug.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(15);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(titleParams);
        topRow.addView(title);
        
        // XP Badge
        TextView xpBadge = new TextView(requireContext());
        xpBadge.setText("⭐ " + bug.xpReward + " XP");
        xpBadge.setTextColor(Color.parseColor("#F59E0B"));
        xpBadge.setTextSize(13);
        xpBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        topRow.addView(xpBadge);
        
        content.addView(topRow);
        
        // Tags row
        LinearLayout tagsRow = new LinearLayout(requireContext());
        tagsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tagsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tagsParams.setMargins(0, dpToPx(8), 0, 0);
        tagsRow.setLayoutParams(tagsParams);
        
        // Language tag
        TextView langTag = createTag(bug.language, "#3B82F6");
        tagsRow.addView(langTag);
        
        // Category tag
        TextView catTag = createTag(bug.category, "#8B5CF6");
        catTag.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams) catTag.getLayoutParams()).setMargins(dpToPx(8), 0, 0, 0);
        tagsRow.addView(catTag);
        
        // Difficulty tag
        TextView diffTag = createTag(bug.difficulty, getDifficultyColor(bug.difficulty));
        diffTag.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams) diffTag.getLayoutParams()).setMargins(dpToPx(8), 0, 0, 0);
        tagsRow.addView(diffTag);
        
        content.addView(tagsRow);
        
        // Description
        TextView desc = new TextView(requireContext());
        desc.setText(bug.description);
        desc.setTextColor(Color.parseColor("#9CA3AF"));
        desc.setTextSize(13);
        desc.setMaxLines(2);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        descParams.setMargins(0, dpToPx(8), 0, 0);
        desc.setLayoutParams(descParams);
        content.addView(desc);
        
        // Buttons row
        LinearLayout btnRow = new LinearLayout(requireContext());
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams btnRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnRowParams.setMargins(0, dpToPx(12), 0, 0);
        btnRow.setLayoutParams(btnRowParams);
        
        // Preview button
        MaterialButton btnPreview = new MaterialButton(requireContext());
        btnPreview.setText("👁️ Preview");
        btnPreview.setTextSize(12);
        btnPreview.setAllCaps(false);
        btnPreview.setBackgroundColor(Color.parseColor("#374151"));
        btnPreview.setCornerRadius(dpToPx(8));
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(0, dpToPx(40), 1f);
        previewParams.setMargins(0, 0, dpToPx(8), 0);
        btnPreview.setLayoutParams(previewParams);
        btnPreview.setOnClickListener(v -> showPreview(bug));
        btnRow.addView(btnPreview);
        
        // Play button
        MaterialButton btnPlay = new MaterialButton(requireContext());
        btnPlay.setText("▶️ Play");
        btnPlay.setTextSize(12);
        btnPlay.setAllCaps(false);
        btnPlay.setBackgroundColor(Color.parseColor("#22C55E"));
        btnPlay.setCornerRadius(dpToPx(8));
        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(0, dpToPx(40), 1f);
        btnPlay.setLayoutParams(playParams);
        btnPlay.setOnClickListener(v -> playBug(bug));
        btnRow.addView(btnPlay);
        
        content.addView(btnRow);
        card.addView(content);
        
        // Entrance animation
        card.setAlpha(0f);
        card.setTranslationY(50f);
        handler.postDelayed(() -> {
            if (!isAdded()) return;
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(index * 50L)
                .start();
        }, 100);
        
        return card;
    }
    
    private TextView createTag(String text, String color) {
        TextView tag = new TextView(requireContext());
        tag.setText(text);
        tag.setTextColor(Color.WHITE);
        tag.setTextSize(11);
        tag.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        tag.setBackgroundColor(Color.parseColor(color));
        return tag;
    }
    
    private String getDifficultyEmoji(String diff) {
        switch (diff.toLowerCase()) {
            case "easy": return "🟢";
            case "medium": return "🟡";
            case "hard": return "🔴";
            default: return "⚪";
        }
    }
    
    private String getDifficultyColor(String diff) {
        switch (diff.toLowerCase()) {
            case "easy": return "#22C55E";
            case "medium": return "#F59E0B";
            case "hard": return "#EF4444";
            default: return "#6B7280";
        }
    }
    
    private void showPreview(LibraryBug bug) {
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        String preview = "📝 " + bug.title + "\n\n" +
                "🐛 Buggy Code:\n" + bug.brokenCode + "\n\n" +
                "💡 Hint: " + bug.hint;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Bug Preview")
            .setMessage(preview)
            .setPositiveButton("▶️ Play This", (d, w) -> playBug(bug))
            .setNegativeButton("Close", null)
            .show();
    }
    
    private void playBug(LibraryBug bug) {
        soundManager.playSound(SoundManager.Sound.BUTTON_START);
        
        // Import to database and start game
        executor.execute(() -> {
            Bug dbBug = new Bug();
            dbBug.setTitle(bug.title);
            dbBug.setDescription(bug.description);
            dbBug.setBrokenCode(bug.brokenCode);
            dbBug.setFixedCode(bug.fixedCode);
            dbBug.setLanguage(bug.language);
            dbBug.setCategory(bug.category);
            dbBug.setDifficulty(bug.difficulty);
            dbBug.setHintText(bug.hint);
            dbBug.setExplanation(bug.explanation);
            dbBug.setXpReward(bug.xpReward);
            
            BugDao dao = DebugMasterDatabase.getInstance(requireContext()).bugDao();
            long id = dao.insertBug(dbBug);
            
            handler.post(() -> {
                if (!isAdded()) return;
                
                Bundle args = new Bundle();
                args.putLong("bug_id", id);
                args.putString("gameMode", "practice");
                
                try {
                    Navigation.findNavController(rootView).navigate(R.id.gameSessionFragment, args);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Starting game...", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    
    private void playRandomBug() {
        if (filteredBugs.isEmpty()) {
            Toast.makeText(getContext(), "No bugs to play!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        LibraryBug bug = filteredBugs.get(random.nextInt(filteredBugs.size()));
        playBug(bug);
    }
    
    private void importAllBugs() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Import All Bugs?")
            .setMessage("Import " + filteredBugs.size() + " bugs to your practice library?")
            .setPositiveButton("Import", (d, w) -> {
                executor.execute(() -> {
                    BugDao dao = DebugMasterDatabase.getInstance(requireContext()).bugDao();
                    int count = 0;
                    
                    for (LibraryBug bug : filteredBugs) {
                        Bug dbBug = new Bug();
                        dbBug.setTitle(bug.title);
                        dbBug.setDescription(bug.description);
                        dbBug.setBrokenCode(bug.brokenCode);
                        dbBug.setFixedCode(bug.fixedCode);
                        dbBug.setLanguage(bug.language);
                        dbBug.setCategory(bug.category);
                        dbBug.setDifficulty(bug.difficulty);
                        dbBug.setHintText(bug.hint);
                        dbBug.setExplanation(bug.explanation);
                        dbBug.setXpReward(bug.xpReward);
                        dao.insertBug(dbBug);
                        count++;
                    }
                    
                    int finalCount = count;
                    handler.post(() -> {
                        if (!isAdded()) return;
                        importedCount += finalCount;
                        updateStats();
                        soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);
                        Toast.makeText(getContext(), "✅ Imported " + finalCount + " bugs!", Toast.LENGTH_LONG).show();
                    });
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void updateStats() {
        if (textTotal != null) textTotal.setText(filteredBugs.size() + " bugs");
        if (textImported != null) textImported.setText(importedCount + " imported");
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         BUG LIBRARY DATABASE
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void initBugLibrary() {
        allBugs = new ArrayList<>();
        
        // ==================== JAVA BUGS ====================
        allBugs.add(new LibraryBug(
            "NullPointer in User Service",
            "Java", "Null Handling", "Easy",
            "Crashes when processing null users",
            "public String greet(User user) {\n    return \"Hello, \" + user.getName();\n}",
            "public String greet(User user) {\n    if (user == null) return \"Hello, Guest\";\n    return \"Hello, \" + user.getName();\n}",
            "What if user is null?",
            "Always check for null before calling methods on objects!",
            40
        ));
        
        allBugs.add(new LibraryBug(
            "Array Index Out of Bounds",
            "Java", "Arrays", "Easy",
            "Loop goes one element too far",
            "int[] nums = {1, 2, 3};\nfor (int i = 0; i <= nums.length; i++) {\n    sum += nums[i];\n}",
            "int[] nums = {1, 2, 3};\nfor (int i = 0; i < nums.length; i++) {\n    sum += nums[i];\n}",
            "Check the loop condition carefully",
            "Arrays are 0-indexed. length gives count, not last index!",
            35
        ));
        
        allBugs.add(new LibraryBug(
            "String Comparison Fail",
            "Java", "Strings", "Easy",
            "User input 'yes' doesn't match",
            "String input = scanner.nextLine();\nif (input == \"yes\") {\n    proceed();\n}",
            "String input = scanner.nextLine();\nif (input.equals(\"yes\")) {\n    proceed();\n}",
            "How do you compare Strings in Java?",
            "== compares references, .equals() compares content!",
            40
        ));
        
        allBugs.add(new LibraryBug(
            "Infinite Loop Terror",
            "Java", "Loops", "Easy",
            "Program hangs forever",
            "int i = 0;\nwhile (i < 10) {\n    System.out.println(i);\n}",
            "int i = 0;\nwhile (i < 10) {\n    System.out.println(i);\n    i++;\n}",
            "Does the loop variable change?",
            "Loops need their condition to eventually become false!",
            30
        ));
        
        allBugs.add(new LibraryBug(
            "Integer Division Surprise",
            "Java", "Math", "Medium",
            "Average calculation returns wrong value",
            "int total = 7;\nint count = 2;\ndouble avg = total / count; // Returns 3.0!",
            "int total = 7;\nint count = 2;\ndouble avg = (double) total / count; // Returns 3.5",
            "What type is total/count?",
            "Integer division truncates! Cast to double first.",
            50
        ));
        
        allBugs.add(new LibraryBug(
            "ConcurrentModification Horror",
            "Java", "Collections", "Hard",
            "Exception when removing from list",
            "for (String s : list) {\n    if (s.equals(\"x\")) {\n        list.remove(s);\n    }\n}",
            "Iterator<String> it = list.iterator();\nwhile (it.hasNext()) {\n    if (it.next().equals(\"x\")) {\n        it.remove();\n    }\n}",
            "Can you modify while iterating?",
            "Use Iterator.remove() or list.removeIf()!",
            70
        ));
        
        allBugs.add(new LibraryBug(
            "Resource Leak",
            "Java", "IO", "Medium",
            "File handle never released",
            "FileInputStream f = new FileInputStream(\"f.txt\");\nbyte[] data = f.readAllBytes();\n// Done?",
            "try (FileInputStream f = new FileInputStream(\"f.txt\")) {\n    byte[] data = f.readAllBytes();\n}",
            "What happens to the file handle?",
            "Streams must be closed! Use try-with-resources.",
            55
        ));
        
        allBugs.add(new LibraryBug(
            "StringBuilder vs String",
            "Java", "Performance", "Medium",
            "String building is extremely slow",
            "String s = \"\";\nfor (int i = 0; i < 10000; i++) {\n    s += i;\n}",
            "StringBuilder sb = new StringBuilder();\nfor (int i = 0; i < 10000; i++) {\n    sb.append(i);\n}\nString s = sb.toString();",
            "How many String objects are created?",
            "String += creates new objects each time. StringBuilder is O(n) vs O(n²)!",
            60
        ));
        
        // ==================== PYTHON BUGS ====================
        allBugs.add(new LibraryBug(
            "Mutable Default Argument",
            "Python", "Functions", "Hard",
            "List default argument behaves weirdly",
            "def add(item, lst=[]):\n    lst.append(item)\n    return lst\n\n# add('a'), add('b') returns ['a', 'b']!",
            "def add(item, lst=None):\n    if lst is None:\n        lst = []\n    lst.append(item)\n    return lst",
            "When is the default list created?",
            "Default mutable arguments are shared between calls!",
            65
        ));
        
        allBugs.add(new LibraryBug(
            "Range Off-by-One",
            "Python", "Loops", "Easy",
            "Loop prints 1-9 instead of 1-10",
            "# Print 1 to 10\nfor i in range(1, 10):\n    print(i)",
            "# Print 1 to 10\nfor i in range(1, 11):\n    print(i)",
            "What does range(1, 10) include?",
            "range() excludes the end value!",
            35
        ));
        
        allBugs.add(new LibraryBug(
            "List Shallow Copy Bug",
            "Python", "Collections", "Medium",
            "Changing copy affects original",
            "original = [[1, 2], [3, 4]]\ncopy = original.copy()\ncopy[0][0] = 99  # original also changed!",
            "import copy\noriginal = [[1, 2], [3, 4]]\ndeep = copy.deepcopy(original)\ndeep[0][0] = 99  # original unchanged",
            "What does .copy() actually copy?",
            "Shallow copy only copies outer list, inner lists are shared!",
            55
        ));
        
        // ==================== JAVASCRIPT BUGS ====================
        allBugs.add(new LibraryBug(
            "Equality vs Strict Equality",
            "JavaScript", "Comparisons", "Easy",
            "'0' and false match unexpectedly",
            "if (value == false) {\n    // '0', '', null all trigger this!\n}",
            "if (value === false) {\n    // Only actual false triggers this\n}",
            "What's the difference between == and ===?",
            "=== checks type AND value, == does type coercion!",
            40
        ));
        
        allBugs.add(new LibraryBug(
            "Missing Await",
            "JavaScript", "Async", "Medium",
            "Data is undefined even though API works",
            "async function load() {\n    const data = fetchAPI(); // Missing await!\n    console.log(data); // Logs Promise\n}",
            "async function load() {\n    const data = await fetchAPI();\n    console.log(data); // Logs actual data\n}",
            "What does fetchAPI return?",
            "Async functions return Promises. Use await to get the value!",
            50
        ));
        
        allBugs.add(new LibraryBug(
            "Closure in Loop",
            "JavaScript", "Closures", "Hard",
            "All timeouts print the same number",
            "for (var i = 0; i < 3; i++) {\n    setTimeout(() => console.log(i), 100);\n}\n// Prints: 3, 3, 3",
            "for (let i = 0; i < 3; i++) {\n    setTimeout(() => console.log(i), 100);\n}\n// Prints: 0, 1, 2",
            "What value does i have when setTimeout runs?",
            "var is function-scoped. let creates new binding per iteration!",
            70
        ));
        
        allBugs.add(new LibraryBug(
            "Array Methods Mutation",
            "JavaScript", "Arrays", "Medium",
            "Original array changed unexpectedly",
            "const arr = [3, 1, 2];\nconst sorted = arr.sort();\n// arr is now also sorted!",
            "const arr = [3, 1, 2];\nconst sorted = [...arr].sort();\n// arr is unchanged",
            "Does sort() return a new array?",
            "sort(), reverse(), splice() mutate in place! Copy first.",
            55
        ));
        
        // Shuffle for variety
        Collections.shuffle(allBugs, random);
        filteredBugs = new ArrayList<>(allBugs);
    }
    
    // Bug data class
    private static class LibraryBug {
        String title, language, category, difficulty, description;
        String brokenCode, fixedCode, hint, explanation;
        int xpReward;
        
        LibraryBug(String title, String language, String category, String difficulty,
                  String description, String brokenCode, String fixedCode,
                  String hint, String explanation, int xpReward) {
            this.title = title;
            this.language = language;
            this.category = category;
            this.difficulty = difficulty;
            this.description = description;
            this.brokenCode = brokenCode;
            this.fixedCode = fixedCode;
            this.hint = hint;
            this.explanation = explanation;
            this.xpReward = xpReward;
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executor != null && !executor.isShutdown()) executor.shutdown();
        if (handler != null) handler.removeCallbacksAndMessages(null);
        
        rootView = null;
        bugsContainer = null;
        filterChips = null;
        textTotal = null;
        textImported = null;
        btnImportAll = null;
        btnPlayNow = null;
    }
}
