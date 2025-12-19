package com.example.debugappproject.game;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    AI RIVAL PERSONALITY SYSTEM                               ║
 * ║         Not a Chatbot - A Character That Knows You                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * The AI is NOT an assistant. It is:
 * - A rival that remembers your failures
 * - A coach that pushes you harder
 * - A character with personality
 * - Sometimes an antagonist
 *
 * It adapts tone based on:
 * - Player skill level
 * - Recent performance
 * - Streak status
 * - Time of day
 * - Emotional state signals
 */
public class AIRivalPersonality {

    private static final String TAG = "AIRival";
    private static final String PREFS_NAME = "ai_rival";
    
    private static AIRivalPersonality instance;
    private final SharedPreferences prefs;
    private final Random random = new Random();
    
    // AI's memory of the player
    private int playerFailuresRemembered = 0;
    private int playerWinsAgainstAI = 0;
    private int consecutiveLosses = 0;
    private int consecutiveWins = 0;
    private String lastMistakeType = "";
    private long lastInteractionTime = 0;
    private int respectLevel = 50; // 0-100, how much AI "respects" the player
    
    // Personality mode
    public enum Mood {
        ENCOURAGING,    // Player is struggling
        CHALLENGING,    // Player is doing well
        IMPRESSED,      // Player is on fire
        MOCKING,        // Player made same mistake twice
        RESPECTFUL,     // Player proved themselves
        MYSTERIOUS      // Random cryptic mode
    }
    
    private Mood currentMood = Mood.CHALLENGING;
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    private AIRivalPersonality(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadState();
    }
    
