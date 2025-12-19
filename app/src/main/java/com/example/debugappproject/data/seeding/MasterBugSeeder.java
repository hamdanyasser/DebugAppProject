package com.example.debugappproject.data.seeding;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - MASTER BUG SEEDER                                    ║
 * ║              Seeds ALL Bug Categories Into Database                          ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Bug ID Ranges:
 * - 1-499: Core Java bugs (existing)
 * - 500-599: Python bugs
 * - 600-699: JavaScript bugs
 * - 700-799: Advanced Java (race conditions, memory, etc.)
 * - 800-819: Security bugs
 * - 820-839: Database bugs
 * - 840-859: Web/Frontend bugs
 * - 860-879: API bugs
 * - 880-899: ML/Data Science bugs
 * - 900+: Custom/Imported bugs
 */
public class MasterBugSeeder {

    private static final String TAG = "MasterBugSeeder";
    private static final String PREFS_NAME = "seeder_prefs";
    private static final String KEY_SEEDED_VERSION = "seeded_version";
    private static final int CURRENT_VERSION = 3; // Increment when adding new bugs
    
    private final Context context;
    private final SharedPreferences prefs;
    
    public MasterBugSeeder(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Check if database needs seeding and seed if necessary
     */
    public void seedIfNeeded() {
        int seededVersion = prefs.getInt(KEY_SEEDED_VERSION, 0);
        
        if (seededVersion < CURRENT_VERSION) {
            Log.d(TAG, "Seeding database from version " + seededVersion + " to " + CURRENT_VERSION);
            seedAllBugs();
            prefs.edit().putInt(KEY_SEEDED_VERSION, CURRENT_VERSION).apply();
        } else {
            Log.d(TAG, "Database already seeded at version " + seededVersion);
        }
    }
    
    /**
     * Force re-seed all bugs
     */
    public void forceReseed() {
        Log.d(TAG, "Force re-seeding database");
        prefs.edit().putInt(KEY_SEEDED_VERSION, 0).apply();
        seedAllBugs();
        prefs.edit().putInt(KEY_SEEDED_VERSION, CURRENT_VERSION).apply();
    }
    
    /**
     * Seed all bug categories
     */
    private void seedAllBugs() {
        // 1. Multi-language bugs (Python, JavaScript, Advanced Java)
        MultiLanguageBugSeeder multiLangSeeder = new MultiLanguageBugSeeder(context);
        multiLangSeeder.seedAllBugs();
        
        // 2. Domain-specific bugs (Security, Database, Web, API, ML)
        DomainSpecificBugSeeder domainSeeder = new DomainSpecificBugSeeder(context);
        domainSeeder.seedAllDomainBugs();
        
        Log.d(TAG, "All bugs seeded successfully!");
    }
    
    /**
     * Get total bugs seeded by category
     */
    public String getSeedingStats() {
        return "Bug Categories Seeded:\n" +
               "• Python: 500-549\n" +
               "• JavaScript: 600-649\n" +
               "• Advanced Java: 700-749\n" +
               "• Security: 800-819\n" +
               "• Database: 820-839\n" +
               "• Web/Frontend: 840-859\n" +
               "• API: 860-879\n" +
               "• ML/Data Science: 880-899\n\n" +
               "Version: " + CURRENT_VERSION;
    }
}
