<?php
// Secure device validation with cryptographic signature verification
ini_set('display_errors', 0);
error_reporting(0);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: X-Device-ID, X-Device-Info, X-App-Version, X-Timestamp, X-Nonce, X-Signature, Content-Type');
header('Access-Control-Allow-Methods: GET, POST');

// Security configuration
define('SHARED_SECRET', 'kiosk-device-validation-secret-2025-v1');
define('JWT_SECRET', 'kiosk-session-token-secret-2025-v1');
define('TIMESTAMP_TOLERANCE', 5 * 60); // 5 minutes in seconds
define('SESSION_TOKEN_EXPIRY', 24 * 60 * 60); // 24 hours
define('LOG_FILE', 'device_access.log');
define('SECURITY_LOG_FILE', 'security_events.log');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    echo '{"status":"preflight_ok"}';
    exit;
}

// Device whitelist - replace with database lookup in production
$whitelist = array(
    // Development & Testing Devices
    '346501863fc6be9e' => array('name' => 'Samsung Galaxy A35', 'location' => 'Development Lab'),
    // 'a1b2c3d4e5f6g7h8' => array('name' => 'Test Tablet 1', 'location' => 'QA Department'),
    
    // To add devices:
    // 1. Load react_example.html or your React app in the kiosk 
    // 2. Copy device ID from "Access Denied" screen or server logs
    // 3. Uncomment and replace one of the examples above with your device ID
    // 4. Or add new line: 'your_device_id' => array('name' => 'Device Name', 'location' => 'Location'),
);

// Security functions
function generateSignature($payload) {
    return base64_encode(hash_hmac('sha256', $payload, SHARED_SECRET, true));
}

function verifySignature($payload, $signature) {
    $expected = generateSignature($payload);
    return hash_equals($expected, $signature);
}

function isTimestampValid($timestamp) {
    $current_time = time();
    $time_diff = abs($current_time - $timestamp);
    return $time_diff <= TIMESTAMP_TOLERANCE;
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

function createSignedResponse($data) {
    $timestamp = time();
    $nonce = base64_encode(random_bytes(16));
    $response_data = json_encode($data);
    $payload = $response_data . '|' . $timestamp . '|' . $nonce;
    $signature = generateSignature($payload);
    
    return [
        'data' => $data,
        'timestamp' => $timestamp,
        'nonce' => $nonce,
        'signature' => $signature
    ];
}

// JWT Token Functions for Session Management
function base64UrlEncode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

function base64UrlDecode($data) {
    return base64_decode(str_pad(strtr($data, '-_', '+/'), strlen($data) % 4, '=', STR_PAD_RIGHT));
}

function generateJWT($device_id, $device_details) {
    $header = json_encode(['typ' => 'JWT', 'alg' => 'HS256']);
    
    $payload = json_encode([
        'iss' => 'kiosk-launcher',           // Issuer
        'sub' => $device_id,                 // Subject (device ID)
        'device_name' => $device_details['name'],
        'device_location' => $device_details['location'],
        'iat' => time(),                     // Issued at
        'exp' => time() + SESSION_TOKEN_EXPIRY,  // Expires
        'jti' => bin2hex(random_bytes(16))   // JWT ID (unique identifier)
    ]);
    
    $headerEncoded = base64UrlEncode($header);
    $payloadEncoded = base64UrlEncode($payload);
    
    $signature = hash_hmac('sha256', $headerEncoded . '.' . $payloadEncoded, JWT_SECRET, true);
    $signatureEncoded = base64UrlEncode($signature);
    
    return $headerEncoded . '.' . $payloadEncoded . '.' . $signatureEncoded;
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

// Get and validate request headers
$device_id = $_SERVER['HTTP_X_DEVICE_ID'] ?? '';
$device_info = $_SERVER['HTTP_X_DEVICE_INFO'] ?? '';
$app_version = $_SERVER['HTTP_X_APP_VERSION'] ?? '';
$timestamp = intval($_SERVER['HTTP_X_TIMESTAMP'] ?? 0);
$nonce = $_SERVER['HTTP_X_NONCE'] ?? '';
$signature = $_SERVER['HTTP_X_SIGNATURE'] ?? '';

// Validate request signature
if (empty($signature) || empty($timestamp) || empty($nonce)) {
    logSecurityEvent('missing_signature_data', [
        'device_id' => $device_id,
        'has_signature' => !empty($signature),
        'has_timestamp' => !empty($timestamp),
        'has_nonce' => !empty($nonce)
    ]);
    echo json_encode(['status' => 'error', 'message' => 'Missing signature data', 'access_granted' => false]);
    exit;
}

// Verify timestamp to prevent replay attacks
if (!isTimestampValid($timestamp)) {
    logSecurityEvent('invalid_timestamp', [
        'device_id' => $device_id,
        'timestamp' => $timestamp,
        'current_time' => time(),
        'time_diff' => abs(time() - $timestamp)
    ]);
    echo json_encode(['status' => 'error', 'message' => 'Request timestamp invalid', 'access_granted' => false]);
    exit;
}

// Verify request signature
$payload = $device_id . '|' . $device_info . '|' . $app_version . '|' . $timestamp . '|' . $nonce;
if (!verifySignature($payload, $signature)) {
    logSecurityEvent('signature_verification_failed', [
        'device_id' => $device_id,
        'payload_length' => strlen($payload),
        'signature_length' => strlen($signature)
    ]);
    echo json_encode(['status' => 'error', 'message' => 'Invalid signature', 'access_granted' => false]);
    exit;
}

// Log validated request
$log = date('Y-m-d H:i:s') . ' - Validated Device: ' . $device_id . ' (' . $device_info . ')' . "\n";
@file_put_contents(LOG_FILE, $log, FILE_APPEND);

// Process validated request
if (empty($device_id)) {
    $response = createSignedResponse([
        'status' => 'error',
        'message' => 'No device ID provided',
        'access_granted' => false
    ]);
    logSecurityEvent('empty_device_id', ['ip' => $_SERVER['REMOTE_ADDR'] ?? 'unknown']);
    echo json_encode($response);
} else if (isset($whitelist[$device_id])) {
    $device_details = $whitelist[$device_id];
    
    // Generate JWT session token for React.js API calls
    $session_token = generateJWT($device_id, $device_details);
    
    $response = createSignedResponse([
        'status' => 'success',
        'message' => 'Device authorized',
        'access_granted' => true,
        'device_id' => $device_id,
        'device_name' => $device_details['name'],
        'device_location' => $device_details['location'],
        'session_token' => $session_token,
        'token_expires_at' => time() + SESSION_TOKEN_EXPIRY,
        'security_mode' => 'hybrid_cryptographic'
    ]);
    
    logSecurityEvent('device_authorized_with_session', [
        'device_id' => $device_id,
        'device_name' => $device_details['name'],
        'location' => $device_details['location'],
        'session_token_issued' => true,
        'token_expires_at' => date('Y-m-d H:i:s', time() + SESSION_TOKEN_EXPIRY)
    ]);
    
    echo json_encode($response);
} else {
    $response = createSignedResponse([
        'status' => 'unauthorized',
        'message' => 'Device not whitelisted',
        'access_granted' => false,
        'device_id' => $device_id
    ]);
    logSecurityEvent('device_unauthorized', [
        'device_id' => $device_id,
        'device_info' => $device_info
    ]);
    echo json_encode($response);
}
?>