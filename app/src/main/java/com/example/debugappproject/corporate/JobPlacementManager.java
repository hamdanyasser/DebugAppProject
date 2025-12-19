package com.example.debugappproject.corporate;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - JOB PLACEMENT SYSTEM                                 ║
 * ║              Connect Skilled Debuggers with Employers                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Revenue Model: 15% commission on successful placements
 */
public class JobPlacementManager {

    private static final String PREFS_NAME = "job_placement_prefs";
    private static final String KEY_PROFILE = "candidate_profile";
    private static final String KEY_JOBS = "available_jobs";
    private static final String KEY_APPLICATIONS = "applications";
    
    private static JobPlacementManager instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    
    public static class CandidateProfile {
        public String userId;
        public String name;
        public String email;
        public String phone;
        public String location;
        public String bio;
        public String resumeUrl;
        public String linkedInUrl;
        public String githubUrl;
        public boolean isPublic;
        public boolean openToWork;
        
        // Skills & Experience
        public int totalBugsFixed;
        public int totalXP;
        public int currentLevel;
        public List<String> languages;
        public List<String> specializations;
        public List<CertificateInfo> certificates;
        public List<BadgeInfo> badges;
        
        // Preferences
        public List<String> preferredRoles;
        public String preferredSalaryRange;
        public boolean remoteOnly;
        public List<String> preferredLocations;
        
        public CandidateProfile() {
            languages = new ArrayList<>();
            specializations = new ArrayList<>();
            certificates = new ArrayList<>();
            badges = new ArrayList<>();
            preferredRoles = new ArrayList<>();
            preferredLocations = new ArrayList<>();
        }
        
        public static class CertificateInfo {
            public String name;
            public String dateEarned;
            public String certificateId;
        }
        
        public static class BadgeInfo {
            public String name;
            public String description;
            public String iconUrl;
        }
    }
    
    public static class JobListing {
        public String id;
        public String companyName;
        public String companyLogo;
        public String title;
        public String description;
        public String location;
        public boolean isRemote;
        public String salaryRange;
        public String experienceLevel; // Junior, Mid, Senior
        public List<String> requiredSkills;
        public List<String> niceToHaveSkills;
        public int minBugsFixed; // Minimum DebugMaster bugs fixed
        public int minLevel; // Minimum DebugMaster level
        public long postedDate;
        public long expiryDate;
        public String applyUrl;
        public int applicantCount;
        public boolean isPartner; // Partner companies get highlighted
        
        public JobListing() {
            requiredSkills = new ArrayList<>();
            niceToHaveSkills = new ArrayList<>();
        }
    }
    
    public static class JobApplication {
        public String id;
        public String jobId;
        public String candidateId;
        public long applicationDate;
        public String status; // PENDING, REVIEWED, INTERVIEW, OFFERED, REJECTED, HIRED
        public String coverLetter;
        public List<String> highlightedProjects;
        
        public JobApplication() {
            highlightedProjects = new ArrayList<>();
        }
    }
    
    private JobPlacementManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    public static synchronized JobPlacementManager getInstance(Context context) {
        if (instance == null) {
            instance = new JobPlacementManager(context);
        }
        return instance;
    }
    
    // ==================== Profile Management ====================
    
    public void saveProfile(CandidateProfile profile) {
        prefs.edit().putString(KEY_PROFILE, gson.toJson(profile)).apply();
    }
    
    public CandidateProfile getProfile() {
        String json = prefs.getString(KEY_PROFILE, null);
        if (json != null) {
            return gson.fromJson(json, CandidateProfile.class);
        }
        return null;
    }
    
    public boolean hasProfile() {
        return getProfile() != null;
    }
    
    // ==================== Job Listings ====================
    
