package com.example.debugappproject.data.seeding;

import android.content.Context;

import com.example.debugappproject.data.local.BugDao;
import com.example.debugappproject.data.local.DebugMasterDatabase;
import com.example.debugappproject.model.Bug;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - DOMAIN-SPECIFIC BUG SEEDER                           ║
 * ║           Security, Database, ML, Web, API Bug Exercises                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class DomainSpecificBugSeeder {

    private final Context context;
    private final ExecutorService executor;
    
    public DomainSpecificBugSeeder(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public void seedAllDomainBugs() {
        executor.execute(() -> {
            BugDao dao = DebugMasterDatabase.getInstance(context).bugDao();
            seedSecurityBugs(dao);
            seedDatabaseBugs(dao);
            seedWebBugs(dao);
            seedAPIBugs(dao);
            seedMLBugs(dao);
        });
    }
    
    private void seedSecurityBugs(BugDao dao) {
        // Security Bugs (800-819)
        dao.insert(new Bug(800, "SQL Injection Vulnerability", "Java", "Hard", "Security",
            "Secure this database query against SQL injection",
            "public List<User> search(String name) {\n    String sql = \"SELECT * FROM users WHERE name = '\" + name + \"'\";\n    return jdbcTemplate.query(sql, userMapper);\n}",
            "Safe database query",
            "Vulnerable to SQL injection: input ' OR '1'='1 returns all users",
            "Never concatenate user input into SQL. Use parameterized queries with PreparedStatement or named parameters",
            "public List<User> search(String name) {\n    String sql = \"SELECT * FROM users WHERE name = ?\";\n    return jdbcTemplate.query(sql, userMapper, name);\n}", false));
        
        dao.insert(new Bug(801, "XSS Vulnerability", "JavaScript", "Hard", "Security",
            "Prevent cross-site scripting in user display",
            "function displayUser(user) {\n    document.getElementById('name').innerHTML = user.name;\n    document.getElementById('bio').innerHTML = user.bio;\n}",
            "Safe display of user data",
            "XSS: Malicious script in user.name executes in browser",
            "Never use innerHTML with untrusted data. Use textContent or sanitize input",
            "function displayUser(user) {\n    document.getElementById('name').textContent = user.name;\n    document.getElementById('bio').textContent = user.bio;\n}", false));
        
        dao.insert(new Bug(802, "Insecure Password Storage", "Java", "Hard", "Security",
            "Fix insecure password storage",
            "public void saveUser(String username, String password) {\n    user.setPassword(password); // Storing plaintext!\n    userRepository.save(user);\n}",
            "Secure password storage",
            "Passwords stored in plaintext - instant compromise if DB leaked",
            "Always hash passwords with bcrypt, scrypt, or Argon2. Never store plaintext",
            "public void saveUser(String username, String password) {\n    String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));\n    user.setPassword(hashed);\n    userRepository.save(user);\n}", false));
        
        dao.insert(new Bug(803, "Path Traversal", "Java", "Hard", "Security",
            "Fix path traversal vulnerability",
            "public File getFile(String filename) {\n    return new File(\"/uploads/\" + filename);\n}",
            "Safe file access",
            "Path traversal: filename='../../../etc/passwd' reads system files",
            "Validate and sanitize file paths. Use canonical paths and whitelist allowed directories",
            "public File getFile(String filename) {\n    File file = new File(\"/uploads/\", filename).getCanonicalFile();\n    if (!file.getPath().startsWith(\"/uploads/\")) {\n        throw new SecurityException(\"Invalid path\");\n    }\n    return file;\n}", false));
        
        dao.insert(new Bug(804, "CSRF Vulnerability", "Java", "Hard", "Security",
            "Add CSRF protection to form submission",
            "@PostMapping(\"/transfer\")\npublic String transfer(@RequestParam Long to, @RequestParam BigDecimal amount) {\n    accountService.transfer(getCurrentUser(), to, amount);\n    return \"success\";\n}",
            "Protected against CSRF attacks",
            "CSRF: Attacker's page can submit this form on behalf of logged-in user",
            "Use CSRF tokens and validate Origin/Referer headers",
            "@PostMapping(\"/transfer\")\npublic String transfer(@RequestParam Long to, @RequestParam BigDecimal amount,\n        @RequestParam String _csrf, HttpServletRequest request) {\n    if (!csrfTokenManager.validate(request, _csrf)) {\n        throw new SecurityException(\"Invalid CSRF token\");\n    }\n    accountService.transfer(getCurrentUser(), to, amount);\n    return \"success\";\n}", false));
    }
    
    private void seedDatabaseBugs(BugDao dao) {
        // Database Bugs (820-839)
        dao.insert(new Bug(820, "N+1 Query Problem", "Java", "Hard", "Database",
            "Fix N+1 query performance issue",
            "public List<OrderDTO> getOrders() {\n    List<Order> orders = orderRepository.findAll();\n    return orders.stream()\n        .map(o -> new OrderDTO(o, o.getItems())) // Each getItems() = 1 query!\n        .collect(toList());\n}",
            "Single efficient query",
            "N+1 problem: 1 query for orders + N queries for items",
            "Use JOIN FETCH or @EntityGraph to load relationships in single query",
            "public List<OrderDTO> getOrders() {\n    List<Order> orders = orderRepository.findAllWithItems(); // JOIN FETCH\n    return orders.stream()\n        .map(o -> new OrderDTO(o, o.getItems()))\n        .collect(toList());\n}", false));
        
        dao.insert(new Bug(821, "Missing Index", "SQL", "Medium", "Database",
            "Add proper indexing for this slow query",
            "-- Query takes 30 seconds on large table\nSELECT * FROM orders \nWHERE customer_id = 12345 \nAND status = 'PENDING'\nORDER BY created_at DESC;",
            "Query executes in milliseconds",
            "Full table scan - no indexes on filter columns",
            "Create composite index on frequently filtered/sorted columns",
            "-- Add composite index\nCREATE INDEX idx_orders_customer_status_date \nON orders(customer_id, status, created_at DESC);\n\nSELECT * FROM orders \nWHERE customer_id = 12345 \nAND status = 'PENDING'\nORDER BY created_at DESC;", false));
        
        dao.insert(new Bug(822, "Connection Leak", "Java", "Hard", "Database",
            "Fix database connection leak",
            "public User getUser(Long id) {\n    Connection conn = dataSource.getConnection();\n    PreparedStatement ps = conn.prepareStatement(\"SELECT * FROM users WHERE id = ?\");\n    ps.setLong(1, id);\n    ResultSet rs = ps.executeQuery();\n    if (rs.next()) {\n        return mapUser(rs);\n    }\n    return null;\n    // Connection never closed!\n}",
            "Connections properly released",
            "Connection leak: connections exhaust pool, app freezes",
            "Use try-with-resources to ensure connections are always closed",
            "public User getUser(Long id) {\n    try (Connection conn = dataSource.getConnection();\n         PreparedStatement ps = conn.prepareStatement(\"SELECT * FROM users WHERE id = ?\")) {\n        ps.setLong(1, id);\n        try (ResultSet rs = ps.executeQuery()) {\n            if (rs.next()) {\n                return mapUser(rs);\n            }\n        }\n    }\n    return null;\n}", false));
        
        dao.insert(new Bug(823, "Transaction Isolation Bug", "Java", "Hard", "Database",
            "Fix race condition in inventory update",
            "@Transactional\npublic void purchase(Long productId, int qty) {\n    Product p = productRepo.findById(productId);\n    if (p.getStock() >= qty) {\n        p.setStock(p.getStock() - qty);\n        productRepo.save(p);\n    }\n}",
            "Concurrent purchases handled correctly",
            "Race condition: two users can buy last item simultaneously",
            "Use pessimistic locking or optimistic locking with version field",
            "@Transactional\npublic void purchase(Long productId, int qty) {\n    Product p = productRepo.findByIdWithLock(productId); // SELECT FOR UPDATE\n    if (p.getStock() >= qty) {\n        p.setStock(p.getStock() - qty);\n        productRepo.save(p);\n    } else {\n        throw new OutOfStockException();\n    }\n}", false));
    }
    
    private void seedWebBugs(BugDao dao) {
        // Web/Frontend Bugs (840-859)
        dao.insert(new Bug(840, "Memory Leak in React", "JavaScript", "Hard", "Web",
            "Fix memory leak from event listener",
            "function ChatRoom({ roomId }) {\n    const [messages, setMessages] = useState([]);\n    \n    useEffect(() => {\n        const socket = createConnection(roomId);\n        socket.on('message', msg => setMessages(m => [...m, msg]));\n        // Missing cleanup!\n    }, [roomId]);\n    \n    return <MessageList messages={messages} />;\n}",
            "No memory leak when component unmounts",
            "Memory leak: socket connection persists after unmount",
            "Always return cleanup function from useEffect",
            "function ChatRoom({ roomId }) {\n    const [messages, setMessages] = useState([]);\n    \n    useEffect(() => {\n        const socket = createConnection(roomId);\n        socket.on('message', msg => setMessages(m => [...m, msg]));\n        \n        return () => socket.disconnect(); // Cleanup!\n    }, [roomId]);\n    \n    return <MessageList messages={messages} />;\n}", false));
        
        dao.insert(new Bug(841, "Stale Closure Bug", "JavaScript", "Medium", "Web",
            "Fix stale closure in interval",
            "function Counter() {\n    const [count, setCount] = useState(0);\n    \n    useEffect(() => {\n        const id = setInterval(() => {\n            setCount(count + 1); // Always uses initial count!\n        }, 1000);\n        return () => clearInterval(id);\n    }, []);\n    \n    return <span>{count}</span>;\n}",
            "Counter increments correctly every second",
            "Stale closure: count is always 0 inside interval",
            "Use functional update form to access latest state",
            "function Counter() {\n    const [count, setCount] = useState(0);\n    \n    useEffect(() => {\n        const id = setInterval(() => {\n            setCount(c => c + 1); // Functional update\n        }, 1000);\n        return () => clearInterval(id);\n    }, []);\n    \n    return <span>{count}</span>;\n}", false));
        
        dao.insert(new Bug(842, "CSS Specificity Issue", "CSS", "Easy", "Web",
            "Fix button color not applying",
            ".btn { color: blue; }\n.container .btn { color: red; }\n.primary { color: green !important; }\n\n/* In HTML: <div class='container'><button class='btn primary'>Click</button></div> */\n/* Expected: green, but it shows... */",
            "Button text is green",
            "!important overrides everything but order matters with equal specificity",
            "This actually works! The bug is a trick question - !important wins",
            ".btn { color: blue; }\n.container .btn { color: red; }\n.primary { color: green !important; }\n\n/* Button IS green - !important has highest priority */\n/* Real fix: avoid !important, use proper specificity */", false));
    }
    
    private void seedAPIBugs(BugDao dao) {
        // API/Backend Bugs (860-879)
        dao.insert(new Bug(860, "REST Idempotency Bug", "Java", "Medium", "API",
            "Make this payment endpoint idempotent",
            "@PostMapping(\"/payments\")\npublic Payment createPayment(@RequestBody PaymentRequest req) {\n    Payment payment = new Payment(req.getAmount(), req.getTo());\n    return paymentService.process(payment);\n    // Network retry = duplicate payment!\n}",
            "Retries don't create duplicate payments",
            "Not idempotent: retry creates duplicate payment",
            "Use idempotency key to detect and handle retries",
            "@PostMapping(\"/payments\")\npublic Payment createPayment(\n        @RequestBody PaymentRequest req,\n        @RequestHeader(\"Idempotency-Key\") String idempotencyKey) {\n    Payment existing = paymentService.findByIdempotencyKey(idempotencyKey);\n    if (existing != null) return existing;\n    \n    Payment payment = new Payment(req.getAmount(), req.getTo(), idempotencyKey);\n    return paymentService.process(payment);\n}", false));
        
        dao.insert(new Bug(861, "Rate Limiting Missing", "Java", "Medium", "API",
            "Add rate limiting to prevent abuse",
            "@GetMapping(\"/search\")\npublic List<Result> search(@RequestParam String q) {\n    return searchService.search(q);\n    // No rate limiting!\n}",
            "API protected against abuse",
            "No rate limiting: vulnerable to DoS, scraping, brute force",
            "Implement rate limiting per IP or user",
            "@GetMapping(\"/search\")\n@RateLimited(requests = 100, period = \"1m\")\npublic List<Result> search(\n        @RequestParam String q,\n        HttpServletRequest request) {\n    String clientIp = request.getRemoteAddr();\n    if (rateLimiter.isLimitExceeded(clientIp)) {\n        throw new TooManyRequestsException();\n    }\n    return searchService.search(q);\n}", false));
        
        dao.insert(new Bug(862, "Broken Pagination", "Java", "Easy", "API",
            "Fix offset-based pagination for large datasets",
            "@GetMapping(\"/users\")\npublic List<User> getUsers(@RequestParam int page, @RequestParam int size) {\n    int offset = page * size;\n    return userRepo.findAll(offset, size);\n    // Slow for large offsets!\n}",
            "Fast pagination for any page number",
            "Offset pagination is O(n) - page 10000 scans 10000 rows",
            "Use cursor-based (keyset) pagination for large datasets",
            "@GetMapping(\"/users\")\npublic Page<User> getUsers(\n        @RequestParam(required = false) Long afterId,\n        @RequestParam int size) {\n    if (afterId == null) {\n        return userRepo.findFirstPage(size);\n    }\n    return userRepo.findAfterId(afterId, size); // WHERE id > afterId\n}", false));
    }
    
    private void seedMLBugs(BugDao dao) {
        // ML/Data Science Bugs (880-899)
        dao.insert(new Bug(880, "Data Leakage", "Python", "Hard", "ML",
            "Fix data leakage in preprocessing",
            "# Preprocessing before train/test split\nfrom sklearn.preprocessing import StandardScaler\n\nscaler = StandardScaler()\nX_scaled = scaler.fit_transform(X)  # Fit on ALL data!\n\nX_train, X_test = train_test_split(X_scaled)\nmodel.fit(X_train, y_train)\nprint(model.score(X_test, y_test))  # Overly optimistic!",
            "Realistic test accuracy",
            "Data leakage: scaler learned from test data, inflating accuracy",
            "Fit preprocessing only on training data",
            "from sklearn.preprocessing import StandardScaler\n\nX_train, X_test, y_train, y_test = train_test_split(X, y)\n\nscaler = StandardScaler()\nX_train_scaled = scaler.fit_transform(X_train)  # Fit only on train\nX_test_scaled = scaler.transform(X_test)  # Transform only\n\nmodel.fit(X_train_scaled, y_train)\nprint(model.score(X_test_scaled, y_test))", false));
        
        dao.insert(new Bug(881, "Imbalanced Classes", "Python", "Medium", "ML",
            "Handle imbalanced classification",
            "# 95% negative, 5% positive\nmodel = LogisticRegression()\nmodel.fit(X_train, y_train)\nprint(f'Accuracy: {model.score(X_test, y_test)}')  # 95% but useless!",
            "Model actually detects positive class",
            "Accuracy is misleading with imbalanced data - predicting all negative gives 95%",
            "Use class weights, oversampling, or appropriate metrics (F1, AUC)",
            "from sklearn.metrics import classification_report, f1_score\nfrom imblearn.over_sampling import SMOTE\n\nsmote = SMOTE()\nX_resampled, y_resampled = smote.fit_resample(X_train, y_train)\n\nmodel = LogisticRegression(class_weight='balanced')\nmodel.fit(X_resampled, y_resampled)\n\nprint(classification_report(y_test, model.predict(X_test)))\nprint(f'F1: {f1_score(y_test, model.predict(X_test))}')", false));
        
        dao.insert(new Bug(882, "Gradient Explosion", "Python", "Hard", "ML",
            "Fix exploding gradients in RNN",
            "import torch.nn as nn\n\nclass SimpleRNN(nn.Module):\n    def __init__(self, input_size, hidden_size, output_size):\n        super().__init__()\n        self.rnn = nn.RNN(input_size, hidden_size, num_layers=10)\n        self.fc = nn.Linear(hidden_size, output_size)\n    \n    def forward(self, x):\n        out, _ = self.rnn(x)\n        return self.fc(out[:, -1, :])\n# Loss becomes NaN after few epochs!",
            "Stable training without NaN",
            "Exploding gradients in deep RNN - weights grow unboundedly",
            "Use gradient clipping and LSTM/GRU instead of vanilla RNN",
            "import torch.nn as nn\n\nclass StableRNN(nn.Module):\n    def __init__(self, input_size, hidden_size, output_size):\n        super().__init__()\n        self.lstm = nn.LSTM(input_size, hidden_size, num_layers=3,\n                           dropout=0.2, batch_first=True)  # LSTM + dropout\n        self.fc = nn.Linear(hidden_size, output_size)\n    \n    def forward(self, x):\n        out, _ = self.lstm(x)\n        return self.fc(out[:, -1, :])\n\n# In training loop:\nnn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)", false));
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
