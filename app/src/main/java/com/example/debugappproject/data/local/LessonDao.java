package com.example.debugappproject.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.debugappproject.model.Lesson;
import com.example.debugappproject.model.LessonQuestion;

import java.util.List;

/**
 * DAO for Lesson and LessonQuestion operations.
 */
@Dao
public interface LessonDao {

    // Lesson queries
    @Query("SELECT * FROM lessons WHERE bugId = :bugId LIMIT 1")
    LiveData<Lesson> getLessonForBug(int bugId);

    @Query("SELECT * FROM lessons WHERE bugId = :bugId LIMIT 1")
    Lesson getLessonForBugSync(int bugId);

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    LiveData<Lesson> getLessonById(int lessonId);

    @Insert
    void insertLesson(Lesson lesson);

    @Insert
    void insertAllLessons(List<Lesson> lessons);

    // Lesson Question queries
    @Query("SELECT * FROM lesson_questions WHERE lessonId = :lessonId ORDER BY orderInLesson ASC")
    LiveData<List<LessonQuestion>> getQuestionsForLesson(int lessonId);

    @Query("SELECT * FROM lesson_questions WHERE lessonId = :lessonId ORDER BY orderInLesson ASC")
    List<LessonQuestion> getQuestionsForLessonSync(int lessonId);

    @Query("SELECT * FROM lesson_questions WHERE id = :questionId")
    LiveData<LessonQuestion> getQuestionById(int questionId);

    @Insert
    void insertQuestion(LessonQuestion question);

    @Insert
    void insertAllQuestions(List<LessonQuestion> questions);
}
