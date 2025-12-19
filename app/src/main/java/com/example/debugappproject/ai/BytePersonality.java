package com.example.debugappproject.ai;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    BYTE - AI MENTOR PERSONALITY                              ║
 * ║       Not a Chatbot. A Coach. A Rival. Sometimes an Antagonist.              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * BYTE is the mascot AI that:
 * - Has a PERSONALITY (not just information)
 * - REMEMBERS player mistakes and calls them out
 * - ADAPTS tone based on player performance
 * - TEASES when player is overconfident
 * - ENCOURAGES when player is struggling
 * - CHALLENGES assumptions
 * - Never dumps explanations - only guides thinking
 *
 * Personality Modes:
 * - SUPPORTIVE: Player struggling, needs encouragement
 * - NEUTRAL: Normal teaching mode
 * - CHALLENGING: Player doing well, push harder
 * - TEASING: Player made same mistake twice, call it out
 * - RIVAL: Competitive mode in battles
 */
public class BytePersonality {

    private static final String TAG = "BytePersonality";
    private static final String PREFS_NAME = "byte_personality";

    // ═══════════════════════════════════════════════════════════════════════════
    //                         PERSONALITY MODES
    // ═══════════════════════════════════════════════════════════════════════════

    public enum Mood {
        SUPPORTIVE,     // Be encouraging
        NEUTRAL,        // Standard teaching
        CHALLENGING,    // Push harder
        TEASING,        // Light trash talk
        RIVAL,          // Competitive mode
        IMPRESSED       // Genuine admiration
    }

    // Memory of player behaviors
    private int consecutiveFails = 0;
    private int consecutiveWins = 0;
    private int nullPointerMistakes = 0;
    private int offByOneMistakes = 0;
    private int sameErrorCount = 0;
    private String lastErrorType = "";
    private int hintUsageTotal = 0;
    private int perfectSolves = 0;
    private boolean usedHintThisSession = false;

    private final SharedPreferences prefs;
    private final Random random = new Random();

    // ═══════════════════════════════════════════════════════════════════════════
    //                         INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════

    public BytePersonality(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadMemory();
    }

    private void loadMemory() {
        nullPointerMistakes = prefs.getInt("null_mistakes", 0);
        offByOneMistakes = prefs.getInt("off_by_one", 0);
        hintUsageTotal = prefs.getInt("hints_used", 0);
        perfectSolves = prefs.getInt("perfect_solves", 0);
    }

