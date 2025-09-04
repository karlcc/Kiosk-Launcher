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

// Device whitelist - replace with database lookup in production
$whitelist = array(
    // Development & Testing Devices
    // 'ff7fca15cf0b74c5' => array('name' => 'Samsung Galaxy A35', 'location' => 'Development Lab'),
    // 'a1b2c3d4e5f6g7h8' => array('name' => 'Test Tablet 1', 'location' => 'QA Department'),
    
    // Reception & Front Desk Examples
    // '1234567890abcdef' => array('name' => 'Reception Kiosk', 'location' => 'Main Lobby'),
    // 'fedcba0987654321' => array('name' => 'Visitor Check-in', 'location' => 'Security Desk'),
    // '555888999000111' => array('name' => 'Guest Registration', 'location' => 'Front Entrance'),
    
    // Conference & Meeting Rooms Examples  
    // 'conf001meeting01' => array('name' => 'Conference Room A Display', 'location' => 'Meeting Room A'),
    // 'conf002meeting02' => array('name' => 'Conference Room B Display', 'location' => 'Meeting Room B'),
    // 'conf003meeting03' => array('name' => 'Boardroom Display', 'location' => 'Executive Boardroom'),
    
    // Retail & Customer Service Examples
    // 'retail001pos001' => array('name' => 'Product Info Kiosk', 'location' => 'Retail Floor Section A'),
    // 'retail002pos002' => array('name' => 'Price Check Station', 'location' => 'Retail Floor Section B'),
    // 'retail003pos003' => array('name' => 'Customer Service', 'location' => 'Service Counter'),
    
    // Healthcare Examples
    // 'medical001patien' => array('name' => 'Patient Check-in', 'location' => 'Hospital Reception'),
    // 'medical002waitin' => array('name' => 'Waiting Room Info', 'location' => 'Waiting Area'),
    
    // Education Examples
    // 'edu001library001' => array('name' => 'Library Catalog', 'location' => 'Main Library'),
    // 'edu002student001' => array('name' => 'Student Services', 'location' => 'Student Center'),
    
    // Corporate Examples
    // 'corp001employee1' => array('name' => 'Employee Directory', 'location' => 'Corporate Lobby'),
    // 'corp002cafeteria' => array('name' => 'Cafeteria Menu', 'location' => 'Employee Cafeteria'),
    
    // Government Examples
    // 'gov001cityhall01' => array('name' => 'City Services', 'location' => 'City Hall Lobby'),
    // 'gov002dmv0000001' => array('name' => 'DMV Services', 'location' => 'DMV Office'),
    
    // Transportation Examples
    // 'transport001gate' => array('name' => 'Airport Gate Info', 'location' => 'Terminal Gate 5'),
    // 'transport002tick' => array('name' => 'Ticket Kiosk', 'location' => 'Metro Station'),
    
    // To add devices:
    // 1. Load simple_validation.html on your device 
    // 2. Copy device ID from "Access Denied" screen
    // 3. Uncomment and replace one of the examples above with your device ID
    // 4. Or add new line: 'your_device_id' => array('name' => 'Device Name', 'location' => 'Location'),
)

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