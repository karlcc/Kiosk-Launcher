<?php
// device_whitelist.php - PHP 7 compatible
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: X-Device-ID, X-Device-Info, X-App-Version, Content-Type');
header('Access-Control-Allow-Methods: GET, POST');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// Device whitelist - replace with database lookup in production
$whitelisted_devices = [
    'abc123def456' => ['name' => 'Kiosk Device 1', 'location' => 'Reception'],
    '789xyz012abc' => ['name' => 'Kiosk Device 2', 'location' => 'Conference Room'],
    // Add your actual device IDs here after testing
];

// Get device information from custom headers
$device_id = $_SERVER['HTTP_X_DEVICE_ID'] ?? null;
$device_info = $_SERVER['HTTP_X_DEVICE_INFO'] ?? 'Unknown';
$app_version = $_SERVER['HTTP_X_APP_VERSION'] ?? 'Unknown';

// Log the request for debugging
$log_message = date('Y-m-d H:i:s') . " - Device access attempt\n";
$log_message .= "  Device ID: " . ($device_id ?? 'null') . "\n";
$log_message .= "  Device Info: $device_info\n";
$log_message .= "  App Version: $app_version\n";
$log_message .= "  User Agent: " . ($_SERVER['HTTP_USER_AGENT'] ?? 'Unknown') . "\n";
$log_message .= "  IP Address: " . ($_SERVER['REMOTE_ADDR'] ?? 'Unknown') . "\n\n";

// Write to log file
file_put_contents('device_access.log', $log_message, FILE_APPEND | LOCK_EX);

if (!$device_id) {
    http_response_code(400);
    echo json_encode([
        'status' => 'error',
        'message' => 'No device ID provided',
        'access_granted' => false,
        'received_headers' => [
            'X-Device-ID' => $device_id,
            'X-Device-Info' => $device_info,
            'X-App-Version' => $app_version
        ]
    ]);
    exit;
}

// Check if device is whitelisted
if (array_key_exists($device_id, $whitelisted_devices)) {
    http_response_code(200);
    echo json_encode([
        'status' => 'success',
        'message' => 'Device authorized',
        'access_granted' => true,
        'device_info' => $whitelisted_devices[$device_id],
        'server_time' => date('Y-m-d H:i:s')
    ]);
} else {
    http_response_code(403);
    echo json_encode([
        'status' => 'unauthorized',
        'message' => 'Device not whitelisted',
        'access_granted' => false,
        'device_id' => $device_id,
        'device_info' => $device_info,
        'server_time' => date('Y-m-d H:i:s'),
        'help' => 'Add your device ID to the whitelist in this PHP file'
    ]);
}
?>