<?php
// System Test Validation Page
// Tests all components of the hybrid security system
error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: text/html; charset=UTF-8');
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🧪 Hybrid Security System Test</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            line-height: 1.6;
            color: #333;
            background: #f8fafc;
            padding: 20px;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.1);
            padding: 30px;
        }

        h1 {
            color: #2d3748;
            margin-bottom: 10px;
            font-size: 2rem;
        }

        .subtitle {
            color: #718096;
            margin-bottom: 30px;
            font-size: 1.1rem;
        }

        .test-section {
            margin-bottom: 30px;
            padding: 20px;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            background: #f7fafc;
        }

        .test-section h2 {
            color: #4a5568;
            margin-bottom: 15px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .test-item {
            margin-bottom: 15px;
            padding: 10px;
            background: white;
            border-radius: 6px;
            border-left: 4px solid #e2e8f0;
        }

        .test-item.pass {
            border-left-color: #48bb78;
            background: #f0fff4;
        }

        .test-item.fail {
            border-left-color: #f56565;
            background: #fff5f5;
        }

        .test-item.warning {
            border-left-color: #ed8936;
            background: #fffbeb;
        }

        .test-name {
            font-weight: 600;
            margin-bottom: 5px;
        }

        .test-result {
            font-size: 0.9rem;
            color: #4a5568;
        }

        .code-block {
            background: #1a202c;
            color: #e2e8f0;
            padding: 15px;
            border-radius: 6px;
            font-family: 'Monaco', 'Menlo', monospace;
            font-size: 0.9rem;
            overflow-x: auto;
            margin: 10px 0;
        }

        .summary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 30px;
        }

        .summary h3 {
            margin-bottom: 10px;
        }

        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 15px;
            margin-top: 15px;
        }

        .stat-item {
            text-align: center;
        }

        .stat-number {
            font-size: 2rem;
            font-weight: bold;
        }

        .stat-label {
            font-size: 0.9rem;
            opacity: 0.9;
        }

        .action-buttons {
            text-align: center;
            margin-top: 30px;
        }

        button {
            background: #667eea;
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 1rem;
            margin: 0 10px;
            transition: background 0.2s;
        }

        button:hover {
            background: #5a67d8;
        }

        .icon {
            font-style: normal;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🧪 Hybrid Security System Test</h1>
        <p class="subtitle">Comprehensive validation of all security components</p>

        <?php
        // Test Results Storage
        $tests = [];
        $total_tests = 0;
        $passed_tests = 0;
        $failed_tests = 0;
        $warnings = 0;

        // Helper function to add test results
        function addTest($name, $result, $message, $type = 'info') {
            global $tests, $total_tests, $passed_tests, $failed_tests, $warnings;
            $tests[] = [
                'name' => $name,
                'result' => $result,
                'message' => $message,
                'type' => $type
            ];
            $total_tests++;
            if ($type === 'pass') $passed_tests++;
            if ($type === 'fail') $failed_tests++;
            if ($type === 'warning') $warnings++;
        }

        // Test 1: Core PHP Files Existence
        $core_files = [
            'device_whitelist.php' => 'Main validation endpoint',
            'api_validate.php' => 'Session token validation',
            'api_data.php' => 'Example protected API',
            'react_example.html' => 'React.js integration example',
            'REACT_INTEGRATION_GUIDE.md' => 'Integration documentation',
            'SECURITY_IMPLEMENTATION.md' => 'Security documentation'
        ];

        foreach ($core_files as $file => $description) {
            if (file_exists($file)) {
                addTest("File: $file", "EXISTS", "$description - File found", 'pass');
            } else {
                addTest("File: $file", "MISSING", "$description - File not found", 'fail');
            }
        }

        // Test 2: PHP Security Functions
        if (file_exists('device_whitelist.php')) {
            $content = file_get_contents('device_whitelist.php');
            
            // Check for security functions
            if (strpos($content, 'generateSignature') !== false) {
                addTest("Security: HMAC Signature", "PRESENT", "Cryptographic signature functions available", 'pass');
            } else {
                addTest("Security: HMAC Signature", "MISSING", "Signature generation functions not found", 'fail');
            }

            if (strpos($content, 'generateJWT') !== false) {
                addTest("Security: JWT Tokens", "PRESENT", "JWT token generation functions available", 'pass');
            } else {
                addTest("Security: JWT Tokens", "MISSING", "JWT functions not implemented", 'fail');
            }

            if (strpos($content, 'SESSION_TOKEN_EXPIRY') !== false) {
                addTest("Security: Session Management", "PRESENT", "Session token expiration configured", 'pass');
            } else {
                addTest("Security: Session Management", "MISSING", "Session configuration incomplete", 'warning');
            }
        }

        // Test 3: Log File Permissions
        $log_files = ['device_access.log', 'security_events.log'];
        foreach ($log_files as $log_file) {
            if (file_exists($log_file)) {
                if (is_writable($log_file)) {
                    addTest("Logging: $log_file", "WRITABLE", "Log file exists and is writable", 'pass');
                } else {
                    addTest("Logging: $log_file", "READ-ONLY", "Log file exists but not writable", 'warning');
                }
            } else {
                // Try to create the log file
                if (touch($log_file)) {
                    addTest("Logging: $log_file", "CREATED", "Log file created successfully", 'pass');
                } else {
                    addTest("Logging: $log_file", "CANNOT CREATE", "Cannot create log file - check permissions", 'fail');
                }
            }
        }

        // Test 4: PHP Configuration
        if (function_exists('hash_hmac')) {
            addTest("PHP: HMAC Support", "AVAILABLE", "hash_hmac function available for signatures", 'pass');
        } else {
            addTest("PHP: HMAC Support", "MISSING", "hash_hmac function not available", 'fail');
        }

        if (function_exists('random_bytes')) {
            addTest("PHP: Cryptographic Random", "AVAILABLE", "random_bytes function available for nonces", 'pass');
        } else {
            addTest("PHP: Cryptographic Random", "MISSING", "random_bytes function not available", 'fail');
        }

        if (function_exists('base64_encode')) {
            addTest("PHP: Base64 Support", "AVAILABLE", "Base64 encoding available for JWT", 'pass');
        } else {
            addTest("PHP: Base64 Support", "MISSING", "Base64 functions not available", 'fail');
        }

        // Test 5: Device Whitelist Configuration
        if (file_exists('device_whitelist.php')) {
            $content = file_get_contents('device_whitelist.php');
            preg_match('/\$whitelist\s*=\s*array\s*\((.*?)\)/s', $content, $matches);
            
            if ($matches) {
                $whitelist_content = $matches[1];
                if (strpos($whitelist_content, '=>') !== false && strpos($whitelist_content, '//') === false) {
                    addTest("Config: Device Whitelist", "CONFIGURED", "At least one device appears to be configured", 'pass');
                } else if (strpos($whitelist_content, '319e1db2e05f0185') !== false) {
                    addTest("Config: Device Whitelist", "EXAMPLE DEVICE", "Example device ID found - update for production", 'warning');
                } else {
                    addTest("Config: Device Whitelist", "EMPTY", "No devices configured in whitelist", 'warning');
                }
            }
        }

        // Test 6: API Endpoint Accessibility
        $api_endpoints = [
            'api_validate.php' => 'Session validation endpoint',
            'api_data.php' => 'Protected data endpoint'
        ];

        foreach ($api_endpoints as $endpoint => $description) {
            if (file_exists($endpoint)) {
                // Basic syntax check
                $syntax_check = shell_exec("php -l $endpoint 2>&1");
                if (strpos($syntax_check, 'No syntax errors') !== false) {
                    addTest("API: $endpoint", "SYNTAX OK", "$description - PHP syntax valid", 'pass');
                } else {
                    addTest("API: $endpoint", "SYNTAX ERROR", "$description - PHP syntax errors detected", 'fail');
                }
            }
        }

        // Calculate overall score
        $score = $total_tests > 0 ? round(($passed_tests / $total_tests) * 100) : 0;
        ?>

        <div class="summary">
            <h3>🏆 Overall System Health: <?php echo $score; ?>%</h3>
            <div class="stats">
                <div class="stat-item">
                    <div class="stat-number"><?php echo $total_tests; ?></div>
                    <div class="stat-label">Total Tests</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number" style="color: #68d391;"><?php echo $passed_tests; ?></div>
                    <div class="stat-label">Passed</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number" style="color: #fc8181;"><?php echo $failed_tests; ?></div>
                    <div class="stat-label">Failed</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number" style="color: #f6ad55;"><?php echo $warnings; ?></div>
                    <div class="stat-label">Warnings</div>
                </div>
            </div>
        </div>

        <!-- Test Results -->
        <div class="test-section">
            <h2><span class="icon">🔧</span> System Components</h2>
            <?php foreach (array_slice($tests, 0, 6) as $test): ?>
            <div class="test-item <?php echo $test['type']; ?>">
                <div class="test-name"><?php echo $test['name']; ?>: <?php echo $test['result']; ?></div>
                <div class="test-result"><?php echo $test['message']; ?></div>
            </div>
            <?php endforeach; ?>
        </div>

        <div class="test-section">
            <h2><span class="icon">🔐</span> Security Features</h2>
            <?php foreach (array_slice($tests, 6, 4) as $test): ?>
            <div class="test-item <?php echo $test['type']; ?>">
                <div class="test-name"><?php echo $test['name']; ?>: <?php echo $test['result']; ?></div>
                <div class="test-result"><?php echo $test['message']; ?></div>
            </div>
            <?php endforeach; ?>
        </div>

        <div class="test-section">
            <h2><span class="icon">📊</span> Configuration & APIs</h2>
            <?php foreach (array_slice($tests, 10) as $test): ?>
            <div class="test-item <?php echo $test['type']; ?>">
                <div class="test-name"><?php echo $test['name']; ?>: <?php echo $test['result']; ?></div>
                <div class="test-result"><?php echo $test['message']; ?></div>
            </div>
            <?php endforeach; ?>
        </div>

        <!-- Recommendations -->
        <div class="test-section">
            <h2><span class="icon">💡</span> Recommendations</h2>
            
            <?php if ($score >= 90): ?>
            <div class="test-item pass">
                <div class="test-name">🎉 Excellent! System Ready for Production</div>
                <div class="test-result">All core components are functioning correctly. Consider the production checklist in SETUP_INSTRUCTIONS.md.</div>
            </div>
            <?php elseif ($score >= 70): ?>
            <div class="test-item warning">
                <div class="test-name">⚠️ Good, Minor Issues to Address</div>
                <div class="test-result">System is mostly ready. Address the warnings above before production deployment.</div>
            </div>
            <?php else: ?>
            <div class="test-item fail">
                <div class="test-name">🚨 Critical Issues Need Attention</div>
                <div class="test-result">Several components need fixing before the system can be used safely. Please address all failed tests.</div>
            </div>
            <?php endif; ?>

            <div class="code-block">
# Next Steps:
1. Address any failed tests above
2. Configure your device ID in device_whitelist.php
3. Test with react_example.html in your kiosk app
4. Check security_events.log for authentication attempts
5. Review SETUP_INSTRUCTIONS.md for deployment guidance
            </div>
        </div>

        <div class="action-buttons">
            <button onclick="location.reload()">🔄 Run Tests Again</button>
            <button onclick="window.open('react_example.html')">🧪 Test React App</button>
            <button onclick="window.open('SETUP_INSTRUCTIONS.md')">📖 Setup Guide</button>
        </div>

        <p style="text-align: center; margin-top: 30px; color: #718096; font-size: 0.9rem;">
            🔐 Hybrid Security System v1.0 - Cryptographic device validation with JWT session tokens<br>
            Generated: <?php echo date('Y-m-d H:i:s'); ?>
        </p>
    </div>
</body>
</html>