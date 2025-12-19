package com.example.debugappproject.data.seeding;

import android.content.Context;

import com.example.debugappproject.data.local.BugDao;
import com.example.debugappproject.data.local.DebugMasterDatabase;
import com.example.debugappproject.model.Bug;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - MULTI-LANGUAGE BUG SEEDER                            ║
 * ║              Python, JavaScript, and Advanced Bug Exercises                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class MultiLanguageBugSeeder {

    private final Context context;
    private final ExecutorService executor;
    
    public MultiLanguageBugSeeder(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public void seedAllBugs() {
        executor.execute(() -> {
            BugDao dao = DebugMasterDatabase.getInstance(context).bugDao();
            
            seedPythonBugs(dao);
            seedJavaScriptBugs(dao);
            seedAdvancedBugs(dao);
        });
    }
    
    private void seedPythonBugs(BugDao dao) {
        // PYTHON - Easy Bugs (500-519)
        dao.insert(new Bug(500, "Python Print Syntax", "Python", "Easy", "Syntax",
            "Print 'Hello World' to the console",
            "print 'Hello World'",
            "Hello World",
            "SyntaxError: Missing parentheses in call to 'print'",
            "In Python 3, print() is a function and requires parentheses",
            "print('Hello World')", false));
        
        dao.insert(new Bug(501, "Python Indentation Error", "Python", "Easy", "Syntax",
            "Define a function that returns the square of a number",
            "def square(n):\nreturn n * n",
            "Returns n squared",
            "IndentationError: expected an indented block",
            "Python uses indentation to define code blocks. The return statement must be indented",
            "def square(n):\n    return n * n", false));
        
        dao.insert(new Bug(502, "Python String Concatenation", "Python", "Easy", "Strings",
            "Create a greeting message with a name and age",
            "name = 'Alice'\nage = 25\nprint('Hello ' + name + ', you are ' + age + ' years old')",
            "Hello Alice, you are 25 years old",
            "TypeError: can only concatenate str (not \"int\") to str",
            "Cannot concatenate string and integer directly. Convert age to string first",
            "name = 'Alice'\nage = 25\nprint('Hello ' + name + ', you are ' + str(age) + ' years old')", false));
        
        dao.insert(new Bug(503, "Python List Index", "Python", "Easy", "Arrays",
            "Get the last element of a list",
            "fruits = ['apple', 'banana', 'orange']\nlast = fruits[3]\nprint(last)",
            "orange",
            "IndexError: list index out of range",
            "List indices start at 0. Use -1 to get the last element or len()-1",
            "fruits = ['apple', 'banana', 'orange']\nlast = fruits[-1]\nprint(last)", false));
        
        dao.insert(new Bug(504, "Python Division", "Python", "Easy", "Math",
            "Calculate the average of two numbers",
            "a = 5\nb = 2\naverage = a + b / 2\nprint(average)",
            "3.5",
            "Output: 6.0 (incorrect result)",
            "Operator precedence: division happens before addition. Use parentheses",
            "a = 5\nb = 2\naverage = (a + b) / 2\nprint(average)", false));
        
        // PYTHON - Medium Bugs (520-539)
        dao.insert(new Bug(520, "Python Mutable Default", "Python", "Medium", "Functions",
            "Create a function that appends to a list",
            "def add_item(item, lst=[]):\n    lst.append(item)\n    return lst\n\nprint(add_item('a'))\nprint(add_item('b'))",
            "['a']\n['b']",
            "['a']\n['a', 'b']",
            "Mutable default arguments persist between calls. Use None as default",
            "def add_item(item, lst=None):\n    if lst is None:\n        lst = []\n    lst.append(item)\n    return lst\n\nprint(add_item('a'))\nprint(add_item('b'))", false));
        
        dao.insert(new Bug(521, "Python Loop Variable Scope", "Python", "Medium", "Loops",
            "Create functions that return different numbers",
            "functions = []\nfor i in range(3):\n    functions.append(lambda: i)\n\nfor f in functions:\n    print(f())",
            "0\n1\n2",
            "2\n2\n2",
            "Lambda captures variable by reference, not value. Capture as default argument",
            "functions = []\nfor i in range(3):\n    functions.append(lambda x=i: x)\n\nfor f in functions:\n    print(f())", false));
        
        dao.insert(new Bug(522, "Python Dictionary Iteration", "Python", "Medium", "Dictionaries",
            "Remove items from dictionary while iterating",
            "scores = {'a': 1, 'b': 2, 'c': 3}\nfor key in scores:\n    if scores[key] < 2:\n        del scores[key]\nprint(scores)",
            "{'b': 2, 'c': 3}",
            "RuntimeError: dictionary changed size during iteration",
            "Cannot modify dictionary while iterating. Create a copy or use list()",
            "scores = {'a': 1, 'b': 2, 'c': 3}\nfor key in list(scores.keys()):\n    if scores[key] < 2:\n        del scores[key]\nprint(scores)", false));
        
        // PYTHON - Hard Bugs (540-549)
        dao.insert(new Bug(540, "Python Async/Await", "Python", "Hard", "Async",
            "Fetch data asynchronously",
            "import asyncio\n\nasync def fetch_data():\n    return 'data'\n\nresult = fetch_data()\nprint(result)",
            "data",
            "<coroutine object fetch_data at 0x...>",
            "Async functions return coroutines. Must use await or asyncio.run()",
            "import asyncio\n\nasync def fetch_data():\n    return 'data'\n\nresult = asyncio.run(fetch_data())\nprint(result)", false));
        
        dao.insert(new Bug(541, "Python Generator Memory", "Python", "Hard", "Memory",
            "Process large data efficiently",
            "def get_squares(n):\n    return [x**2 for x in range(n)]\n\n# Memory error for large n\nfor sq in get_squares(10000000):\n    if sq > 100:\n        break",
            "Works efficiently",
            "MemoryError (for very large n)",
            "List comprehension loads all into memory. Use generator expression",
            "def get_squares(n):\n    return (x**2 for x in range(n))\n\nfor sq in get_squares(10000000):\n    if sq > 100:\n        break", false));
    }
    
    private void seedJavaScriptBugs(BugDao dao) {
        // JAVASCRIPT - Easy Bugs (600-619)
        dao.insert(new Bug(600, "JS Equality Check", "JavaScript", "Easy", "Conditionals",
            "Check if a value equals a number",
            "let x = '5';\nif (x == 5) {\n    console.log('Equal');\n} else {\n    console.log('Not equal');\n}",
            "Should show 'Not equal' for string '5' and number 5",
            "Shows 'Equal' due to type coercion",
            "Use === for strict equality to avoid type coercion",
            "let x = '5';\nif (x === 5) {\n    console.log('Equal');\n} else {\n    console.log('Not equal');\n}", false));
        
        dao.insert(new Bug(601, "JS Var Hoisting", "JavaScript", "Easy", "Variables",
            "Use a variable before declaration",
            "console.log(x);\nvar x = 5;\nconsole.log(x);",
            "Error or 5, 5",
            "undefined\n5",
            "var declarations are hoisted but not initializations. Use let/const",
            "let x = 5;\nconsole.log(x);\nconsole.log(x);", false));
        
        dao.insert(new Bug(602, "JS Array Methods", "JavaScript", "Easy", "Arrays",
            "Remove first element and return it",
            "let arr = [1, 2, 3];\nlet first = arr.pop();\nconsole.log(first);",
            "1",
            "3",
            "pop() removes from end, shift() removes from beginning",
            "let arr = [1, 2, 3];\nlet first = arr.shift();\nconsole.log(first);", false));
        
        dao.insert(new Bug(603, "JS Object Property", "JavaScript", "Easy", "Objects",
            "Access object property with variable",
            "let obj = {name: 'Alice', age: 25};\nlet key = 'name';\nconsole.log(obj.key);",
            "Alice",
            "undefined",
            "Dot notation looks for literal 'key'. Use bracket notation for variables",
            "let obj = {name: 'Alice', age: 25};\nlet key = 'name';\nconsole.log(obj[key]);", false));
        
        dao.insert(new Bug(604, "JS String Template", "JavaScript", "Easy", "Strings",
            "Create a template string",
            "let name = 'World';\nlet greeting = 'Hello, ${name}!';\nconsole.log(greeting);",
            "Hello, World!",
            "Hello, ${name}!",
            "Template literals require backticks (`), not single/double quotes",
            "let name = 'World';\nlet greeting = `Hello, ${name}!`;\nconsole.log(greeting);", false));
        
        // JAVASCRIPT - Medium Bugs (620-639)
        dao.insert(new Bug(620, "JS Async Callback", "JavaScript", "Medium", "Async",
            "Get data from async function",
            "function getData() {\n    setTimeout(() => {\n        return 'data';\n    }, 100);\n}\n\nlet result = getData();\nconsole.log(result);",
            "data",
            "undefined",
            "Async operations need callbacks, promises, or async/await",
            "function getData(callback) {\n    setTimeout(() => {\n        callback('data');\n    }, 100);\n}\n\ngetData((result) => {\n    console.log(result);\n});", false));
        
        dao.insert(new Bug(621, "JS This Binding", "JavaScript", "Medium", "Objects",
            "Access object property in callback",
            "const obj = {\n    name: 'Alice',\n    greet: function() {\n        setTimeout(function() {\n            console.log('Hello, ' + this.name);\n        }, 100);\n    }\n};\nobj.greet();",
            "Hello, Alice",
            "Hello, undefined",
            "Regular functions have their own 'this'. Use arrow function or bind()",
            "const obj = {\n    name: 'Alice',\n    greet: function() {\n        setTimeout(() => {\n            console.log('Hello, ' + this.name);\n        }, 100);\n    }\n};\nobj.greet();", false));
        
        dao.insert(new Bug(622, "JS Array Reference", "JavaScript", "Medium", "Arrays",
            "Copy an array without reference",
            "let arr1 = [1, 2, 3];\nlet arr2 = arr1;\narr2.push(4);\nconsole.log(arr1);",
            "[1, 2, 3]",
            "[1, 2, 3, 4]",
            "Assignment copies reference. Use spread operator or slice() for shallow copy",
            "let arr1 = [1, 2, 3];\nlet arr2 = [...arr1];\narr2.push(4);\nconsole.log(arr1);", false));
        
        // JAVASCRIPT - Hard Bugs (640-649)
        dao.insert(new Bug(640, "JS Promise Chain", "JavaScript", "Hard", "Async",
            "Chain promises correctly",
            "fetch('/api/user')\n    .then(response => response.json())\n    .then(user => fetch('/api/posts/' + user.id))\n    .then(response => response.json())\n    .then(posts => console.log(posts))\n    .catch(err => console.log(err));",
            "User's posts",
            "Promise chaining works but error handling could be improved",
            "Each .then() should handle its own errors, or use async/await",
            "async function getUserPosts() {\n    try {\n        const userRes = await fetch('/api/user');\n        const user = await userRes.json();\n        const postsRes = await fetch('/api/posts/' + user.id);\n        const posts = await postsRes.json();\n        console.log(posts);\n    } catch (err) {\n        console.log(err);\n    }\n}\ngetUserPosts();", false));
        
        dao.insert(new Bug(641, "JS Event Loop", "JavaScript", "Hard", "Async",
            "Understand execution order",
            "console.log('1');\nsetTimeout(() => console.log('2'), 0);\nPromise.resolve().then(() => console.log('3'));\nconsole.log('4');",
            "1, 4, 3, 2",
            "Understanding event loop order",
            "Microtasks (Promises) execute before macrotasks (setTimeout)",
            "// Correct order: 1, 4, 3, 2\n// 1: Synchronous\n// 4: Synchronous\n// 3: Microtask (Promise)\n// 2: Macrotask (setTimeout)\nconsole.log('1');\nsetTimeout(() => console.log('2'), 0);\nPromise.resolve().then(() => console.log('3'));\nconsole.log('4');", false));
    }
    
    private void seedAdvancedBugs(BugDao dao) {
        // Advanced Java Bugs - Race Conditions, Memory Leaks, Security (700-720)
        dao.insert(new Bug(700, "Race Condition", "Java", "Hard", "Concurrency",
            "Increment a counter from multiple threads",
            "public class Counter {\n    private int count = 0;\n    \n    public void increment() {\n        count++;\n    }\n    \n    public int getCount() {\n        return count;\n    }\n}",
            "Correct count after concurrent increments",
            "Count is less than expected due to race condition",
            "count++ is not atomic. Use synchronized or AtomicInteger",
            "public class Counter {\n    private AtomicInteger count = new AtomicInteger(0);\n    \n    public void increment() {\n        count.incrementAndGet();\n    }\n    \n    public int getCount() {\n        return count.get();\n    }\n}", false));
        
        dao.insert(new Bug(701, "Memory Leak - Listener", "Java", "Hard", "Memory",
            "Register and unregister event listener",
            "public class MyActivity {\n    private EventBus bus;\n    \n    public void onCreate() {\n        bus = EventBus.getInstance();\n        bus.register(this);\n    }\n    \n    // Missing onDestroy!\n}",
            "Activity is garbage collected properly",
            "Activity leaks because listener still registered",
            "Always unregister listeners in onDestroy/cleanup",
            "public class MyActivity {\n    private EventBus bus;\n    \n    public void onCreate() {\n        bus = EventBus.getInstance();\n        bus.register(this);\n    }\n    \n    public void onDestroy() {\n        bus.unregister(this);\n    }\n}", false));
        
        dao.insert(new Bug(702, "SQL Injection", "Java", "Hard", "Security",
            "Query database with user input",
            "String query = \"SELECT * FROM users WHERE name = '\" + userInput + \"'\";\nStatement stmt = conn.createStatement();\nResultSet rs = stmt.executeQuery(query);",
            "Safe database query",
            "Vulnerable to SQL injection attacks",
            "Use PreparedStatement with parameterized queries",
            "String query = \"SELECT * FROM users WHERE name = ?\";\nPreparedStatement stmt = conn.prepareStatement(query);\nstmt.setString(1, userInput);\nResultSet rs = stmt.executeQuery();", false));
        
        dao.insert(new Bug(703, "Deadlock", "Java", "Hard", "Concurrency",
            "Two threads acquiring locks in different order",
            "// Thread 1\nsynchronized(lockA) {\n    synchronized(lockB) {\n        // work\n    }\n}\n\n// Thread 2\nsynchronized(lockB) {\n    synchronized(lockA) {\n        // work\n    }\n}",
            "Both threads complete",
            "Potential deadlock - threads wait for each other",
            "Always acquire locks in the same order",
            "// Thread 1 and Thread 2 - same order\nsynchronized(lockA) {\n    synchronized(lockB) {\n        // work\n    }\n}", false));
        
        dao.insert(new Bug(704, "Resource Leak", "Java", "Hard", "Resources",
            "Read file and handle exceptions",
            "FileInputStream fis = new FileInputStream(\"file.txt\");\nint data = fis.read();\nfis.close();",
            "File is always closed",
            "File not closed if exception occurs before close()",
            "Use try-with-resources for automatic resource management",
            "try (FileInputStream fis = new FileInputStream(\"file.txt\")) {\n    int data = fis.read();\n}", false));
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
