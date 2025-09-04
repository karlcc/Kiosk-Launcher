<?php
// device_whitelist.php - PHP 7 compatible with error handling
error_reporting(E_ALL);
ini_set('display_errors', 0); // Don't display errors in output
ini_set('log_errors', 1); // Log errors to file

// Set JSON headers first
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: X-Device-ID, X-Device-Info, X-App-Version, Content-Type');
header('Access-Control-Allow-Methods: GET, POST');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    echo json_encode(['status' => 'preflight_ok']);
    exit;
}

// Function to safely output JSON response
function sendJsonResponse($data, $httpCode = 200) {
    http_response_code($httpCode);
    echo json_encode($data, JSON_PRETTY_PRINT);
    exit;
}

// Error handler that outputs JSON
function handleError($message, $httpCode = 500) {
    sendJsonResponse([
        'status' => 'error',
        'message' => $message,
        'access_granted' => false,
        'server_time' => date('Y-m-d H:i:s')
    ], $httpCode);
}

// Set custom error handler
set_error_handler(function($errno, $errstr, $errfile, $errline) {
    handleError("PHP Error: $errstr in $errfile:$errline");
});

// Set exception handler
set_exception_handler(function($exception) {
    handleError("PHP Exception: " . $exception->getMessage());
});

try {

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
$log_message .= "  Request Method: " . ($_SERVER['REQUEST_METHOD'] ?? 'Unknown') . "\n";
$log_message .= "  Request URI: " . ($_SERVER['REQUEST_URI'] ?? 'Unknown') . "\n";
$log_message .= "  Device ID: " . ($device_id ?? 'null') . "\n";
$log_message .= "  Device Info: $device_info\n";
$log_message .= "  App Version: $app_version\n";
$log_message .= "  User Agent: " . ($_SERVER['HTTP_USER_AGENT'] ?? 'Unknown') . "\n";
$log_message .= "  IP Address: " . ($_SERVER['REMOTE_ADDR'] ?? 'Unknown') . "\n";

// Log all X- headers for debugging
$log_message .= "  Custom Headers:\n";
foreach (getallheaders() as $name => $value) {
    if (stripos($name, 'x-') === 0) {
        $log_message .= "    $name: $value\n";
    }
}
$log_message .= "\n";

    // Write to log file
    if (!file_put_contents('device_access.log', $log_message, FILE_APPEND | LOCK_EX)) {
        error_log("Failed to write to device_access.log");
    }

    if (!$device_id) {
        sendJsonResponse([
            'status' => 'error',
            'message' => 'No device ID provided',
            'access_granted' => false,
            'received_headers' => [
                'X-Device-ID' => $device_id,
                'X-Device-Info' => $device_info,
                'X-App-Version' => $app_version
            ],
            'debug_info' => [
                'request_method' => $_SERVER['REQUEST_METHOD'] ?? 'Unknown',
                'request_uri' => $_SERVER['REQUEST_URI'] ?? 'Unknown',
                'user_agent' => $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown'
            ]
        ], 400);
    }

    // Check if device is whitelisted
    if (array_key_exists($device_id, $whitelisted_devices)) {
        sendJsonResponse([
            'status' => 'success',
            'message' => 'Device authorized',
            'access_granted' => true,
            'device_id' => $device_id,
            'device_info' => $whitelisted_devices[$device_id],
            'server_time' => date('Y-m-d H:i:s')
        ]);
    } else {
        sendJsonResponse([
            'status' => 'unauthorized',
            'message' => 'Device not whitelisted',
            'access_granted' => false,
            'device_id' => $device_id,
            'device_info' => $device_info,
            'server_time' => date('Y-m-d H:i:s'),
            'help' => 'Add your device ID to the whitelist in this PHP file',
            'available_devices' => array_keys($whitelisted_devices)
        ], 403);
    }

} catch (Exception $e) {
    handleError("Unexpected error: " . $e->getMessage());
} catch (Error $e) {
    handleError("Fatal error: " . $e->getMessage());
}
?>