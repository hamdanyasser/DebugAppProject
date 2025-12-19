package com.example.debugappproject.corporate;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - CORPORATE TIER SYSTEM                                ║
 * ║              Team Accounts, Analytics, Custom Bug Databases                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Features:
 * - Team accounts with admin controls
 * - Custom bug databases for company-specific training
 * - Team analytics and progress tracking
 * - Job placement integration
 * - Bulk license management
 */
public class CorporateTierManager {

    private static final String PREFS_NAME = "corporate_prefs";
    private static final String KEY_TEAM_DATA = "team_data";
    private static final String KEY_MEMBERS = "team_members";
    private static final String KEY_CUSTOM_BUGS = "custom_bugs";
    private static final String KEY_ANALYTICS = "analytics";
    
    private static CorporateTierManager instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    
    public static class TeamData {
        public String teamId;
        public String teamName;
        public String companyName;
        public String adminEmail;
        public String licenseKey;
        public long expirationDate;
        public int maxMembers;
        public boolean isActive;
        public List<String> features;
        
        public TeamData() {
            features = new ArrayList<>();
        }
    }
    
    public static class TeamMember {
        public String odai;
        public String name;
        public String email;
        public String role; // "admin", "manager", "member"
        public int bugsFixed;
        public int totalXP;
        public int streakDays;
        public long lastActive;
        public List<String> completedPaths;
        public Map<String, Integer> skillScores;
        
        public TeamMember() {
            completedPaths = new ArrayList<>();
            skillScores = new HashMap<>();
        }
    }
    
    public static class TeamAnalytics {
        public int totalBugsFixed;
        public int totalXPEarned;
        public int averageScore;
        public int activeMembers;
        public Map<String, Integer> bugsByCategory;
        public Map<String, Integer> bugsByDifficulty;
        public Map<String, Integer> bugsByLanguage;
        public List<LeaderboardEntry> topPerformers;
        public List<ActivityEntry> recentActivity;
        
        public TeamAnalytics() {
            bugsByCategory = new HashMap<>();
            bugsByDifficulty = new HashMap<>();
            bugsByLanguage = new HashMap<>();
            topPerformers = new ArrayList<>();
            recentActivity = new ArrayList<>();
        }
        
        public static class LeaderboardEntry {
            public String memberId;
            public String name;
            public int score;
            public int bugsFixed;
        }
        
        public static class ActivityEntry {
            public String memberId;
            public String action;
            public long timestamp;
        }
    }
    
    public static class CustomBug {
        public String id;
        public String title;
        public String language;
        public String difficulty;
        public String category;
        public String description;
        public String brokenCode;
        public String fixedCode;
        public String explanation;
        public String createdBy;
        public long createdAt;
        public List<String> tags;
        
        public CustomBug() {
            tags = new ArrayList<>();
        }
    }
    
    private CorporateTierManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    public static synchronized CorporateTierManager getInstance(Context context) {
        if (instance == null) {
            instance = new CorporateTierManager(context);
        }
        return instance;
    }
    
    // ==================== Team Management ====================
    
    public void createTeam(TeamData team) {
        prefs.edit().putString(KEY_TEAM_DATA, gson.toJson(team)).apply();
    }
    
    public TeamData getTeamData() {
        String json = prefs.getString(KEY_TEAM_DATA, null);
        if (json != null) {
            return gson.fromJson(json, TeamData.class);
        }
        return null;
    }
    
    public boolean isTeamActive() {
        TeamData team = getTeamData();
        return team != null && team.isActive && team.expirationDate > System.currentTimeMillis();
    }
    
    public boolean validateLicenseKey(String key) {
        // Simplified validation - in production, verify with server
        return key != null && key.startsWith("DM-CORP-") && key.length() == 20;
    }
    
    // ==================== Member Management ====================
    
    public void addMember(TeamMember member) {
        List<TeamMember> members = getMembers();
        members.add(member);
        saveMembers(members);
    }
    
