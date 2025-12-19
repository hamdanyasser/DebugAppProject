package com.example.debugappproject.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.debugappproject.model.MentalProfile;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    MENTAL PROFILE DATA ACCESS                                ║
 * ║               Room DAO for Mental Evolution System                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Dao
public interface MentalProfileDao {

    // ═══════════════════════════════════════════════════════════════════════════
    //                         BASIC OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    @Query("SELECT * FROM mental_profile WHERE id = 1 LIMIT 1")
    LiveData<MentalProfile> getProfile();

    @Query("SELECT * FROM mental_profile WHERE id = 1 LIMIT 1")
    MentalProfile getProfileSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(MentalProfile profile);

    @Update
    void update(MentalProfile profile);

    // ═══════════════════════════════════════════════════════════════════════════
    //                         SKILL QUERIES
    // ═══════════════════════════════════════════════════════════════════════════

    @Query("SELECT patternRecognition FROM mental_profile WHERE id = 1")
    int getPatternRecognition();

    @Query("SELECT errorIntuition FROM mental_profile WHERE id = 1")
    int getErrorIntuition();

    @Query("SELECT logicFlow FROM mental_profile WHERE id = 1")
    int getLogicFlow();

    @Query("SELECT speedDebugging FROM mental_profile WHERE id = 1")
    int getSpeedDebugging();

    @Query("SELECT complexityTolerance FROM mental_profile WHERE id = 1")
    int getComplexityTolerance();

    @Query("SELECT focusEndurance FROM mental_profile WHERE id = 1")
    int getFocusEndurance();

    @Query("SELECT riskAssessment FROM mental_profile WHERE id = 1")
    int getRiskAssessment();

    @Query("SELECT codeMemory FROM mental_profile WHERE id = 1")
    int getCodeMemory();

    // ═══════════════════════════════════════════════════════════════════════════
    //                         SPECIALTY SKILL QUERIES
    // ═══════════════════════════════════════════════════════════════════════════

    @Query("SELECT nullHunter FROM mental_profile WHERE id = 1")
    int getNullHunter();

    @Query("SELECT loopMaster FROM mental_profile WHERE id = 1")
    int getLoopMaster();

    @Query("SELECT typeWrangler FROM mental_profile WHERE id = 1")
    int getTypeWrangler();

    @Query("SELECT boundaryExpert FROM mental_profile WHERE id = 1")
    int getBoundaryExpert();

    @Query("SELECT concurrencySage FROM mental_profile WHERE id = 1")
    int getConcurrencySage();

    @Query("SELECT memoryArchitect FROM mental_profile WHERE id = 1")
    int getMemoryArchitect();

    // ═══════════════════════════════════════════════════════════════════════════
    //                         ELO & RANKED QUERIES
    // ═══════════════════════════════════════════════════════════════════════════

    @Query("SELECT eloRating FROM mental_profile WHERE id = 1")
    int getEloRating();

    @Query("SELECT peakElo FROM mental_profile WHERE id = 1")
    int getPeakElo();

    @Query("SELECT rankedTier FROM mental_profile WHERE id = 1")
    int getRankedTier();

    @Query("SELECT rankedPoints FROM mental_profile WHERE id = 1")
    int getRankedPoints();

    @Query("SELECT battleWins FROM mental_profile WHERE id = 1")
    int getBattleWins();

    @Query("SELECT battlesPlayed FROM mental_profile WHERE id = 1")
    int getBattlesPlayed();

    @Query("SELECT battleWinStreak FROM mental_profile WHERE id = 1")
    int getBattleWinStreak();

    // ═══════════════════════════════════════════════════════════════════════════
    //                         UPDATE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    @Query("UPDATE mental_profile SET eloRating = :elo WHERE id = 1")
    void updateEloRating(int elo);

    @Query("UPDATE mental_profile SET peakElo = :peakElo WHERE id = 1")
    void updatePeakElo(int peakElo);