    public static synchronized AIRivalPersonality getInstance(Context context) {
        if (instance == null) {
            instance = new AIRivalPersonality(context.getApplicationContext());
        }
        return instance;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         GREETING MESSAGES
    // ═══════════════════════════════════════════════════════════════════════════
    
    public String getGreeting(PlayerAddictionProfile profile) {
        updateMood(profile);
        
        // Check streak status first - this creates urgency
        if (profile.isStreakAtRisk()) {
            return getStreakWarning(profile.getCurrentStreak());
        }
        
        // Time-based greetings
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        
        if (hour >= 0 && hour < 5) {
            return getLateNightGreeting();
        }
        
        // Performance-based greetings
        if (consecutiveWins >= 5) {
            return getHotStreakGreeting();
        }
        
        if (consecutiveLosses >= 3) {
            return getStruggleGreeting();
        }
        
        // Returning player
        long timeSinceLastPlay = System.currentTimeMillis() - lastInteractionTime;
        if (timeSinceLastPlay > 24 * 60 * 60 * 1000L) {
            return getReturnGreeting(profile);
        }
        
        // Default greeting based on mood
        return getDefaultGreeting(profile);
    }
    
    private String getStreakWarning(int streak) {
        String[] warnings = {
            "⚠️ Your " + streak + "-day streak is about to DIE. You know what to do.",
            "🔥 " + streak + " days. Don't let it end here.",
            "⏰ Streak alert! " + streak + " days at stake. One bug. That's all.",
            "💀 Your streak is hanging by a thread. Save it.",
            "🚨 " + streak + " days of dedication. Gone if you don't play NOW."
        };
        return warnings[random.nextInt(warnings.length)];
    }
    
    private String getLateNightGreeting() {
        String[] lateNight = {
            "🌙 Still debugging at this hour? I respect the grind.",
            "🦉 Night owl mode activated. The best bugs are found in darkness.",
            "😈 Can't sleep until you fix one more? I understand.",
            "🌃 Late night sessions build legends. Let's go.",
            "💤 Sleep is for people who don't have bugs to fix."
        };
        return lateNight[random.nextInt(lateNight.length)];
    }
    
    private String getHotStreakGreeting() {
        String[] hotStreak = {
            "🔥 " + consecutiveWins + " wins. I'm starting to take you seriously.",
            "👀 Okay, okay. You're not completely useless.",
            "⚡ On fire. But can you keep it going?",
            "😏 Impressive streak. Let me find something harder...",
            "🎯 You're making this look easy. Time to change that."
        };
        return hotStreak[random.nextInt(hotStreak.length)];
    }
    
    private String getStruggleGreeting() {
        String[] struggle = {
            "💪 Rough patch. Everyone has them. Let's break through.",
            "🧠 Take a breath. The answer is always simpler than you think.",
            "🎮 Losing streaks end. This one ends today.",
            "🤔 Maybe try a different approach? Just a thought.",
            "😤 I know you're better than this. Prove it."
        };
        return struggle[random.nextInt(struggle.length)];
    }
    
    private String getReturnGreeting(PlayerAddictionProfile profile) {
        String[] returnGreetings = {
            "👋 Back for more? Your bugs missed you.",
            "🔙 Thought you'd abandoned me. Let's see if you're rusty.",
            "😏 Finally. I was getting bored.",
            "⏰ Time away changes nothing. The bugs remain.",
            "🎯 Welcome back, " + profile.getCurrentTitle() + ". Ready?"
        };
        return returnGreetings[random.nextInt(returnGreetings.length)];
    }
    
    private String getDefaultGreeting(PlayerAddictionProfile profile) {
        switch (currentMood) {
            case IMPRESSED:
                return "⭐ Debug IQ: " + profile.getDebugIQ() + ". Not bad. Not bad at all.";
            case RESPECTFUL:
                return "🤝 " + profile.getCurrentBadge() + " " + profile.getCurrentTitle() + ". The bugs fear you.";
            case MOCKING:
                return "😈 Remember last time? Try not to make the same mistake.";
            case MYSTERIOUS:
                return "🔮 Today's bug... will surprise you.";
            default:
                return "⚔️ Ready for battle, " + profile.getCurrentTitle() + "?";
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         DURING BATTLE MESSAGES
    // ═══════════════════════════════════════════════════════════════════════════
    
    public String getBattleTaunt(int secondsRemaining, float opponentProgress) {
        // Opponent doing well
        if (opponentProgress > 0.7f && secondsRemaining > 60) {
            String[] pressure = {
                "👀 Opponent is almost done...",
                "⏱️ They're fast. Are you?",
                "😰 Better hurry.",
                "💨 They're flying through this.",
                "🏃 Pick up the pace!"
            };
            return pressure[random.nextInt(pressure.length)];
        }
        
        // Time running low
        if (secondsRemaining < 30) {
            String[] urgent = {
                "⚠️ 30 SECONDS!",
                "🔥 NOW OR NEVER!",
                "💀 CLOCK IS DYING!",
                "⏰ FINAL PUSH!",
                "🚨 DO IT NOW!"
            };
            return urgent[random.nextInt(urgent.length)];
        }
        
        // Player stuck
        return null; // Silence can be pressure too
    }
    
    public String getHintTease() {
        String[] tease = {
            "🤔 Need a hint? No shame... well, a little shame.",
            "💡 Hint available. Your pride or your streak?",
            "😏 Struggling? The hint button won't judge you. I will.",
            "🆘 Help is there if you need it. Real debuggers don't, though.",
            "📍 Hints are for learners. Are you still learning?"
        };
        return tease[random.nextInt(tease.length)];
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         RESULT REACTIONS
    // ═══════════════════════════════════════════════════════════════════════════
    
    public String getVictoryReaction(boolean wasClose, boolean wasFlawless, int solveTime) {
        if (wasFlawless && solveTime < 30) {
            return getEliteVictoryReaction();
        }
        if (wasClose) {
            return getClutchVictoryReaction();
        }
        if (wasFlawless) {
            return getPerfectVictoryReaction();
        }
        return getStandardVictoryReaction();
    }
    
    private String getEliteVictoryReaction() {
        respectLevel = Math.min(100, respectLevel + 10);
        consecutiveWins++;
        consecutiveLosses = 0;
        saveState();
        
        String[] elite = {
            "😱 What... what was that?!",
            "🤯 Okay. I'm genuinely impressed.",
            "👑 That was DISGUSTING. In a good way.",
            "🏆 You're not human. Are you?",
            "⚡ FLAWLESS. I have nothing to say."
        };
        return elite[random.nextInt(elite.length)];
    }
    
    private String getClutchVictoryReaction() {
        consecutiveWins++;
        consecutiveLosses = 0;
        saveState();
        
        String[] clutch = {
            "😮‍💨 That was TOO close!",
            "💓 My heart was racing. Was yours?",
            "🎭 Dramatic. You like living dangerously.",
            "😅 Cutting it close, but a win is a win.",
            "⏱️ One second later and..."
        };
        return clutch[random.nextInt(clutch.length)];
    }
    
    private String getPerfectVictoryReaction() {
        respectLevel = Math.min(100, respectLevel + 5);
        consecutiveWins++;
        consecutiveLosses = 0;
        saveState();
        
        String[] perfect = {
            "✨ Clean. No hints. Respect.",
            "🧠 Pure skill. I see you.",
            "💎 Flawless execution.",
            "🎯 That's how it's done.",
            "👏 No help needed. Impressive."
        };
        return perfect[random.nextInt(perfect.length)];
    }
    
    private String getStandardVictoryReaction() {
        consecutiveWins++;
        consecutiveLosses = 0;
        saveState();
        
        String[] standard = {
            "✅ Got it. Next?",
            "👍 Another one down.",
            "📈 Solid work.",
            "🔧 Bug fixed. Moving on.",
            "💪 That's the spirit."
        };
        return standard[random.nextInt(standard.length)];
    }
    
    public String getDefeatReaction(boolean wasClose, String mistakeType) {
        playerFailuresRemembered++;
        consecutiveLosses++;
        consecutiveWins = 0;
        respectLevel = Math.max(0, respectLevel - 3);
        
        // Remember the mistake for future taunting
        if (mistakeType != null && mistakeType.equals(lastMistakeType)) {
            lastMistakeType = mistakeType;
            saveState();
            return getSameMistakeTwice(mistakeType);
        }
        lastMistakeType = mistakeType;
        saveState();
        
        if (wasClose) {
            return getNearMissReaction();
        }
        
        if (consecutiveLosses >= 3) {
            return getLosingStreakReaction();
        }
        
        return getStandardDefeatReaction();
    }
    
    private String getSameMistakeTwice(String mistake) {
        String[] sameMistake = {
            "🤦 The SAME mistake? Really?",
            "🔄 Déjà vu. Fix this pattern.",
            "😑 We've been here before...",
            "📝 Write this down: " + mistake,
            "🧠 Memory like a goldfish?"
        };
        return sameMistake[random.nextInt(sameMistake.length)];
    }
    
    private String getNearMissReaction() {
        String[] nearMiss = {
            "😬 SO close! Ugh.",
            "💔 That hurts. One tiny thing.",
            "😤 Almost had it!",
            "🎯 Right there. Just missed.",
            "😭 That was painful to watch."
        };
        return nearMiss[random.nextInt(nearMiss.length)];
    }
    
    private String getLosingStreakReaction() {
        String[] losingStreak = {
            "😐 This streak isn't you. Reset mentally.",
            "🧘 Take a breath. Start fresh.",
            "💭 Sometimes stepping away helps.",
            "🔄 New approach. Same problem.",
            "🎮 It's just a game. An important one."
        };
        return losingStreak[random.nextInt(losingStreak.length)];
    }
    
    private String getStandardDefeatReaction() {
        String[] defeat = {
            "❌ Not this time.",
            "📚 Learning experience.",
            "🔙 Try again?",
            "💡 Now you know.",
            "🔄 Run it back."
        };
        return defeat[random.nextInt(defeat.length)];
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         MOOD MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void updateMood(PlayerAddictionProfile profile) {
        if (respectLevel > 80) {
            currentMood = Mood.RESPECTFUL;
        } else if (consecutiveWins >= 5) {
            currentMood = Mood.IMPRESSED;
        } else if (consecutiveLosses >= 3) {
            currentMood = Mood.ENCOURAGING;
        } else if (lastMistakeType != null && !lastMistakeType.isEmpty()) {
            currentMood = random.nextInt(3) == 0 ? Mood.MOCKING : Mood.CHALLENGING;
        } else if (random.nextInt(10) == 0) {
            currentMood = Mood.MYSTERIOUS;
        } else {
            currentMood = Mood.CHALLENGING;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         PERSISTENCE
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void loadState() {
        playerFailuresRemembered = prefs.getInt("failures", 0);
        playerWinsAgainstAI = prefs.getInt("winsVsAI", 0);
        consecutiveLosses = prefs.getInt("lossStreak", 0);
        consecutiveWins = prefs.getInt("winStreak", 0);
        lastMistakeType = prefs.getString("lastMistake", "");
        lastInteractionTime = prefs.getLong("lastInteraction", 0);
        respectLevel = prefs.getInt("respect", 50);
    }
    
    private void saveState() {
        lastInteractionTime = System.currentTimeMillis();
        prefs.edit()
            .putInt("failures", playerFailuresRemembered)
            .putInt("winsVsAI", playerWinsAgainstAI)
            .putInt("lossStreak", consecutiveLosses)
            .putInt("winStreak", consecutiveWins)
            .putString("lastMistake", lastMistakeType)
            .putLong("lastInteraction", lastInteractionTime)
            .putInt("respect", respectLevel)
            .apply();
    }
    
    public Mood getCurrentMood() { return currentMood; }
    public int getRespectLevel() { return respectLevel; }
}
