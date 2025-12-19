package com.example.debugappproject.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - MULTI-FILE BUG MODEL                                 ║
 * ║              Bugs That Span Across Multiple Files                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class MultiFileBug {

    private int id;
    private String title;
    private String description;
    private String difficulty; // Easy, Medium, Hard, Expert
    private String category;
    private String language;
    
    // List of files involved
    private List<BugFile> files;
    
    // Which file contains the actual bug
    private int bugFileIndex;
    
    // Line number of the bug in the bug file
    private int bugLineNumber;
    
    // The fix explanation
    private String explanation;
    
    // Hints for each level of difficulty
    private List<String> hints;
    
    // XP reward
    private int xpReward;
    
    // Time limit in seconds (0 = no limit)
    private int timeLimit;
    
    // Tags for categorization
    private List<String> tags;
    
    public MultiFileBug() {
        files = new ArrayList<>();
        hints = new ArrayList<>();
        tags = new ArrayList<>();
    }
    
    public static class BugFile {
        public String fileName;
        public String filePath; // e.g., "src/main/java/com/example/Service.java"
        public String brokenCode;
        public String fixedCode;
        public String fileType; // "java", "xml", "json", etc.
        public boolean containsBug;
        
        public BugFile() {}
        
        public BugFile(String fileName, String filePath, String brokenCode, String fixedCode, String fileType, boolean containsBug) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.brokenCode = brokenCode;
            this.fixedCode = fixedCode;
            this.fileType = fileType;
            this.containsBug = containsBug;
        }
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public List<BugFile> getFiles() { return files; }
    public void setFiles(List<BugFile> files) { this.files = files; }
    
    public int getBugFileIndex() { return bugFileIndex; }
    public void setBugFileIndex(int bugFileIndex) { this.bugFileIndex = bugFileIndex; }
    
    public int getBugLineNumber() { return bugLineNumber; }
    public void setBugLineNumber(int bugLineNumber) { this.bugLineNumber = bugLineNumber; }
    
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    
    public List<String> getHints() { return hints; }
    public void setHints(List<String> hints) { this.hints = hints; }
    
    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }
    
    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    // Helper methods
    public BugFile getBugFile() {
        if (bugFileIndex >= 0 && bugFileIndex < files.size()) {
            return files.get(bugFileIndex);
        }
        return null;
    }
    
    public int getFileCount() {
        return files.size();
    }
    
    public void addFile(BugFile file) {
        files.add(file);
    }
    
    public String getHint(int level) {
        if (level > 0 && level <= hints.size()) {
            return hints.get(level - 1);
        }
        return "No hint available for this level.";
    }
}
