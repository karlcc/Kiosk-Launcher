<?php
// Ultra-simple device validation - guaranteed to return JSON
ini_set('display_errors', 0);
error_reporting(0);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: X-Device-ID, X-Device-Info, X-App-Version, Content-Type');
header('Access-Control-Allow-Methods: GET, POST');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    echo '{"status":"preflight_ok"}';
    exit;
}

// Empty whitelist for new server - no devices authorized
$whitelist = array();

// Get device ID
$device_id = '';
if (isset($_SERVER['HTTP_X_DEVICE_ID'])) {
    $device_id = $_SERVER['HTTP_X_DEVICE_ID'];
}

// Log request
$log = date('Y-m-d H:i:s') . ' - Device: ' . $device_id . "\n";
@file_put_contents('device_access.log', $log, FILE_APPEND);

// Always return JSON response
if (empty($device_id)) {
    echo '{"status":"error","message":"No device ID provided","access_granted":false}';
} else if (isset($whitelist[$device_id])) {
    echo '{"status":"success","message":"Device authorized","access_granted":true,"device_id":"' . $device_id . '"}';
} else {
    echo '{"status":"unauthorized","message":"Device not whitelisted","access_granted":false,"device_id":"' . $device_id . '"}';
}
?>