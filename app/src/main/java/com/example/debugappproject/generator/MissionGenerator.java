package com.example.debugappproject.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              DEBUGMASTER - AI MISSION GENERATOR                              ║
 * ║           Generate Buggy Code Challenges Dynamically 🎯                      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - Supports 7 languages: Java, Python, JavaScript, C++, C#, Swift, Kotlin
 * - 4 difficulty levels: Easy, Medium, Hard, Expert
 * - Multiple bug categories
 * - Auto-generated explanations
 * - Template-based generation for consistent quality
 */
public class MissionGenerator {

    private final Random random = new Random();

    // Supported languages
    public enum Language {
        JAVA("Java"),
        PYTHON("Python"),
        JAVASCRIPT("JavaScript"),
        CPP("C++"),
        CSHARP("C#"),
        SWIFT("Swift"),
        KOTLIN("Kotlin");

        private final String displayName;
        Language(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    // Difficulty levels
    public enum Difficulty {
        EASY(10, "Easy"),
        MEDIUM(25, "Medium"),
        HARD(50, "Hard"),
        EXPERT(100, "Expert");

        private final int baseXp;
        private final String displayName;
        Difficulty(int baseXp, String displayName) {
            this.baseXp = baseXp;
            this.displayName = displayName;
        }
        public int getBaseXp() { return baseXp; }
        public String getDisplayName() { return displayName; }
    }

    // Bug categories
    public enum BugCategory {
        SYNTAX("Syntax Error"),
        LOGIC("Logic Bug"),
        OFF_BY_ONE("Off-by-One"),
        NULL_REFERENCE("Null Reference"),
        INFINITE_LOOP("Infinite Loop"),
        TYPE_ERROR("Type Error"),
        ARRAY_BOUNDS("Array Bounds"),
        STRING_MANIPULATION("String Manipulation"),
        OPERATOR_PRECEDENCE("Operator Precedence"),
        COMPARISON_ERROR("Comparison Error");

        private final String displayName;
        BugCategory(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    /**
     * Generate a mission based on language and difficulty.
     */
    public GeneratedMission generate(Language language, Difficulty difficulty) {
        // Select a random bug category appropriate for difficulty
        BugCategory category = selectCategoryForDifficulty(difficulty);

        // Get template for this language and category
        MissionTemplate template = getTemplate(language, category, difficulty);

        // Build the mission
        GeneratedMission mission = new GeneratedMission();
        mission.title = template.title;
        mission.language = language.getDisplayName();
        mission.difficulty = difficulty.getDisplayName();
        mission.category = category.getDisplayName();
        mission.description = template.description;
        mission.brokenCode = template.brokenCode;
        mission.fixedCode = template.fixedCode;
        mission.hint = template.hint;
        mission.explanation = template.explanation;
        mission.expectedOutput = template.expectedOutput;
        mission.actualOutput = template.actualOutput;
        mission.baseXp = difficulty.getBaseXp();

        return mission;
    }

    private BugCategory selectCategoryForDifficulty(Difficulty difficulty) {
        BugCategory[] easyCats = {BugCategory.SYNTAX, BugCategory.COMPARISON_ERROR, BugCategory.STRING_MANIPULATION};
        BugCategory[] mediumCats = {BugCategory.LOGIC, BugCategory.OFF_BY_ONE, BugCategory.ARRAY_BOUNDS, BugCategory.NULL_REFERENCE};
        BugCategory[] hardCats = {BugCategory.INFINITE_LOOP, BugCategory.TYPE_ERROR, BugCategory.OPERATOR_PRECEDENCE};
        BugCategory[] expertCats = BugCategory.values(); // All categories

        BugCategory[] pool;
        switch (difficulty) {
            case EASY: pool = easyCats; break;
            case MEDIUM: pool = mediumCats; break;
            case HARD: pool = hardCats; break;
            default: pool = expertCats; break;
        }

        return pool[random.nextInt(pool.length)];
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEMPLATE DATABASE
    // ═══════════════════════════════════════════════════════════════════════════

    private MissionTemplate getTemplate(Language language, BugCategory category, Difficulty difficulty) {
        // Get templates for this language
        Map<BugCategory, List<MissionTemplate>> langTemplates = getTemplatesForLanguage(language);

        // Get templates for this category
        List<MissionTemplate> categoryTemplates = langTemplates.get(category);
        if (categoryTemplates == null || categoryTemplates.isEmpty()) {
            // Fallback to a generic template
            categoryTemplates = getGenericTemplates(language);
        }

        // Select random template
        return categoryTemplates.get(random.nextInt(categoryTemplates.size()));
    }

    private Map<BugCategory, List<MissionTemplate>> getTemplatesForLanguage(Language language) {
        Map<BugCategory, List<MissionTemplate>> templates = new HashMap<>();

        switch (language) {
            case JAVA:
                templates = getJavaTemplates();
                break;
            case PYTHON:
                templates = getPythonTemplates();
                break;
            case JAVASCRIPT:
                templates = getJavaScriptTemplates();
                break;
            case KOTLIN:
                templates = getKotlinTemplates();
                break;
            case SWIFT:
                templates = getSwiftTemplates();
                break;
            case CPP:
                templates = getCppTemplates();
                break;
            case CSHARP:
                templates = getCSharpTemplates();
                break;
        }

        return templates;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // JAVA TEMPLATES
    // ═══════════════════════════════════════════════════════════════════════════

    private Map<BugCategory, List<MissionTemplate>> getJavaTemplates() {
        Map<BugCategory, List<MissionTemplate>> templates = new HashMap<>();

        // Syntax Error Templates
        List<MissionTemplate> syntaxTemplates = new ArrayList<>();
        syntaxTemplates.add(new MissionTemplate()
            .setTitle("🔧 Missing Semicolon")
            .setDescription("The code won't compile! Find the missing semicolon.")
            .setBrokenCode("public class Main {\n    public static void main(String[] args) {\n        int x = 5\n        System.out.println(x);\n    }\n}")
            .setFixedCode("public class Main {\n    public static void main(String[] args) {\n        int x = 5;\n        System.out.println(x);\n    }\n}")
            .setHint("Every statement in Java must end with a semicolon!")
            .setExplanation("Java requires semicolons to end statements. The line 'int x = 5' was missing its semicolon.")
            .setExpectedOutput("5")
            .setActualOutput("Compilation Error"));
        templates.put(BugCategory.SYNTAX, syntaxTemplates);

        // Off-by-One Templates
        List<MissionTemplate> offByOneTemplates = new ArrayList<>();
        offByOneTemplates.add(new MissionTemplate()
            .setTitle("🎯 Array Index Issue")
            .setDescription("The array access is one position off!")
            .setBrokenCode("int[] arr = {1, 2, 3, 4, 5};\nfor (int i = 0; i <= arr.length; i++) {\n    System.out.println(arr[i]);\n}")
            .setFixedCode("int[] arr = {1, 2, 3, 4, 5};\nfor (int i = 0; i < arr.length; i++) {\n    System.out.println(arr[i]);\n}")
            .setHint("Array indices go from 0 to length-1. What happens at i = length?")
            .setExplanation("Classic off-by-one! Use < instead of <= because array indices are 0 to length-1.")
            .setExpectedOutput("1 2 3 4 5")
            .setActualOutput("ArrayIndexOutOfBoundsException"));
        templates.put(BugCategory.OFF_BY_ONE, offByOneTemplates);

        // Logic Bug Templates
        List<MissionTemplate> logicTemplates = new ArrayList<>();
        logicTemplates.add(new MissionTemplate()
            .setTitle("🧠 Wrong Comparison")
            .setDescription("The max function returns the wrong value!")
            .setBrokenCode("public int max(int a, int b) {\n    if (a < b) {\n        return a;\n    }\n    return b;\n}")
            .setFixedCode("public int max(int a, int b) {\n    if (a > b) {\n        return a;\n    }\n    return b;\n}")
            .setHint("If a is less than b, should we return a or b for max?")
            .setExplanation("The comparison was inverted! For max, return a when a > b, not when a < b.")
            .setExpectedOutput("Returns larger value")
            .setActualOutput("Returns smaller value"));
        templates.put(BugCategory.LOGIC, logicTemplates);

        // Infinite Loop Templates
        List<MissionTemplate> loopTemplates = new ArrayList<>();
        loopTemplates.add(new MissionTemplate()
            .setTitle("♾️ Never-ending Loop")
            .setDescription("This loop never stops! Find out why.")
            .setBrokenCode("int count = 0;\nwhile (count < 10) {\n    System.out.println(count);\n    // count++; <- forgot to increment!\n}")
            .setFixedCode("int count = 0;\nwhile (count < 10) {\n    System.out.println(count);\n    count++;\n}")
            .setHint("The loop condition depends on count, but count never changes...")
            .setExplanation("Without incrementing count, the condition count < 10 is always true!")
            .setExpectedOutput("0 1 2 3 4 5 6 7 8 9")
            .setActualOutput("0 0 0 0... (forever)"));
        templates.put(BugCategory.INFINITE_LOOP, loopTemplates);

        return templates;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PYTHON TEMPLATES
    // ═══════════════════════════════════════════════════════════════════════════

    private Map<BugCategory, List<MissionTemplate>> getPythonTemplates() {
        Map<BugCategory, List<MissionTemplate>> templates = new HashMap<>();

        // Syntax Error Templates
        List<MissionTemplate> syntaxTemplates = new ArrayList<>();
        syntaxTemplates.add(new MissionTemplate()
            .setTitle("🐍 Indentation Error")
            .setDescription("Python is picky about whitespace!")
            .setBrokenCode("def greet(name):\nprint(f\"Hello, {name}!\")")
            .setFixedCode("def greet(name):\n    print(f\"Hello, {name}!\")")
            .setHint("In Python, the function body must be indented!")
            .setExplanation("Python uses indentation to define code blocks. The print statement needs 4 spaces.")
            .setExpectedOutput("Hello, World!")
            .setActualOutput("IndentationError"));
        templates.put(BugCategory.SYNTAX, syntaxTemplates);

        // Logic Bug Templates
        List<MissionTemplate> logicTemplates = new ArrayList<>();
        logicTemplates.add(new MissionTemplate()
            .setTitle("🔢 Average Calculation Bug")
            .setDescription("The average is coming out wrong!")
            .setBrokenCode("def average(a, b, c):\n    return a + b + c / 3")
            .setFixedCode("def average(a, b, c):\n    return (a + b + c) / 3")
            .setHint("Remember order of operations: division before addition!")
            .setExplanation("Without parentheses, only c is divided by 3. Use (a + b + c) / 3!")
            .setExpectedOutput("20.0 for average(10, 20, 30)")
            .setActualOutput("40.0"));
        templates.put(BugCategory.LOGIC, logicTemplates);

        // Off-by-One Templates
        List<MissionTemplate> offByOneTemplates = new ArrayList<>();
        offByOneTemplates.add(new MissionTemplate()
            .setTitle("📝 List Slicing Error")
            .setDescription("The slice is missing the last element!")
            .setBrokenCode("numbers = [1, 2, 3, 4, 5]\nfirst_three = numbers[0:2]\nprint(first_three)")
            .setFixedCode("numbers = [1, 2, 3, 4, 5]\nfirst_three = numbers[0:3]\nprint(first_three)")
            .setHint("Python slices go up to but don't include the end index!")
            .setExplanation("numbers[0:2] gives indices 0 and 1 only. Use [0:3] for first three elements.")
            .setExpectedOutput("[1, 2, 3]")
            .setActualOutput("[1, 2]"));
        templates.put(BugCategory.OFF_BY_ONE, offByOneTemplates);

        return templates;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // JAVASCRIPT TEMPLATES
    // ═══════════════════════════════════════════════════════════════════════════

    private Map<BugCategory, List<MissionTemplate>> getJavaScriptTemplates() {
        Map<BugCategory, List<MissionTemplate>> templates = new HashMap<>();

        // Comparison Error Templates
        List<MissionTemplate> comparisonTemplates = new ArrayList<>();
        comparisonTemplates.add(new MissionTemplate()
            .setTitle("⚡ Equality Check Bug")
            .setDescription("The comparison doesn't work as expected!")
            .setBrokenCode("function isEqual(a, b) {\n    return a == b;\n}\nconsole.log(isEqual('5', 5)); // Should be false!")
            .setFixedCode("function isEqual(a, b) {\n    return a === b;\n}\nconsole.log(isEqual('5', 5)); // Now correctly false")
            .setHint("JavaScript has two equality operators: == and ===. One checks type too!")
            .setExplanation("Use === for strict equality that checks both value AND type!")
            .setExpectedOutput("false")
            .setActualOutput("true"));
        templates.put(BugCategory.COMPARISON_ERROR, comparisonTemplates);

        // Null Reference Templates
        List<MissionTemplate> nullTemplates = new ArrayList<>();
        nullTemplates.add(new MissionTemplate()
            .setTitle("💥 Cannot Read Property")
            .setDescription("Accessing a property on undefined crashes!")
            .setBrokenCode("function getLength(str) {\n    return str.length;\n}\nconsole.log(getLength(undefined));")
            .setFixedCode("function getLength(str) {\n    if (str == null) return 0;\n    return str.length;\n}\nconsole.log(getLength(undefined));")
            .setHint("What happens when str is undefined or null?")
            .setExplanation("Always check for null/undefined before accessing properties!")
            .setExpectedOutput("0")
            .setActualOutput("TypeError: Cannot read property 'length' of undefined"));
        templates.put(BugCategory.NULL_REFERENCE, nullTemplates);

        return templates;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // KOTLIN TEMPLATES
    // ═══════════════════════════════════════════════════════════════════════════

    private Map<BugCategory, List<MissionTemplate>> getKotlinTemplates() {
        Map<BugCategory, List<MissionTemplate>> templates = new HashMap<>();

        // Null Safety Templates
        List<MissionTemplate> nullTemplates = new ArrayList<>();
        nullTemplates.add(new MissionTemplate()
            .setTitle("🛡️ Null Safety Violation")
            .setDescription("Kotlin's null safety is being bypassed unsafely!")
            .setBrokenCode("fun getLength(str: String?): Int {\n    return str!!.length\n}")
            .setFixedCode("fun getLength(str: String?): Int {\n    return str?.length ?: 0\n}")
            .setHint("Using !! is dangerous! What's the safe alternative?")
            .setExplanation("Use ?. for safe calls and ?: for default values instead of !!")
            .setExpectedOutput("0 when null")
            .setActualOutput("NullPointerException"));
        templates.put(BugCategory.NULL_REFERENCE, nullTemplates);

        return templates;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SWIFT TEMPLATES
    // ═══════════════════════════════════════════════════════════════════════════

    private Map<BugCategory, List<MissionTemplate>> getSwiftTemplates() {
        Map<BugCategory, List<MissionTemplate>> templates = new HashMap<>();

        // Optional Handling Templates
        List<MissionTemplate> optionalTemplates = new ArrayList<>();
        optionalTemplates.add(new MissionTemplate()
            .setTitle("📱 Force Unwrap Crash")
            .setDescription("The app crashes on nil optional!")
            .setBrokenCode("func greet(name: String?) {\n    print(\"Hello, \\(name!)\")\n}")
            .setFixedCode("func greet(name: String?) {\n    print(\"Hello, \\(name ?? \"Guest\")\")\n}")
            .setHint("Force unwrapping (!) on nil causes a crash. Use nil coalescing!")
            .setExplanation("Use ?? to provide a default value instead of force unwrapping.")
            .setExpectedOutput("Hello, Guest")
            .setActualOutput("Fatal error: Unexpectedly found nil"));
        templates.put(BugCategory.NULL_REFERENCE, optionalTemplates);

        return templates;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // C++ TEMPLATES
    // ═══════════════════════════════════════════════════════════════════════════

    private Map<BugCategory, List<MissionTemplate>> getCppTemplates() {
        Map<BugCategory, List<MissionTemplate>> templates = new HashMap<>();

        // Array Bounds Templates
        List<MissionTemplate> boundsTemplates = new ArrayList<>();
        boundsTemplates.add(new MissionTemplate()
            .setTitle("💾 Buffer Overflow")
            .setDescription("Writing past array bounds causes undefined behavior!")
            .setBrokenCode("int arr[5];\nfor (int i = 0; i <= 5; i++) {\n    arr[i] = i;\n}")
            .setFixedCode("int arr[5];\nfor (int i = 0; i < 5; i++) {\n    arr[i] = i;\n}")
            .setHint("Array of size 5 has indices 0-4, not 0-5!")
            .setExplanation("Writing to arr[5] is undefined behavior. Use i < 5 instead of i <= 5.")
            .setExpectedOutput("Array filled with 0-4")
            .setActualOutput("Undefined behavior / crash"));
        templates.put(BugCategory.ARRAY_BOUNDS, boundsTemplates);

        return templates;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // C# TEMPLATES
    // ═══════════════════════════════════════════════════════════════════════════

    private Map<BugCategory, List<MissionTemplate>> getCSharpTemplates() {
        Map<BugCategory, List<MissionTemplate>> templates = new HashMap<>();

        // Null Reference Templates
        List<MissionTemplate> nullTemplates = new ArrayList<>();
        nullTemplates.add(new MissionTemplate()
            .setTitle("⚠️ NullReferenceException")
            .setDescription("The code throws NullReferenceException!")
            .setBrokenCode("string name = null;\nint length = name.Length;")
            .setFixedCode("string name = null;\nint length = name?.Length ?? 0;")
            .setHint("Accessing properties on null throws. Use null-conditional operator!")
            .setExplanation("Use ?. and ?? operators to safely handle null values in C#.")
            .setExpectedOutput("0")
            .setActualOutput("NullReferenceException"));
        templates.put(BugCategory.NULL_REFERENCE, nullTemplates);

        return templates;
    }

    private List<MissionTemplate> getGenericTemplates(Language language) {
        List<MissionTemplate> templates = new ArrayList<>();
        templates.add(new MissionTemplate()
            .setTitle("🔍 Find the Bug")
            .setDescription("Something is wrong with this code. Can you fix it?")
            .setBrokenCode("// Debug this " + language.getDisplayName() + " code\nint x = 10;\nint y = 0;\nint result = x / y;")
            .setFixedCode("int x = 10;\nint y = 1; // Changed from 0\nint result = x / y;")
            .setHint("Division by zero is always a problem!")
            .setExplanation("Cannot divide by zero. Changed y from 0 to a non-zero value.")
            .setExpectedOutput("10")
            .setActualOutput("ArithmeticException: / by zero"));
        return templates;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DATA CLASSES
    // ═══════════════════════════════════════════════════════════════════════════

    public static class GeneratedMission {
        public String title = "";
        public String language = "";
        public String difficulty = "";
        public String category = "";
        public String description = "";
        public String brokenCode = "";
        public String fixedCode = "";
        public String hint = "";
        public String explanation = "";
        public String expectedOutput = "";
        public String actualOutput = "";
        public int baseXp = 10;
    }

    public static class MissionTemplate {
        public String title = "";
        public String description = "";
        public String brokenCode = "";
        public String fixedCode = "";
        public String hint = "";
        public String explanation = "";
        public String expectedOutput = "";
        public String actualOutput = "";

        public MissionTemplate setTitle(String t) { this.title = t; return this; }
        public MissionTemplate setDescription(String d) { this.description = d; return this; }
        public MissionTemplate setBrokenCode(String c) { this.brokenCode = c; return this; }
        public MissionTemplate setFixedCode(String c) { this.fixedCode = c; return this; }
        public MissionTemplate setHint(String h) { this.hint = h; return this; }
        public MissionTemplate setExplanation(String e) { this.explanation = e; return this; }
        public MissionTemplate setExpectedOutput(String o) { this.expectedOutput = o; return this; }
        public MissionTemplate setActualOutput(String o) { this.actualOutput = o; return this; }
    }
}