    @Query("UPDATE mental_profile SET rankedTier = :tier, rankedPoints = :points WHERE id = 1")
    void updateRankedStatus(int tier, int points);

    @Query("UPDATE mental_profile SET battleWins = battleWins + 1, battlesPlayed = battlesPlayed + 1, battleWinStreak = battleWinStreak + 1 WHERE id = 1")
    void recordBattleWin();

    @Query("UPDATE mental_profile SET battlesPlayed = battlesPlayed + 1, battleWinStreak = 0 WHERE id = 1")
    void recordBattleLoss();

    @Query("UPDATE mental_profile SET bugsSolved = bugsSolved + 1 WHERE id = 1")
    void incrementBugsSolved();

    @Query("UPDATE mental_profile SET perfectSolves = perfectSolves + 1 WHERE id = 1")
    void incrementPerfectSolves();

    @Query("UPDATE mental_profile SET nearMisses = nearMisses + 1 WHERE id = 1")
    void incrementNearMisses();

    @Query("UPDATE mental_profile SET totalXp = totalXp + :xp WHERE id = 1")
    void addXp(int xp);

    @Query("UPDATE mental_profile SET lastActivityTimestamp = :timestamp WHERE id = 1")
    void updateLastActivity(long timestamp);

    // ═══════════════════════════════════════════════════════════════════════════
    //                         SKILL UPDATES
    // ═══════════════════════════════════════════════════════════════════════════

    @Query("UPDATE mental_profile SET patternRecognition = MIN(1000, patternRecognition + :gain) WHERE id = 1")
    void improvePatternRecognition(int gain);

    @Query("UPDATE mental_profile SET errorIntuition = MIN(1000, errorIntuition + :gain) WHERE id = 1")
    void improveErrorIntuition(int gain);

    @Query("UPDATE mental_profile SET logicFlow = MIN(1000, logicFlow + :gain) WHERE id = 1")
    void improveLogicFlow(int gain);

    @Query("UPDATE mental_profile SET speedDebugging = MIN(1000, speedDebugging + :gain) WHERE id = 1")
    void improveSpeedDebugging(int gain);

    @Query("UPDATE mental_profile SET complexityTolerance = MIN(1000, complexityTolerance + :gain) WHERE id = 1")
    void improveComplexityTolerance(int gain);

    @Query("UPDATE mental_profile SET focusEndurance = MIN(1000, focusEndurance + :gain) WHERE id = 1")
    void improveFocusEndurance(int gain);

    @Query("UPDATE mental_profile SET riskAssessment = MIN(1000, riskAssessment + :gain) WHERE id = 1")
    void improveRiskAssessment(int gain);

    @Query("UPDATE mental_profile SET codeMemory = MIN(1000, codeMemory + :gain) WHERE id = 1")
    void improveCodeMemory(int gain);

    @Query("UPDATE mental_profile SET nullHunter = MIN(1000, nullHunter + :gain) WHERE id = 1")
    void improveNullHunter(int gain);

    @Query("UPDATE mental_profile SET loopMaster = MIN(1000, loopMaster + :gain) WHERE id = 1")
    void improveLoopMaster(int gain);

    @Query("UPDATE mental_profile SET typeWrangler = MIN(1000, typeWrangler + :gain) WHERE id = 1")
    void improveTypeWrangler(int gain);

    @Query("UPDATE mental_profile SET boundaryExpert = MIN(1000, boundaryExpert + :gain) WHERE id = 1")
    void improveBoundaryExpert(int gain);

    @Query("UPDATE mental_profile SET concurrencySage = MIN(1000, concurrencySage + :gain) WHERE id = 1")
    void improveConcurrencySage(int gain);

    @Query("UPDATE mental_profile SET memoryArchitect = MIN(1000, memoryArchitect + :gain) WHERE id = 1")
    void improveMemoryArchitect(int gain);

    // ═══════════════════════════════════════════════════════════════════════════
    //                         INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════

    @Query("SELECT COUNT(*) FROM mental_profile WHERE id = 1")
    int hasProfile();
}
