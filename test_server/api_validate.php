<?php
// Session Token Validation API for React.js Applications
// This endpoint validates JWT session tokens for authenticated API calls
ini_set('display_errors', 0);
error_reporting(0);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Authorization, Content-Type');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');

// Import JWT configuration from main validation script
define('JWT_SECRET', 'kiosk-session-token-secret-2025-v1');
define('SECURITY_LOG_FILE', 'security_events.log');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    echo '{"status":"preflight_ok"}';
    exit;
}

// JWT Token Functions (shared with device_whitelist.php)
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

function logSecurityEvent($event, $details = []) {
    $log_entry = [
        'timestamp' => date('Y-m-d H:i:s'),
        'event' => $event,
        'ip' => $_SERVER['REMOTE_ADDR'] ?? 'unknown',
        'user_agent' => $_SERVER['HTTP_USER_AGENT'] ?? 'unknown',
        'details' => $details
    ];
    @file_put_contents(SECURITY_LOG_FILE, json_encode($log_entry) . "\n", FILE_APPEND);
}

// Get Authorization header
$headers = getallheaders();
$auth_header = $headers['Authorization'] ?? $_SERVER['HTTP_AUTHORIZATION'] ?? '';

if (empty($auth_header)) {
    logSecurityEvent('missing_authorization_header', [
        'endpoint' => 'api_validate',
        'user_agent' => $_SERVER['HTTP_USER_AGENT'] ?? 'unknown'
    ]);
    
    http_response_code(401);
    echo json_encode([
        'status' => 'error',
        'message' => 'Authorization header required',
        'authenticated' => false,
        'error_code' => 'MISSING_AUTH_HEADER'
    ]);
    exit;
}

// Extract Bearer token
if (!preg_match('/Bearer\s+(.*)$/i', $auth_header, $matches)) {
    logSecurityEvent('invalid_authorization_format', [
        'endpoint' => 'api_validate',
        'auth_header_format' => substr($auth_header, 0, 20) . '...'
    ]);
    
    http_response_code(401);
    echo json_encode([
        'status' => 'error',
        'message' => 'Invalid authorization format. Use: Bearer <token>',
        'authenticated' => false,
        'error_code' => 'INVALID_AUTH_FORMAT'
    ]);
    exit;
}

$jwt_token = $matches[1];

// Validate JWT token
$token_payload = verifyJWT($jwt_token);

if (!$token_payload) {
    logSecurityEvent('invalid_jwt_token', [
        'endpoint' => 'api_validate',
        'token_preview' => substr($jwt_token, 0, 20) . '...',
        'token_length' => strlen($jwt_token)
    ]);
    
    http_response_code(401);
    echo json_encode([
        'status' => 'error',
        'message' => 'Invalid or expired session token',
        'authenticated' => false,
        'error_code' => 'INVALID_JWT_TOKEN'
    ]);
    exit;
}

// Token is valid - return user information
logSecurityEvent('valid_session_access', [
    'endpoint' => 'api_validate',
    'device_id' => $token_payload['sub'],
    'device_name' => $token_payload['device_name'],
    'token_expires_at' => date('Y-m-d H:i:s', $token_payload['exp'])
]);

// Return successful validation response
echo json_encode([
    'status' => 'success',
    'message' => 'Session token valid',
    'authenticated' => true,
    'device_info' => [
        'device_id' => $token_payload['sub'],
        'device_name' => $token_payload['device_name'],
        'device_location' => $token_payload['device_location'],
        'issued_at' => date('Y-m-d H:i:s', $token_payload['iat']),
        'expires_at' => date('Y-m-d H:i:s', $token_payload['exp']),
        'time_remaining' => $token_payload['exp'] - time()
    ],
    'security_mode' => 'session_token_validated'
]);
?>