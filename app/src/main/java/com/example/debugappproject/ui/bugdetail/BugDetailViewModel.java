package com.example.debugappproject.ui.bugdetail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.Hint;
import com.example.debugappproject.model.Lesson;
import com.example.debugappproject.model.LessonQuestion;

import java.util.List;

/**
 * ViewModel for BugDetailFragment.
 * Manages bug details, hints, and completion logic.
 */
public class BugDetailViewModel extends AndroidViewModel {

    private final BugRepository repository;
    private LiveData<Bug> currentBug;
    private LiveData<List<Hint>> hints;
    private final MutableLiveData<Lesson> lesson;
    private final MutableLiveData<List<LessonQuestion>> quizQuestions;
    private final MutableLiveData<Integer> currentHintLevel;
    private final MutableLiveData<Boolean> showingSolution;
    private int hintsUsedForCurrentBug; // Track hints used for the current bug

    public BugDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
        lesson = new MutableLiveData<>();
        quizQuestions = new MutableLiveData<>();
        currentHintLevel = new MutableLiveData<>(0);
        showingSolution = new MutableLiveData<>(false);
        hintsUsedForCurrentBug = 0;
    }

    /**
     * Load bug by ID, along with associated lesson and quiz questions.
     */
    public void loadBug(int bugId) {
        currentBug = repository.getBugById(bugId);
        hints = repository.getHintsForBug(bugId);

        // Load lesson and quiz questions in background
        repository.getExecutorService().execute(() -> {
            Lesson lessonData = repository.getLessonDao().getLessonForBugSync(bugId);
            lesson.postValue(lessonData);

            if (lessonData != null) {
                List<LessonQuestion> questions = repository.getLessonDao()
                    .getQuestionsForLessonSync(lessonData.getId());
                quizQuestions.postValue(questions);
            }
        });
    }

    public LiveData<Bug> getCurrentBug() {
        return currentBug;
    }

    public LiveData<List<Hint>> getHints() {
        return hints;
    }

    public LiveData<Lesson> getLesson() {
        return lesson;
    }

    public LiveData<List<LessonQuestion>> getQuizQuestions() {
        return quizQuestions;
    }

    public LiveData<Integer> getCurrentHintLevel() {
        return currentHintLevel;
    }

    public LiveData<Boolean> isShowingSolution() {
        return showingSolution;
    }

    /**
     * Reveal next hint (increment hint level).
     * Also tracks the number of hints used for XP calculation.
     */
    public void revealNextHint() {
        Integer current = currentHintLevel.getValue();
        if (current != null) {
            currentHintLevel.setValue(current + 1);
            hintsUsedForCurrentBug++;
            // Update global hints used count
            repository.incrementHintsUsed();
        }
    }

    /**
     * Reset hints to hidden.
     */
    public void resetHints() {
        currentHintLevel.setValue(0);
        hintsUsedForCurrentBug = 0;
    }

    /**
     * Show solution.
     */
    public void showSolution() {
        showingSolution.setValue(true);
    }

    /**
     * Get number of hints used for the current bug.
     */
    public int getHintsUsedForCurrentBug() {
        return hintsUsedForCurrentBug;
    }

    /**
     * Mark current bug as completed.
     * This is the old method for manual "Mark as Solved" button.
     */
    public void markBugAsCompleted(int bugId, String difficulty) {
        repository.markBugAsCompleted(bugId, difficulty);
    }

    /**
     * Mark bug as completed with XP rewards.
     * Called when user passes all tests.
     *
     * XP rewards:
     * - Easy: 10 XP
     * - Medium: 20 XP
     * - Hard: 30 XP
     * - Bonus +5 XP if solved without hints
     */
    public void markBugAsCompletedWithXP(int bugId, String difficulty) {
        boolean solvedWithoutHints = (hintsUsedForCurrentBug == 0);
        repository.markBugAsCompletedWithXP(bugId, difficulty, solvedWithoutHints);
    }

    /**
     * Save user notes for a bug.
     */
    public void saveBugNotes(int bugId, String notes) {
        repository.updateBugNotes(bugId, notes);
    }
}
