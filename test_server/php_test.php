<?php
// php_test.php - Simple diagnostic script
header('Content-Type: application/json');

$result = [
    'status' => 'php_working',
    'message' => 'PHP is working correctly',
    'server_info' => [
        'php_version' => phpversion(),
        'server_time' => date('Y-m-d H:i:s'),
        'timezone' => date_default_timezone_get()
    ],
    'received_headers' => [],
    'server_vars' => [
        'REQUEST_METHOD' => $_SERVER['REQUEST_METHOD'] ?? 'unknown',
        'REQUEST_URI' => $_SERVER['REQUEST_URI'] ?? 'unknown',
        'HTTP_USER_AGENT' => $_SERVER['HTTP_USER_AGENT'] ?? 'unknown'
    ]
];

// Collect all headers
if (function_exists('getallheaders')) {
    foreach (getallheaders() as $name => $value) {
        $result['received_headers'][$name] = $value;
    }
} else {
    // Fallback for servers without getallheaders()
    foreach ($_SERVER as $name => $value) {
        if (strpos($name, 'HTTP_') === 0) {
            $header = str_replace(' ', '-', ucwords(str_replace('_', ' ', strtolower(substr($name, 5)))));
            $result['received_headers'][$header] = $value;
        }
    }
}

echo json_encode($result, JSON_PRETTY_PRINT);
?>