    public void updateMember(TeamMember member) {
        List<TeamMember> members = getMembers();
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).odai.equals(member.odai)) {
                members.set(i, member);
                break;
            }
        }
        saveMembers(members);
    }
    
    public void removeMember(String odai) {
        List<TeamMember> members = getMembers();
        members.removeIf(m -> m.odai.equals(odai));
        saveMembers(members);
    }
    
    public List<TeamMember> getMembers() {
        String json = prefs.getString(KEY_MEMBERS, "[]");
        Type type = new TypeToken<List<TeamMember>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    private void saveMembers(List<TeamMember> members) {
        prefs.edit().putString(KEY_MEMBERS, gson.toJson(members)).apply();
    }
    
    public TeamMember getMember(String odai) {
        for (TeamMember member : getMembers()) {
            if (member.odai.equals(odai)) return member;
        }
        return null;
    }
    
    // ==================== Custom Bug Database ====================
    
    public void addCustomBug(CustomBug bug) {
        List<CustomBug> bugs = getCustomBugs();
        bug.id = "CORP-" + System.currentTimeMillis();
        bug.createdAt = System.currentTimeMillis();
        bugs.add(bug);
        saveCustomBugs(bugs);
    }
    
    public void updateCustomBug(CustomBug bug) {
        List<CustomBug> bugs = getCustomBugs();
        for (int i = 0; i < bugs.size(); i++) {
            if (bugs.get(i).id.equals(bug.id)) {
                bugs.set(i, bug);
                break;
            }
        }
        saveCustomBugs(bugs);
    }
    
    public void deleteCustomBug(String id) {
        List<CustomBug> bugs = getCustomBugs();
        bugs.removeIf(b -> b.id.equals(id));
        saveCustomBugs(bugs);
    }
    
    public List<CustomBug> getCustomBugs() {
        String json = prefs.getString(KEY_CUSTOM_BUGS, "[]");
        Type type = new TypeToken<List<CustomBug>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    public List<CustomBug> getCustomBugsByLanguage(String language) {
        List<CustomBug> result = new ArrayList<>();
        for (CustomBug bug : getCustomBugs()) {
            if (bug.language.equalsIgnoreCase(language)) result.add(bug);
        }
        return result;
    }
    
    private void saveCustomBugs(List<CustomBug> bugs) {
        prefs.edit().putString(KEY_CUSTOM_BUGS, gson.toJson(bugs)).apply();
    }
    
    // ==================== Analytics ====================
    
    public TeamAnalytics getAnalytics() {
        TeamAnalytics analytics = new TeamAnalytics();
        List<TeamMember> members = getMembers();
        
        int totalBugs = 0;
        int totalXP = 0;
        int activeCount = 0;
        long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
        
        for (TeamMember member : members) {
            totalBugs += member.bugsFixed;
            totalXP += member.totalXP;
            
            if (member.lastActive > oneWeekAgo) {
                activeCount++;
            }
            
            // Aggregate skill scores
            for (Map.Entry<String, Integer> entry : member.skillScores.entrySet()) {
                analytics.bugsByCategory.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
            
            // Add to leaderboard
            TeamAnalytics.LeaderboardEntry entry = new TeamAnalytics.LeaderboardEntry();
            entry.memberId = member.odai;
            entry.name = member.name;
            entry.score = member.totalXP;
            entry.bugsFixed = member.bugsFixed;
            analytics.topPerformers.add(entry);
        }
        
        analytics.totalBugsFixed = totalBugs;
        analytics.totalXPEarned = totalXP;
        analytics.activeMembers = activeCount;
        analytics.averageScore = members.isEmpty() ? 0 : totalXP / members.size();
        
        // Sort leaderboard
        analytics.topPerformers.sort((a, b) -> b.score - a.score);
        if (analytics.topPerformers.size() > 10) {
            analytics.topPerformers = analytics.topPerformers.subList(0, 10);
        }
        
        return analytics;
    }
    
    public void recordActivity(String memberId, String action) {
        TeamAnalytics analytics = getStoredAnalytics();
        TeamAnalytics.ActivityEntry entry = new TeamAnalytics.ActivityEntry();
        entry.memberId = memberId;
        entry.action = action;
        entry.timestamp = System.currentTimeMillis();
        analytics.recentActivity.add(0, entry);
        
        // Keep only last 100 activities
        if (analytics.recentActivity.size() > 100) {
            analytics.recentActivity = analytics.recentActivity.subList(0, 100);
        }
        
        saveAnalytics(analytics);
    }
    
    private TeamAnalytics getStoredAnalytics() {
        String json = prefs.getString(KEY_ANALYTICS, null);
        if (json != null) {
            return gson.fromJson(json, TeamAnalytics.class);
        }
        return new TeamAnalytics();
    }
    
    private void saveAnalytics(TeamAnalytics analytics) {
        prefs.edit().putString(KEY_ANALYTICS, gson.toJson(analytics)).apply();
    }
    
    // ==================== Reports ====================
    
    public String generateProgressReport() {
        TeamData team = getTeamData();
        TeamAnalytics analytics = getAnalytics();
        List<TeamMember> members = getMembers();
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════\n");
        report.append("  DEBUGMASTER CORPORATE PROGRESS REPORT\n");
        report.append("═══════════════════════════════════════\n\n");
        
        if (team != null) {
            report.append("Team: ").append(team.teamName).append("\n");
            report.append("Company: ").append(team.companyName).append("\n\n");
        }
        
        report.append("📊 TEAM STATISTICS\n");
        report.append("─────────────────────\n");
        report.append("Total Members: ").append(members.size()).append("\n");
        report.append("Active (7 days): ").append(analytics.activeMembers).append("\n");
        report.append("Total Bugs Fixed: ").append(analytics.totalBugsFixed).append("\n");
        report.append("Total XP Earned: ").append(analytics.totalXPEarned).append("\n");
        report.append("Average Score: ").append(analytics.averageScore).append("\n\n");
        
        report.append("🏆 TOP PERFORMERS\n");
        report.append("─────────────────────\n");
        int rank = 1;
        for (TeamAnalytics.LeaderboardEntry entry : analytics.topPerformers) {
            report.append(rank).append(". ").append(entry.name)
                  .append(" - ").append(entry.score).append(" XP")
                  .append(" (").append(entry.bugsFixed).append(" bugs)\n");
            rank++;
            if (rank > 5) break;
        }
        
        report.append("\n📈 SKILLS BREAKDOWN\n");
        report.append("─────────────────────\n");
        for (Map.Entry<String, Integer> entry : analytics.bugsByCategory.entrySet()) {
            report.append(entry.getKey()).append(": ").append(entry.getValue()).append(" bugs\n");
        }
        
        return report.toString();
    }
    
    // ==================== Job Placement ====================
    
    public static class JobCandidate {
        public String memberId;
        public String name;
        public String email;
        public int totalBugsFixed;
        public int totalXP;
        public List<String> skills;
        public List<String> certificates;
        public String profileUrl;
    }
    
    public List<JobCandidate> getJobReadyCandidates(int minBugsFixed, int minXP) {
        List<JobCandidate> candidates = new ArrayList<>();
        
        for (TeamMember member : getMembers()) {
            if (member.bugsFixed >= minBugsFixed && member.totalXP >= minXP) {
                JobCandidate candidate = new JobCandidate();
                candidate.memberId = member.odai;
                candidate.name = member.name;
                candidate.email = member.email;
                candidate.totalBugsFixed = member.bugsFixed;
                candidate.totalXP = member.totalXP;
                candidate.skills = new ArrayList<>(member.skillScores.keySet());
                candidate.certificates = member.completedPaths;
                candidates.add(candidate);
            }
        }
        
        return candidates;
    }
}
