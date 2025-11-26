package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * LessonQuestion entity representing a quiz question for a lesson.
 * Each lesson can have multiple interactive questions (MCQ, True/False).
 */
@Entity(
    tableName = "lesson_questions",
    foreignKeys = {
        @ForeignKey(entity = Lesson.class, parentColumns = "id", childColumns = "lessonId", onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("lessonId")}
)
public class LessonQuestion {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int lessonId;               // Associated lesson
    private String questionText;        // Question text
    private String questionType;        // "MCQ" or "TRUE_FALSE"
    private String optionsJson;         // JSON array of options (e.g., ["Option A", "Option B", "Option C"])
    private int correctOptionIndex;     // Index of correct option (0-based)
    private String explanation;         // Explanation for the correct answer
    private int orderInLesson;          // Display order within the lesson

    public LessonQuestion() {
    }

    @Ignore
    public LessonQuestion(int lessonId, String questionText, String questionType,
                         String optionsJson, int correctOptionIndex, String explanation, int orderInLesson) {
        this.lessonId = lessonId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.optionsJson = optionsJson;
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = explanation;
        this.orderInLesson = orderInLesson;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public void setCorrectOptionIndex(int correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getOrderInLesson() {
        return orderInLesson;
    }

    public void setOrderInLesson(int orderInLesson) {
        this.orderInLesson = orderInLesson;
    }
}
