package com.example.debugappproject.game;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    RANKED BATTLE SYSTEM                                      ║
 * ║         Brutal, Fair, Addictive Competitive Experience                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * This system creates:
 * - Ladder anxiety (fear of losing rank)
 * - Progression satisfaction (climbing feels earned)
 * - Fair matchmaking (skill-based, not time-based)
 * - Seasonal resets (keeps everyone engaged)
 *
 * Rank Tiers:
 * - Unranked: 0-9 placement matches
 * - Bronze: 1000-1199 Elo
 * - Silver: 1200-1399 Elo
 * - Gold: 1400-1599 Elo
 * - Diamond: 1600-1799 Elo
 * - Master: 1800-1999 Elo
 * - Legend: 2000+ Elo (top 0.1%)
 */
public class RankedBattleSystem {

    private static final String TAG = "RankedBattleSystem";

    // ═══════════════════════════════════════════════════════════════════════════
    //                         RANK DEFINITIONS
    // ═══════════════════════════════════════════════════════════════════════════

    public enum RankTier {
        UNRANKED(0, 0, 0, "Unranked", "⚪", 0x808080),
        BRONZE(1, 1000, 1199, "Bronze", "🥉", 0xCD7F32),
        SILVER(2, 1200, 1399, "Silver", "🥈", 0xC0C0C0),
        GOLD(3, 1400, 1599, "Gold", "🥇", 0xFFD700),
        DIAMOND(4, 1600, 1799, "Diamond", "💎", 0xB9F2FF),
        MASTER(5, 1800, 1999, "Master", "👑", 0x9400D3),
        LEGEND(6, 2000, 9999, "Legend", "🏆", 0xFF4500);

        public final int tier;
        public final int minElo;
        public final int maxElo;
        public final String name;
        public final String emoji;
        public final int color;

        RankTier(int tier, int minElo, int maxElo, String name, String emoji, int color) {
            this.tier = tier;
            this.minElo = minElo;
            this.maxElo = maxElo;
            this.name = name;
            this.emoji = emoji;
            this.color = color;
        }

        public static RankTier fromElo(int elo) {
            if (elo >= 2000) return LEGEND;
            if (elo >= 1800) return MASTER;
            if (elo >= 1600) return DIAMOND;
            if (elo >= 1400) return GOLD;
            if (elo >= 1200) return SILVER;
            if (elo >= 1000) return BRONZE;
            return UNRANKED;
        }

