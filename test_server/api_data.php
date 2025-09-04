<?php
// Example Protected API Endpoint for React.js Applications
// This demonstrates how to protect actual API endpoints with session token validation
ini_set('display_errors', 0);
error_reporting(0);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Authorization, Content-Type');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');

// Import JWT configuration
define('JWT_SECRET', 'kiosk-session-token-secret-2025-v1');
define('SECURITY_LOG_FILE', 'security_events.log');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    echo '{"status":"preflight_ok"}';
    exit;
}

// JWT Token Functions (reusable across all API endpoints)
function base64UrlDecode($data) {
    return base64_decode(str_pad(strtr($data, '-_', '+/'), strlen($data) % 4, '=', STR_PAD_RIGHT));
}

function verifyJWT($jwt) {
    $parts = explode('.', $jwt);
    if (count($parts) !== 3) {
        return false;
    }
    
    list($headerEncoded, $payloadEncoded, $signatureEncoded) = $parts;
    
    // Verify signature
    $signature = base64UrlDecode($signatureEncoded);
    $expectedSignature = hash_hmac('sha256', $headerEncoded . '.' . $payloadEncoded, JWT_SECRET, true);
    
    if (!hash_equals($signature, $expectedSignature)) {
        return false;
    }
    
    // Decode payload and check expiration
    $payload = json_decode(base64UrlDecode($payloadEncoded), true);
    if (!$payload || $payload['exp'] < time()) {
        return false;
    }
    
    return $payload;
}

function validateSessionToken() {
    // Get Authorization header
    $headers = getallheaders();
    $auth_header = $headers['Authorization'] ?? $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    
    if (empty($auth_header)) {
        http_response_code(401);
        echo json_encode([
            'status' => 'error',
            'message' => 'Authorization header required',
            'error_code' => 'MISSING_AUTH_HEADER'
        ]);
        exit;
    }
    
    // Extract Bearer token
    if (!preg_match('/Bearer\s+(.*)$/i', $auth_header, $matches)) {
        http_response_code(401);
        echo json_encode([
            'status' => 'error',
            'message' => 'Invalid authorization format',
            'error_code' => 'INVALID_AUTH_FORMAT'
        ]);
        exit;
    }
    
    $jwt_token = $matches[1];
    $token_payload = verifyJWT($jwt_token);
    
    if (!$token_payload) {
        http_response_code(401);
        echo json_encode([
            'status' => 'error',
            'message' => 'Invalid or expired session token',
            'error_code' => 'INVALID_JWT_TOKEN'
        ]);
        exit;
    }
    
    return $token_payload;
}

// Validate session token (this protects the API endpoint)
$user_session = validateSessionToken();

// Example API Data (replace with your actual business logic)
$sample_data = [
    'dashboard' => [
        'device_status' => 'online',
        'last_activity' => date('Y-m-d H:i:s'),
        'uptime' => '24 hours, 32 minutes',
        'memory_usage' => '65%',
        'storage_available' => '2.4 GB'
    ],
    'notifications' => [
        [
            'id' => 1,
            'type' => 'system',
            'message' => 'System update available',
            'timestamp' => date('Y-m-d H:i:s', time() - 3600),
            'read' => false
        ],
        [
            'id' => 2,
            'type' => 'security',
            'message' => 'Device authenticated successfully',
            'timestamp' => date('Y-m-d H:i:s', time() - 300),
            'read' => true
        ]
    ],
    'settings' => [
        'theme' => 'dark',
        'language' => 'en',
        'auto_updates' => true,
        'notifications_enabled' => true
    ]
];

// Add device-specific information from the session token
$response_data = [
    'status' => 'success',
    'message' => 'Data retrieved successfully',
    'session_info' => [
        'device_id' => $user_session['sub'],
        'device_name' => $user_session['device_name'],
        'device_location' => $user_session['device_location'],
        'session_expires_in' => $user_session['exp'] - time()
    ],
    'data' => $sample_data,
    'timestamp' => date('Y-m-d H:i:s'),
    'api_version' => '1.0',
    'security_mode' => 'jwt_protected'
];

// Return the protected data
echo json_encode($response_data);
?>