    public List<JobListing> getAvailableJobs() {
        String json = prefs.getString(KEY_JOBS, "[]");
        Type type = new TypeToken<List<JobListing>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    public List<JobListing> getMatchingJobs(CandidateProfile profile) {
        List<JobListing> allJobs = getAvailableJobs();
        List<JobListing> matching = new ArrayList<>();
        
        for (JobListing job : allJobs) {
            if (isJobMatch(job, profile)) {
                matching.add(job);
            }
        }
        
        // Sort by match quality
        matching.sort((a, b) -> calculateMatchScore(b, profile) - calculateMatchScore(a, profile));
        
        return matching;
    }
    
    private boolean isJobMatch(JobListing job, CandidateProfile profile) {
        // Check minimum requirements
        if (profile.totalBugsFixed < job.minBugsFixed) return false;
        if (profile.currentLevel < job.minLevel) return false;
        
        // Check at least one required skill
        for (String skill : job.requiredSkills) {
            if (profile.languages.contains(skill) || profile.specializations.contains(skill)) {
                return true;
            }
        }
        
        return false;
    }
    
    private int calculateMatchScore(JobListing job, CandidateProfile profile) {
        int score = 0;
        
        // Skill matches
        for (String skill : job.requiredSkills) {
            if (profile.languages.contains(skill)) score += 10;
            if (profile.specializations.contains(skill)) score += 15;
        }
        
        for (String skill : job.niceToHaveSkills) {
            if (profile.languages.contains(skill)) score += 5;
            if (profile.specializations.contains(skill)) score += 8;
        }
        
        // Experience bonus
        score += Math.min(profile.totalBugsFixed / 10, 50);
        score += profile.currentLevel * 2;
        
        // Certificate bonus
        score += profile.certificates.size() * 10;
        
        // Partner company bonus
        if (job.isPartner) score += 20;
        
        return score;
    }
    
    // ==================== Applications ====================
    
    public void applyToJob(JobApplication application) {
        List<JobApplication> applications = getApplications();
        application.id = "APP-" + System.currentTimeMillis();
        application.applicationDate = System.currentTimeMillis();
        application.status = "PENDING";
        applications.add(application);
        saveApplications(applications);
    }
    
    public List<JobApplication> getApplications() {
        String json = prefs.getString(KEY_APPLICATIONS, "[]");
        Type type = new TypeToken<List<JobApplication>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    public List<JobApplication> getApplicationsByStatus(String status) {
        List<JobApplication> result = new ArrayList<>();
        for (JobApplication app : getApplications()) {
            if (app.status.equals(status)) result.add(app);
        }
        return result;
    }
    
    private void saveApplications(List<JobApplication> applications) {
        prefs.edit().putString(KEY_APPLICATIONS, gson.toJson(applications)).apply();
    }
    
    public boolean hasApplied(String jobId) {
        for (JobApplication app : getApplications()) {
            if (app.jobId.equals(jobId)) return true;
        }
        return false;
    }
    
    // ==================== Sample Jobs ====================
    
    public void loadSampleJobs() {
        List<JobListing> jobs = new ArrayList<>();
        
        JobListing job1 = new JobListing();
        job1.id = "JOB001";
        job1.companyName = "TechCorp";
        job1.title = "Junior Java Developer";
        job1.description = "Join our team to build scalable backend services. We value debugging skills!";
        job1.location = "San Francisco, CA";
        job1.isRemote = true;
        job1.salaryRange = "$80k - $100k";
        job1.experienceLevel = "Junior";
        job1.requiredSkills.add("Java");
        job1.requiredSkills.add("SQL");
        job1.niceToHaveSkills.add("Spring Boot");
        job1.minBugsFixed = 50;
        job1.minLevel = 5;
        job1.postedDate = System.currentTimeMillis();
        job1.isPartner = true;
        jobs.add(job1);
        
        JobListing job2 = new JobListing();
        job2.id = "JOB002";
        job2.companyName = "StartupXYZ";
        job2.title = "Full Stack Developer";
        job2.description = "Fast-paced startup looking for problem solvers who can debug anything!";
        job2.location = "Remote";
        job2.isRemote = true;
        job2.salaryRange = "$90k - $120k";
        job2.experienceLevel = "Mid";
        job2.requiredSkills.add("JavaScript");
        job2.requiredSkills.add("Python");
        job2.niceToHaveSkills.add("React");
        job2.niceToHaveSkills.add("Node.js");
        job2.minBugsFixed = 100;
        job2.minLevel = 10;
        job2.postedDate = System.currentTimeMillis();
        jobs.add(job2);
        
        JobListing job3 = new JobListing();
        job3.id = "JOB003";
        job3.companyName = "BigData Inc";
        job3.title = "Senior Software Engineer";
        job3.description = "Debug complex distributed systems at scale.";
        job3.location = "New York, NY";
        job3.isRemote = false;
        job3.salaryRange = "$150k - $200k";
        job3.experienceLevel = "Senior";
        job3.requiredSkills.add("Java");
        job3.requiredSkills.add("Python");
        job3.requiredSkills.add("SQL");
        job3.niceToHaveSkills.add("Kubernetes");
        job3.niceToHaveSkills.add("AWS");
        job3.minBugsFixed = 500;
        job3.minLevel = 25;
        job3.postedDate = System.currentTimeMillis();
        job3.isPartner = true;
        jobs.add(job3);
        
        prefs.edit().putString(KEY_JOBS, gson.toJson(jobs)).apply();
    }
    
    // ==================== Stats ====================
    
    public int getTotalPlacements() {
        int count = 0;
        for (JobApplication app : getApplications()) {
            if (app.status.equals("HIRED")) count++;
        }
        return count;
    }
    
    public String getProfileStrength(CandidateProfile profile) {
        if (profile == null) return "Not Created";
        
        int score = 0;
        if (profile.name != null && !profile.name.isEmpty()) score += 10;
        if (profile.email != null && !profile.email.isEmpty()) score += 10;
        if (profile.bio != null && !profile.bio.isEmpty()) score += 15;
        if (profile.linkedInUrl != null && !profile.linkedInUrl.isEmpty()) score += 10;
        if (profile.githubUrl != null && !profile.githubUrl.isEmpty()) score += 15;
        if (!profile.languages.isEmpty()) score += 15;
        if (!profile.certificates.isEmpty()) score += 15;
        if (profile.totalBugsFixed >= 100) score += 10;
        
        if (score >= 80) return "Strong";
        if (score >= 50) return "Good";
        if (score >= 30) return "Basic";
        return "Weak";
    }
}
