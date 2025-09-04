<?php
// Clean device validation script
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

try {
    // Device whitelist
    $whitelisted_devices = array(
        'abc123def456' => array('name' => 'Test Device 1', 'location' => 'Office'),
        'ff7fca15cf0b74c5' => array('name' => 'Samsung Galaxy A35', 'location' => 'Test Device')
    );

    // Get device ID from headers
    $device_id = isset($_SERVER['HTTP_X_DEVICE_ID']) ? $_SERVER['HTTP_X_DEVICE_ID'] : null;
    $device_info = isset($_SERVER['HTTP_X_DEVICE_INFO']) ? $_SERVER['HTTP_X_DEVICE_INFO'] : 'Unknown';
    $app_version = isset($_SERVER['HTTP_X_APP_VERSION']) ? $_SERVER['HTTP_X_APP_VERSION'] : 'Unknown';

    // Log the request
    $log_message = date('Y-m-d H:i:s') . " - Device: " . ($device_id ? $device_id : 'null') . " Info: " . $device_info . "\n";
    file_put_contents('device_access.log', $log_message, FILE_APPEND | LOCK_EX);

    // Check if device ID exists
    if (!$device_id) {
        http_response_code(400);
        echo json_encode(array(
            'status' => 'error',
            'message' => 'No device ID provided',
            'access_granted' => false
        ));
        exit;
    }

    // Check if device is whitelisted
    if (array_key_exists($device_id, $whitelisted_devices)) {
        // AUTHORIZED
        http_response_code(200);
        echo json_encode(array(
            'status' => 'success',
            'message' => 'Device authorized',
            'access_granted' => true,
            'device_id' => $device_id,
            'device_info' => $whitelisted_devices[$device_id]
        ));
    } else {
        // NOT AUTHORIZED
        http_response_code(403);
        echo json_encode(array(
            'status' => 'unauthorized',
            'message' => 'Device not whitelisted',
            'access_granted' => false,
            'device_id' => $device_id,
            'device_info' => $device_info
        ));
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(array(
        'status' => 'error',
        'message' => 'Server error: ' . $e->getMessage(),
        'access_granted' => false
    ));
}
?>