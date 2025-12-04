package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           PRO SUBSCRIPTION MANAGER                                           ║
 * ║              Premium Features & Subscription Handling                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Manages Pro subscription status and features:
 * 
 * FREE FEATURES:
 * - Basic debugging challenges (first 30 bugs)
 * - Battle Arena (5 battles/day)
 * - Daily Challenge
 * - Basic achievements
 * - 3 hints per day
 * - Basic leaderboard
 * 
 * PRO FEATURES ($4.99/month or $39.99/year):
 * - All 90+ debugging challenges
 * - Unlimited battles
 * - Algorithm Arena
 * - Code Review Mode
 * - Code Golf challenges
 * - Unlimited hints
 * - Advanced analytics & insights
 * - Custom themes (10+)
 * - No ads
 * - Offline mode
 * - Priority support
 * - Exclusive badges & titles
 * - Certificate generation
 * - Resume/portfolio export
 * - Interview prep mode
 * - Company-specific practice
 * - Early access to new features
 * - Monthly tournaments
 * - Team battles
 * - 2x XP weekends
 */
public class ProManager {
    
    private static final String PREFS_NAME = "pro_subscription";
    private static final String KEY_IS_PRO = "is_pro";
    private static final String KEY_PRO_EXPIRY = "pro_expiry";
    private static final String KEY_SUBSCRIPTION_TYPE = "subscription_type";
    private static final String KEY_DEMO_MODE = "demo_mode";
    private static final String KEY_DEMO_EXPIRY = "demo_expiry";
    
    // Daily limits for free users
    private static final String KEY_BATTLES_TODAY = "battles_today";
    private static final String KEY_HINTS_TODAY = "hints_today";
    private static final String KEY_LAST_RESET_DATE = "last_reset_date";
    
    public static final int FREE_BATTLES_PER_DAY = 5;
    public static final int FREE_HINTS_PER_DAY = 3;
    public static final int FREE_BUG_LIMIT = 30;
    
    private static ProManager instance;
    private final SharedPreferences prefs;
    private final Context context;
    
    public enum SubscriptionType {
        FREE,
        MONTHLY,
        YEARLY,
        LIFETIME
    }
    
    public enum ProFeature {
        UNLIMITED_BUGS("Access all 90+ challenges"),
        UNLIMITED_BATTLES("Unlimited PvP battles"),
        UNLIMITED_HINTS("Unlimited hints"),
        ALGORITHM_ARENA("Algorithm challenges"),
        CODE_REVIEW("Code review mode"),
        CODE_GOLF("Code golf challenges"),
        ADVANCED_ANALYTICS("Performance insights"),
        CUSTOM_THEMES("10+ custom themes"),
        NO_ADS("Ad-free experience"),
        OFFLINE_MODE("Download for offline"),
        PRIORITY_SUPPORT("Priority support"),
        EXCLUSIVE_BADGES("Exclusive badges"),
        CERTIFICATES("Achievement certificates"),
        RESUME_EXPORT("Export to resume"),
        INTERVIEW_PREP("Interview preparation"),
        COMPANY_PRACTICE("Company-specific practice"),
        EARLY_ACCESS("Early feature access"),
        TOURNAMENTS("Monthly tournaments"),
        TEAM_BATTLES("Team battle mode"),
        DOUBLE_XP_WEEKENDS("2x XP weekends");
        
        private final String description;
        
        ProFeature(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private ProManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        checkDailyReset();
    }
    
