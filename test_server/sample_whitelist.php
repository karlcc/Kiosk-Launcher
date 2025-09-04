<?php
// Sample Device Whitelist - Copy this format into your device_whitelist.php

// Device whitelist - replace with database lookup in production
$whitelisted_devices = [
    // Development & Testing Devices
    'ff7fca15cf0b74c5' => ['name' => 'Samsung Galaxy A35', 'location' => 'Development Lab'],
    'a1b2c3d4e5f6g7h8' => ['name' => 'Test Tablet 1', 'location' => 'QA Department'],
    'h8g7f6e5d4c3b2a1' => ['name' => 'Test Tablet 2', 'location' => 'QA Department'],
    
    // Reception & Front Desk
    '1234567890abcdef' => ['name' => 'Reception Kiosk', 'location' => 'Main Lobby'],
    'fedcba0987654321' => ['name' => 'Visitor Check-in', 'location' => 'Security Desk'],
    '555888999000111' => ['name' => 'Guest Registration', 'location' => 'Front Entrance'],
    
    // Conference & Meeting Rooms
    'conf001meeting01' => ['name' => 'Conference Room A Display', 'location' => 'Meeting Room A'],
    'conf002meeting02' => ['name' => 'Conference Room B Display', 'location' => 'Meeting Room B'],
    'conf003meeting03' => ['name' => 'Boardroom Display', 'location' => 'Executive Boardroom'],
    'conf004meeting04' => ['name' => 'Training Room Kiosk', 'location' => 'Training Center'],
    
    // Retail & Customer Service
    'retail001pos001' => ['name' => 'Product Info Kiosk', 'location' => 'Retail Floor Section A'],
    'retail002pos002' => ['name' => 'Price Check Station', 'location' => 'Retail Floor Section B'],
    'retail003pos003' => ['name' => 'Customer Service', 'location' => 'Service Counter'],
    'retail004pos004' => ['name' => 'Self-Service Portal', 'location' => 'Customer Area'],
    
    // Food Service & Hospitality
    'restaurant001001' => ['name' => 'Menu Display', 'location' => 'Restaurant Entrance'],
    'restaurant002002' => ['name' => 'Order Kiosk', 'location' => 'Fast Food Counter'],
    'hotel001checkin1' => ['name' => 'Hotel Check-in', 'location' => 'Hotel Lobby'],
    'hotel002concier1' => ['name' => 'Concierge Services', 'location' => 'Hotel Services'],
    
    // Healthcare & Medical
    'medical001patien' => ['name' => 'Patient Check-in', 'location' => 'Hospital Reception'],
    'medical002waitin' => ['name' => 'Waiting Room Info', 'location' => 'Waiting Area'],
    'medical003pharma' => ['name' => 'Pharmacy Kiosk', 'location' => 'Hospital Pharmacy'],
    
    // Education & Libraries
    'edu001library001' => ['name' => 'Library Catalog', 'location' => 'Main Library'],
    'edu002student001' => ['name' => 'Student Services', 'location' => 'Student Center'],
    'edu003campus0001' => ['name' => 'Campus Directory', 'location' => 'Campus Entrance'],
    
    // Transportation & Public
    'transport001gate' => ['name' => 'Airport Gate Info', 'location' => 'Terminal Gate 5'],
    'transport002tick' => ['name' => 'Ticket Kiosk', 'location' => 'Metro Station'],
    'transport003info' => ['name' => 'Transit Info', 'location' => 'Bus Terminal'],
    
    // Government & Municipal
    'gov001cityhall01' => ['name' => 'City Services', 'location' => 'City Hall Lobby'],
    'gov002dmv0000001' => ['name' => 'DMV Services', 'location' => 'DMV Office'],
    'gov003library001' => ['name' => 'Public Library', 'location' => 'Public Library'],
    
    // Corporate & Office
    'corp001employee1' => ['name' => 'Employee Directory', 'location' => 'Corporate Lobby'],
    'corp002cafeteria' => ['name' => 'Cafeteria Menu', 'location' => 'Employee Cafeteria'],
    'corp003mailroom1' => ['name' => 'Mailroom Kiosk', 'location' => 'Mail Center'],
    
    // Backup & Maintenance
    'backup001spare01' => ['name' => 'Backup Device 1', 'location' => 'IT Storage'],
    'backup002spare02' => ['name' => 'Backup Device 2', 'location' => 'IT Storage'],
    'maintenance00001' => ['name' => 'Maintenance Tablet', 'location' => 'Facilities Department'],
    
    // Demo & Sales
    'demo001sales0001' => ['name' => 'Sales Demo Unit', 'location' => 'Sales Office'],
    'demo002client001' => ['name' => 'Client Presentation', 'location' => 'Conference Room'],
    'demo003showroom1' => ['name' => 'Showroom Display', 'location' => 'Product Showroom'],
    
    // To add more devices:
    // 1. Load simple_validation.html on the new device
    // 2. Copy the device ID from the "Access Denied" screen
    // 3. Add a new line like this:
    // 'your_device_id_here' => ['name' => 'Device Name', 'location' => 'Physical Location'],
    
    // Your actual device ID from logs (uncomment to authorize):
    // 'ff7fca15cf0b74c5' => ['name' => 'Samsung Galaxy A35', 'location' => 'Test Device'],
];
?>