        public static RankTier fromTier(int tier) {
            for (RankTier rank : values()) {
                if (rank.tier == tier) return rank;
            }
            return UNRANKED;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         PLACEMENT MATCHES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Calculate initial Elo after placement matches
     *
     * @param wins Number of wins in placement
     * @param losses Number of losses in placement
     * @param avgOpponentElo Average Elo of opponents faced
     * @return Starting Elo rating
     */
    public static int calculatePlacementElo(int wins, int losses, int avgOpponentElo) {
        // Base calculation
        int baseElo = 1000;

        // Win bonus: +50 per win
        int winBonus = wins * 50;

        // Loss penalty: -25 per loss
        int lossPenalty = losses * 25;

        // Opponent strength adjustment
        int opponentAdjustment = (avgOpponentElo - 1200) / 4;

        int finalElo = baseElo + winBonus - lossPenalty + opponentAdjustment;

        // Clamp between Bronze floor and Gold ceiling for placement
        return Math.max(1000, Math.min(1599, finalElo));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         ELO CALCULATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Calculate Elo change for a match result
     *
     * @param playerElo Current player Elo
     * @param opponentElo Opponent's Elo
     * @param won Whether player won
     * @param isPerfectWin Won without using hints (bonus)
     * @param timeAdvantage Time advantage percentage (0-1)
     * @return Elo change (positive or negative)
     */
    public static EloChangeResult calculateEloChange(int playerElo, int opponentElo,
                                                      boolean won, boolean isPerfectWin,
                                                      float timeAdvantage) {
        // Dynamic K-factor based on rating
        int kFactor = getKFactor(playerElo);

        // Expected score (0.0 to 1.0)
        double expected = 1.0 / (1.0 + Math.pow(10, (opponentElo - playerElo) / 400.0));

        // Actual score
        double actual = won ? 1.0 : 0.0;

        // Base Elo change
        int baseChange = (int) Math.round(kFactor * (actual - expected));

        // Bonuses for impressive wins
        int bonusElo = 0;
        String bonusReason = null;

        if (won) {
            // Perfect win bonus (no hints used)
            if (isPerfectWin) {
                bonusElo += 5;
                bonusReason = "Perfect win!";
            }

            // Speed bonus (solved significantly faster)
            if (timeAdvantage > 0.3f) {
                bonusElo += (int) (timeAdvantage * 10);
                bonusReason = bonusReason != null ?
                        bonusReason + " Speed bonus!" : "Speed bonus!";
            }

            // Upset bonus (beat higher rated player)
            int eloDiff = opponentElo - playerElo;
            if (eloDiff > 100) {
                int upsetBonus = Math.min(eloDiff / 50, 10);
                bonusElo += upsetBonus;
                bonusReason = bonusReason != null ?
                        bonusReason + " Upset!" : "Upset victory!";
            }
        }

        int totalChange = baseChange + bonusElo;

        // Minimum change (always feel something)
        if (won && totalChange < 5) totalChange = 5;
        if (!won && totalChange > -5) totalChange = -5;

        return new EloChangeResult(
                totalChange,
                baseChange,
                bonusElo,
                bonusReason,
                expected,
                kFactor
        );
    }

    /**
     * Get K-factor based on rating (higher = more volatile)
     */
    private static int getKFactor(int elo) {
        if (elo < 1200) return 40;  // New players - fast adjustment
        if (elo < 1400) return 32;  // Low rated - moderate
        if (elo < 1600) return 28;  // Mid rated
        if (elo < 1800) return 24;  // High rated
        return 20;                   // Master+ - stable
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         RANK POINTS & TIERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Calculate rank points within tier (0-100)
     * Used for showing progress within a rank
     */
    public static int calculateRankPoints(int elo) {
        RankTier tier = RankTier.fromElo(elo);
        if (tier == RankTier.UNRANKED) return 0;
        if (tier == RankTier.LEGEND) {
            // Legend has no cap, show excess over 2000
            return Math.min(100, (elo - 2000) / 2);
        }

        int range = tier.maxElo - tier.minElo + 1;
        int progress = elo - tier.minElo;
        return (progress * 100) / range;
    }

    /**
     * Check if player is at risk of demotion
     */
    public static boolean isAtDemotionRisk(int elo) {
        RankTier tier = RankTier.fromElo(elo);
        return elo - tier.minElo < 25; // Within 25 Elo of tier floor
    }

    /**
     * Check if player is close to promotion
     */
    public static boolean isNearPromotion(int elo) {
        RankTier tier = RankTier.fromElo(elo);
        if (tier == RankTier.LEGEND) return false;
        return tier.maxElo - elo < 25; // Within 25 Elo of next tier
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         MATCHMAKING
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get acceptable Elo range for matchmaking
     * Expands over time if no match found
     *
     * @param playerElo Player's current Elo
     * @param waitTimeSeconds How long player has been waiting
     * @return Min and max acceptable opponent Elo
     */
    public static int[] getMatchmakingRange(int playerElo, int waitTimeSeconds) {
        // Base range: ±100 Elo
        int baseRange = 100;

        // Expand by 25 Elo every 10 seconds of waiting
        int expansion = (waitTimeSeconds / 10) * 25;

        // Cap expansion at ±300
        int maxExpansion = 300;
        int totalRange = Math.min(baseRange + expansion, baseRange + maxExpansion);

        return new int[] {
                Math.max(0, playerElo - totalRange),
                playerElo + totalRange
        };
    }

    /**
     * Calculate match quality score (0-100)
     * Higher = better matched opponents
     */
    public static int calculateMatchQuality(int player1Elo, int player2Elo) {
        int diff = Math.abs(player1Elo - player2Elo);
        if (diff <= 50) return 100;
        if (diff <= 100) return 90;
        if (diff <= 150) return 75;
        if (diff <= 200) return 60;
        if (diff <= 300) return 40;
        return 20;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         SEASONAL SYSTEM
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Calculate soft reset Elo for new season
     * Formula: NewElo = (OldElo + 1200) / 2
     * This compresses everyone toward the middle
     */
    public static int calculateSeasonResetElo(int currentElo) {
        return (currentElo + 1200) / 2;
    }

    /**
     * Get season rewards based on peak rank achieved
     */
    public static SeasonReward getSeasonRewards(RankTier peakRank, int gamesPlayed) {
        int xpReward = 0;
        int gemsReward = 0;
        String title = null;
        String badge = null;

        switch (peakRank) {
            case LEGEND:
                xpReward = 5000;
                gemsReward = 500;
                title = "Season Legend";
                badge = "legend_s1";
                break;
            case MASTER:
                xpReward = 3000;
                gemsReward = 300;
                title = "Season Master";
                badge = "master_s1";
                break;
            case DIAMOND:
                xpReward = 2000;
                gemsReward = 200;
                badge = "diamond_s1";
                break;
            case GOLD:
                xpReward = 1000;
                gemsReward = 100;
                badge = "gold_s1";
                break;
            case SILVER:
                xpReward = 500;
                gemsReward = 50;
                break;
            case BRONZE:
                xpReward = 250;
                gemsReward = 25;
                break;
            default:
                xpReward = 100;
                break;
        }

        // Games played bonus
        xpReward += Math.min(gamesPlayed * 10, 1000);

        return new SeasonReward(xpReward, gemsReward, title, badge, peakRank);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         WIN/LOSS STREAKS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get streak bonus/penalty multiplier
     */
    public static float getStreakMultiplier(int winStreak, int lossStreak) {
        if (winStreak >= 5) return 1.3f;  // Hot streak bonus
        if (winStreak >= 3) return 1.15f;  // Warm streak
        if (lossStreak >= 5) return 0.8f;  // Loss protection
        if (lossStreak >= 3) return 0.9f;  // Slight protection
        return 1.0f;
    }

    /**
     * Generate streak message
     */
    public static String getStreakMessage(int winStreak, int lossStreak) {
        if (winStreak >= 5) return "🔥 ON FIRE! " + winStreak + " WIN STREAK!";
        if (winStreak >= 3) return "⚡ " + winStreak + " wins in a row!";
        if (lossStreak >= 3) return "💪 Don't give up! Break the streak!";
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         RESULT CLASSES
    // ═══════════════════════════════════════════════════════════════════════════

    public static class EloChangeResult {
        public final int totalChange;
        public final int baseChange;
        public final int bonusChange;
        public final String bonusReason;
        public final double expectedWinRate;
        public final int kFactor;

        public EloChangeResult(int totalChange, int baseChange, int bonusChange,
                               String bonusReason, double expectedWinRate, int kFactor) {
            this.totalChange = totalChange;
            this.baseChange = baseChange;
            this.bonusChange = bonusChange;
            this.bonusReason = bonusReason;
            this.expectedWinRate = expectedWinRate;
            this.kFactor = kFactor;
        }

        public String getChangeText() {
            return totalChange >= 0 ? "+" + totalChange : String.valueOf(totalChange);
        }
    }

    public static class SeasonReward {
        public final int xpReward;
        public final int gemsReward;
        public final String title;        // null if no title earned
        public final String badge;        // null if no badge earned
        public final RankTier peakRank;

        public SeasonReward(int xpReward, int gemsReward, String title,
                            String badge, RankTier peakRank) {
            this.xpReward = xpReward;
            this.gemsReward = gemsReward;
            this.title = title;
            this.badge = badge;
            this.peakRank = peakRank;
        }
    }

    public static class MatchResult {
        public final int playerElo;
        public final int opponentElo;
        public final int eloChange;
        public final RankTier oldRank;
        public final RankTier newRank;
        public final boolean promoted;
        public final boolean demoted;
        public final int newRankPoints;
        public final String message;

        public MatchResult(int playerElo, int opponentElo, int eloChange,
                           RankTier oldRank, RankTier newRank, int newRankPoints,
                           String message) {
            this.playerElo = playerElo;
            this.opponentElo = opponentElo;
            this.eloChange = eloChange;
            this.oldRank = oldRank;
            this.newRank = newRank;
            this.promoted = newRank.tier > oldRank.tier;
            this.demoted = newRank.tier < oldRank.tier;
            this.newRankPoints = newRankPoints;
            this.message = message;
        }
    }
}