    public static synchronized ProManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProManager(context);
        }
        return instance;
    }
    
    /**
     * Check if user has Pro subscription (or demo mode).
     */
    public boolean isPro() {
        // Check demo mode first
        if (isDemoMode()) {
            return true;
        }
        
        // Check actual subscription
        boolean isPro = prefs.getBoolean(KEY_IS_PRO, false);
        if (isPro) {
            long expiry = prefs.getLong(KEY_PRO_EXPIRY, 0);
            if (expiry > System.currentTimeMillis()) {
                return true;
            } else {
                // Subscription expired
                prefs.edit().putBoolean(KEY_IS_PRO, false).apply();
                return false;
            }
        }
        return false;
    }
    
    /**
     * Check if demo mode is active.
     */
    public boolean isDemoMode() {
        boolean demo = prefs.getBoolean(KEY_DEMO_MODE, false);
        if (demo) {
            long expiry = prefs.getLong(KEY_DEMO_EXPIRY, 0);
            if (expiry > System.currentTimeMillis()) {
                return true;
            } else {
                prefs.edit().putBoolean(KEY_DEMO_MODE, false).apply();
                return false;
            }
        }
        return false;
    }
    
    /**
     * Enable demo mode for testing (24 hours).
     */
    public void enableDemoMode() {
        long expiry = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 hours
        prefs.edit()
            .putBoolean(KEY_DEMO_MODE, true)
            .putLong(KEY_DEMO_EXPIRY, expiry)
            .apply();
    }
    
    /**
     * Disable demo mode.
     */
    public void disableDemoMode() {
        prefs.edit()
            .putBoolean(KEY_DEMO_MODE, false)
            .putLong(KEY_DEMO_EXPIRY, 0)
            .apply();
    }
    
    /**
     * Get remaining demo time in milliseconds.
     */
    public long getDemoTimeRemaining() {
        if (!isDemoMode()) return 0;
        long expiry = prefs.getLong(KEY_DEMO_EXPIRY, 0);
        return Math.max(0, expiry - System.currentTimeMillis());
    }
    
    /**
     * Activate Pro subscription.
     */
    public void activatePro(SubscriptionType type) {
        long expiry;
        switch (type) {
            case MONTHLY:
                expiry = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
                break;
            case YEARLY:
                expiry = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000);
                break;
            case LIFETIME:
                expiry = Long.MAX_VALUE;
                break;
            default:
                return;
        }
        
        prefs.edit()
            .putBoolean(KEY_IS_PRO, true)
            .putLong(KEY_PRO_EXPIRY, expiry)
            .putString(KEY_SUBSCRIPTION_TYPE, type.name())
            .apply();
    }
    
    /**
     * Cancel Pro subscription.
     */
    public void cancelPro() {
        prefs.edit()
            .putBoolean(KEY_IS_PRO, false)
            .putLong(KEY_PRO_EXPIRY, 0)
            .putString(KEY_SUBSCRIPTION_TYPE, SubscriptionType.FREE.name())
            .apply();
    }
    
    /**
     * Get subscription type.
     */
    public SubscriptionType getSubscriptionType() {
        String type = prefs.getString(KEY_SUBSCRIPTION_TYPE, SubscriptionType.FREE.name());
        try {
            return SubscriptionType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return SubscriptionType.FREE;
        }
    }
    
    /**
     * Get Pro expiry date.
     */
    public long getProExpiry() {
        return prefs.getLong(KEY_PRO_EXPIRY, 0);
    }
    
    /**
     * Check if a specific feature is available.
     */
    public boolean hasFeature(ProFeature feature) {
        return isPro();
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // DAILY LIMITS FOR FREE USERS
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void checkDailyReset() {
        String today = getTodayString();
        String lastReset = prefs.getString(KEY_LAST_RESET_DATE, "");
        
        if (!today.equals(lastReset)) {
            // Reset daily counters
            prefs.edit()
                .putInt(KEY_BATTLES_TODAY, 0)
                .putInt(KEY_HINTS_TODAY, 0)
                .putString(KEY_LAST_RESET_DATE, today)
                .apply();
        }
    }
    
    private String getTodayString() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.DAY_OF_YEAR);
    }
    
    /**
     * Check if user can start a battle (respects daily limit for free users).
     */
    public boolean canStartBattle() {
        if (isPro()) return true;
        int battlesToday = prefs.getInt(KEY_BATTLES_TODAY, 0);
        return battlesToday < FREE_BATTLES_PER_DAY;
    }
    
    /**
     * Get remaining battles for today.
     */
    public int getRemainingBattles() {
        if (isPro()) return Integer.MAX_VALUE;
        int battlesToday = prefs.getInt(KEY_BATTLES_TODAY, 0);
        return Math.max(0, FREE_BATTLES_PER_DAY - battlesToday);
    }
    
    /**
     * Record a battle played.
     */
    public void recordBattlePlayed() {
        if (!isPro()) {
            int battles = prefs.getInt(KEY_BATTLES_TODAY, 0);
            prefs.edit().putInt(KEY_BATTLES_TODAY, battles + 1).apply();
        }
    }
    
    /**
     * Check if user can use a hint.
     */
    public boolean canUseHint() {
        if (isPro()) return true;
        int hintsToday = prefs.getInt(KEY_HINTS_TODAY, 0);
        return hintsToday < FREE_HINTS_PER_DAY;
    }
    
    /**
     * Get remaining hints for today.
     */
    public int getRemainingHints() {
        if (isPro()) return Integer.MAX_VALUE;
        int hintsToday = prefs.getInt(KEY_HINTS_TODAY, 0);
        return Math.max(0, FREE_HINTS_PER_DAY - hintsToday);
    }
    
    /**
     * Record a hint used.
     */
    public void recordHintUsed() {
        if (!isPro()) {
            int hints = prefs.getInt(KEY_HINTS_TODAY, 0);
            prefs.edit().putInt(KEY_HINTS_TODAY, hints + 1).apply();
        }
    }
    
    /**
     * Check if a bug ID is accessible (free users limited to first 30).
     */
    public boolean canAccessBug(int bugId) {
        if (isPro()) return true;
        return bugId <= FREE_BUG_LIMIT;
    }
    
    /**
     * Check if it's a 2x XP weekend (Pro feature).
     */
    public boolean isDoubleXPActive() {
        if (!isPro()) return false;
        
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }
    
    /**
     * Get XP multiplier based on Pro status and day.
     */
    public float getXPMultiplier() {
        if (isDoubleXPActive()) return 2.0f;
        return 1.0f;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PRICING INFO
    // ═══════════════════════════════════════════════════════════════════════════
    
    public static String getMonthlyPrice() {
        return "$4.99/month";
    }
    
    public static String getYearlyPrice() {
        return "$39.99/year";
    }
    
    public static String getYearlySavings() {
        return "Save 33%";
    }
    
    public static String getLifetimePrice() {
        return "$99.99 one-time";
    }
    
    /**
     * Get all Pro features as a list of descriptions.
     */
    public static String[] getProFeaturesList() {
        return new String[]{
            "🐛 All 90+ debugging challenges",
            "⚔️ Unlimited PvP battles",
            "💡 Unlimited hints",
            "🧮 Algorithm Arena",
            "🔍 Code Review Mode",
            "⛳ Code Golf challenges",
            "📊 Advanced analytics & insights",
            "🎨 10+ custom themes",
            "🚫 Ad-free experience",
            "📱 Offline mode",
            "⭐ Priority support",
            "🏅 Exclusive badges & titles",
            "📜 Achievement certificates",
            "📄 Resume/portfolio export",
            "💼 Interview prep mode",
            "🏢 Company-specific practice",
            "🚀 Early access to new features",
            "🏆 Monthly tournaments",
            "👥 Team battle mode",
            "2️⃣ 2x XP weekends"
        };
    }
}