    private void saveMemory() {
        prefs.edit()
                .putInt("null_mistakes", nullPointerMistakes)
                .putInt("off_by_one", offByOneMistakes)
                .putInt("hints_used", hintUsageTotal)
                .putInt("perfect_solves", perfectSolves)
                .apply();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         MOOD CALCULATION
    // ═══════════════════════════════════════════════════════════════════════════

    public Mood getCurrentMood() {
        // Too many fails = supportive
        if (consecutiveFails >= 3) return Mood.SUPPORTIVE;

        // Made same mistake = teasing
        if (sameErrorCount >= 2) return Mood.TEASING;

        // On a roll = challenging
        if (consecutiveWins >= 3) return Mood.CHALLENGING;

        // Hot streak = impressed
        if (consecutiveWins >= 5) return Mood.IMPRESSED;

        // In a battle = rival
        // (set externally)

        return Mood.NEUTRAL;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         GREETING MESSAGES
    // ═══════════════════════════════════════════════════════════════════════════

    public String getGreeting() {
        Mood mood = getCurrentMood();

        switch (mood) {
            case SUPPORTIVE:
                return pickRandom(SUPPORTIVE_GREETINGS);
            case CHALLENGING:
                return pickRandom(CHALLENGING_GREETINGS);
            case TEASING:
                return pickRandom(TEASING_GREETINGS);
            case IMPRESSED:
                return pickRandom(IMPRESSED_GREETINGS);
            default:
                return pickRandom(NEUTRAL_GREETINGS);
        }
    }

    private static final String[] NEUTRAL_GREETINGS = {
            "Ready to debug?",
            "Another bug awaits.",
            "Let's hunt some bugs.",
            "Show me what you've got.",
            "Time to code."
    };

    private static final String[] SUPPORTIVE_GREETINGS = {
            "Don't worry, you'll get this one.",
            "Fresh start. Fresh bug. You got this.",
            "Every bug you squash makes you stronger.",
            "Learning is supposed to feel hard sometimes.",
            "I believe in you. Let's do this."
    };

    private static final String[] CHALLENGING_GREETINGS = {
            "Too easy for you? Let's fix that.",
            "Don't get comfortable. This one's tricky.",
            "Your streak won't protect you here.",
            "Feeling confident? Good. You'll need it.",
            "Let's see if you can keep this up."
    };

    private static final String[] TEASING_GREETINGS = {
            "Oh, it's you again. Remember last time?",
            "Back for more? Bold move.",
            "Let me guess - null pointer won't get you this time?",
            "I hope you learned from yesterday...",
            "Same mistakes await. Will you repeat them?"
    };

    private static final String[] IMPRESSED_GREETINGS = {
            "Okay, I'm impressed. Genuinely.",
            "You're actually good at this.",
            "Not bad, coder. Not bad at all.",
            "I might have to start trying harder.",
            "You're making me look bad here."
    };

    // ═══════════════════════════════════════════════════════════════════════════
    //                         HINT COMMENTARY
    // ═══════════════════════════════════════════════════════════════════════════

    public String getHintReaction(int hintLevel) {
        usedHintThisSession = true;
        hintUsageTotal++;
        saveMemory();

        if (consecutiveWins >= 3) {
            // Was doing well, now needs hint - tease
            return pickRandom(new String[]{
                    "And there goes your streak of independence.",
                    "Even the best need help sometimes... I guess.",
                    "Hmm, not so confident now?",
                    "This one got you, huh?"
            });
        }

        switch (hintLevel) {
            case 1:
                return pickRandom(new String[]{
                        "Just a nudge. You'll figure it out.",
                        "Here's a thought...",
                        "Consider this...",
                        "Something to think about:"
                });
            case 2:
                return pickRandom(new String[]{
                        "Getting warmer? Here's more.",
                        "Still stuck? Okay, more specifically...",
                        "Let me be clearer...",
                        "Focus on this area:"
                });
            case 3:
                return pickRandom(new String[]{
                        "Alright, I'll basically tell you.",
                        "Last hint. After this, it's the answer.",
                        "Fine, here's the big clue:",
                        "This should make it obvious:"
                });
            default:
                return "Need a hint?";
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         SUCCESS REACTIONS
    // ═══════════════════════════════════════════════════════════════════════════

    public String getSuccessReaction(boolean perfect, int timeSeconds, String difficulty) {
        consecutiveWins++;
        consecutiveFails = 0;

        if (perfect) {
            perfectSolves++;
            saveMemory();

            if (timeSeconds < 30) {
                return pickRandom(new String[]{
                        "WHAT. That was insane.",
                        "Speedrun mode activated?!",
                        "I barely had time to blink.",
                        "Okay, show-off. Well done.",
                        "Are you even human?"
                });
            }

            return pickRandom(new String[]{
                    "Perfect. No hints. Clean solve.",
                    "That's how it's done.",
                    "Flawless execution.",
                    "You didn't even need me.",
                    "Textbook debugging right there."
            });
        }

        if (usedHintThisSession) {
            return pickRandom(new String[]{
                    "Got there eventually!",
                    "Hints helped, but you still solved it.",
                    "A win is a win.",
                    "Next time, try without hints?",
                    "Mission accomplished. With assistance."
            });
        }

        if (difficulty.equalsIgnoreCase("hard") || difficulty.equalsIgnoreCase("expert")) {
            return pickRandom(new String[]{
                    "Impressive. That was a tough one.",
                    "You handled that complexity well.",
                    "Not many people solve that.",
                    "Your skills are showing.",
                    "Hard-earned victory."
            });
        }

        return pickRandom(new String[]{
                "Nice work!",
                "Bug squashed.",
                "Clean solve.",
                "On to the next one?",
                "Another one bites the dust."
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         FAILURE REACTIONS
    // ═══════════════════════════════════════════════════════════════════════════

    public String getFailureReaction(String errorType, boolean isNearMiss) {
        consecutiveFails++;
        consecutiveWins = 0;

        // Check for repeated mistakes
        if (errorType.equals(lastErrorType)) {
            sameErrorCount++;
        } else {
            sameErrorCount = 1;
            lastErrorType = errorType;
        }

        // Track specific error types
        if (errorType.contains("null") || errorType.contains("NullPointer")) {
            nullPointerMistakes++;
            saveMemory();

            if (nullPointerMistakes >= 5) {
                return pickRandom(new String[]{
                        "Null again? We need to talk about this.",
                        "You and null pointers have a history.",
                        "I've lost count of your null errors.",
                        "Maybe always check for null first?",
                        "Null is not your friend. Remember that."
                });
            }
        }

        if (errorType.contains("index") || errorType.contains("bound") || errorType.contains("off-by-one")) {
            offByOneMistakes++;
            saveMemory();

            if (offByOneMistakes >= 5) {
                return pickRandom(new String[]{
                        "Off-by-one again. Classic.",
                        "Arrays start at 0. Still.",
                        "Check your loop bounds...",
                        "Your old nemesis: the array index.",
                        "Boundary bugs love you."
                });
            }
        }

        // Near miss reactions
        if (isNearMiss) {
            return pickRandom(new String[]{
                    "So close! That's painful.",
                    "Almost had it. Almost.",
                    "You can see the answer, can't you?",
                    "One more look. You're right there.",
                    "That was 95% correct. What's missing?"
            });
        }

        // Repeated failures
        if (consecutiveFails >= 3) {
            return pickRandom(new String[]{
                    "Hey, take a breath. It's okay.",
                    "This one's tricky. Want a hint?",
                    "Don't force it. Think methodically.",
                    "Sometimes stepping back helps.",
                    "You'll get it. Just... not yet."
            });
        }

        // Same mistake multiple times
        if (sameErrorCount >= 2) {
            return pickRandom(new String[]{
                    "Same mistake twice. Think about why.",
                    "You did this exact thing before...",
                    "Déjà vu? It should be.",
                    "Pattern detected: you keep doing this.",
                    "Learn from the error, not just the fix."
            });
        }

        return pickRandom(new String[]{
                "Not quite. Try again.",
                "Bug's still there. Keep looking.",
                "Nope. But you're thinking.",
                "Wrong path. Backtrack a bit.",
                "Not the fix. What else could it be?"
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         BATTLE MODE
    // ═══════════════════════════════════════════════════════════════════════════

    public String getBattleStart(String opponentName, int opponentElo, int playerElo) {
        int eloDiff = opponentElo - playerElo;

        if (eloDiff > 200) {
            return pickRandom(new String[]{
                    "Underdog status: activated.",
                    opponentName + " is highly ranked. Prove them wrong.",
                    "They think they'll win easily. Surprise them.",
                    "High Elo doesn't guarantee victory."
            });
        } else if (eloDiff < -200) {
            return pickRandom(new String[]{
                    "You're the favorite here. Don't choke.",
                    "Lower ranked opponent. No excuses.",
                    "Should be easy... right?",
                    "Don't underestimate them."
            });
        }

        return pickRandom(new String[]{
                "Fair matchup. May the best debugger win.",
                "Even match. This comes down to skill.",
                "Similar ratings. This will be close.",
                "Prove you deserve your rank."
        });
    }

    public String getBattleWin(boolean wasUnderdog, int eloDiff) {
        if (wasUnderdog && eloDiff > 200) {
            return pickRandom(new String[]{
                    "UPSET! They never saw it coming!",
                    "Underdog victory! Beautiful!",
                    "That's how you climb the ladder!",
                    "Elo is just a number. Skill is real."
            });
        }

        return pickRandom(new String[]{
                "Victory! Your Elo rises.",
                "Dominant performance.",
                "They couldn't keep up.",
                "Clean win. Well played."
        });
    }

    public String getBattleLoss(boolean wasUnderdog, int eloDiff) {
        if (wasUnderdog) {
            return pickRandom(new String[]{
                    "Expected loss, but still hurts.",
                    "Learn from them. They're higher ranked for a reason.",
                    "Losing to better players teaches you.",
                    "Minimal Elo lost. Good effort."
            });
        }

        return pickRandom(new String[]{
                "That one stings.",
                "They outplayed you. Study what they did.",
                "Elo down. Time to climb back.",
                "Loss happens. Recovery matters."
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         THINKING PROMPTS (Never Give Answers)
    // ═══════════════════════════════════════════════════════════════════════════

    public String getThinkingPrompt(String bugCategory) {
        switch (bugCategory.toLowerCase()) {
            case "null":
            case "nullpointer":
                return pickRandom(new String[]{
                        "What could be null here?",
                        "Trace back: where does this value come from?",
                        "Is everything initialized?",
                        "Check the object lifecycle."
                });

            case "loop":
            case "iteration":
                return pickRandom(new String[]{
                        "Walk through the loop manually.",
                        "What happens on the first iteration? Last?",
                        "Is the condition ever false?",
                        "Count the iterations by hand."
                });

            case "array":
            case "index":
                return pickRandom(new String[]{
                        "What's the valid index range?",
                        "Arrays are zero-indexed. Always.",
                        "Where does the index come from?",
                        "Check the boundary conditions."
                });

            case "logic":
            case "conditional":
                return pickRandom(new String[]{
                        "Trace each branch. Where does it go wrong?",
                        "Are the conditions mutually exclusive?",
                        "What's the edge case?",
                        "Draw out the truth table."
                });

            default:
                return pickRandom(new String[]{
                        "Read the code like you've never seen it.",
                        "What does the error actually tell you?",
                        "Don't guess. Understand.",
                        "Follow the data flow."
                });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         UTILITY
    // ═══════════════════════════════════════════════════════════════════════════

    private String pickRandom(String[] options) {
        return options[random.nextInt(options.length)];
    }

    public void resetSession() {
        usedHintThisSession = false;
        sameErrorCount = 0;
        lastErrorType = "";
    }

    public void resetStreak() {
        consecutiveWins = 0;
        consecutiveFails = 0;
    }

    // Stats for display
    public int getNullPointerMistakes() { return nullPointerMistakes; }
    public int getOffByOneMistakes() { return offByOneMistakes; }
    public int getHintUsageTotal() { return hintUsageTotal; }
    public int getPerfectSolves() { return perfectSolves; }
}
