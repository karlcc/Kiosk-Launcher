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
define('TIMESTAMP_TOLERANCE', 5 * 60); // 5 minutes in seconds
define('LOG_FILE', 'device_access.log');
define('SECURITY_LOG_FILE', 'security_events.log');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    echo '{"status":"preflight_ok"}';
    exit;
}

// Device whitelist - replace with database lookup in production
$whitelist = array(
    // Development & Testing Devices
    // 'ff7fca15cf0b74c5' => array('name' => 'Samsung Galaxy A35', 'location' => 'Development Lab'),
    // 'a1b2c3d4e5f6g7h8' => array('name' => 'Test Tablet 1', 'location' => 'QA Department'),
    
    // To add devices:
    // 1. Load simple_validation.html on your device 
    // 2. Copy device ID from "Access Denied" screen
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
    $response = createSignedResponse([
        'status' => 'success',
        'message' => 'Device authorized',
        'access_granted' => true,
        'device_id' => $device_id,
        'device_name' => $device_details['name'],
        'device_location' => $device_details['location']
    ]);
    logSecurityEvent('device_authorized', [
        'device_id' => $device_id,
        'device_name' => $device_details['name'],
        'location' => $device_details['location']
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