<?php
// Sample Device Whitelist with Descriptions
// Replace the empty whitelist in device_whitelist.php with this format:

$whitelist = array(
    // Your actual device (from your logs)
    'ff7fca15cf0b74c5' => array(
        'name' => 'Samsung Galaxy A35', 
        'location' => 'Development Testing',
        'description' => 'Primary test device for kiosk development'
    ),
    
    // Reception Area Tablet
    '1a2b3c4d5e6f7890' => array(
        'name' => 'Reception Kiosk', 
        'location' => 'Front Lobby',
        'description' => 'Main reception tablet for visitor check-in'
    ),
    
    // Conference Room Display
    '9876543210abcdef' => array(
        'name' => 'Conference Room Display', 
        'location' => 'Meeting Room A',
        'description' => 'Conference room booking and display system'
    ),
    
    // Retail Display
    'abc123def456789' => array(
        'name' => 'Product Showcase', 
        'location' => 'Retail Floor',
        'description' => 'Customer-facing product information display'
    ),
    
    // Information Booth
    'fedcba0987654321' => array(
        'name' => 'Information Kiosk', 
        'location' => 'Mall Entrance',
        'description' => 'Public information and directory kiosk'
    ),
    
    // Staff Training Device
    '555666777888999' => array(
        'name' => 'Training Tablet', 
        'location' => 'Training Room',
        'description' => 'Staff training and onboarding system'
    ),
    
    // Backup Device
    'backup123456789' => array(
        'name' => 'Backup Kiosk', 
        'location' => 'Storage Room',
        'description' => 'Emergency replacement device'
    ),
    
    // Demo Device for Clients
    'demo987654321abc' => array(
        'name' => 'Client Demo Device', 
        'location' => 'Sales Office',
        'description' => 'Demonstration device for client presentations'
    )
);

/*
USAGE INSTRUCTIONS:

1. TO ADD YOUR DEVICE:
   Copy your device ID from the "Access Denied" screen
   Replace 'YOUR_DEVICE_ID_HERE' with your actual device ID
   
2. TO GET DEVICE IDs:
   - Load simple_validation.html on each device
   - Copy the device ID from the "Access Denied" screen
   - Add it to this whitelist array
   
3. TO REPLACE IN device_whitelist.php:
   Replace this line:
   $whitelist = array();
   
   With this (using your actual device IDs):
   $whitelist = array(
       'your_device_id_1' => array('name' => 'Device Name', 'location' => 'Location', 'description' => 'Description'),
       'your_device_id_2' => array('name' => 'Device Name', 'location' => 'Location', 'description' => 'Description'),
   );

4. RESULT:
   - Authorized devices → "Access Granted" 🟢
   - Unknown devices → "Access Denied" 🔴 (shows device ID for you to add)
*/
?>