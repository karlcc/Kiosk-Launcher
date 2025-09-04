<?php
header('Content-Type: application/json');
echo json_encode(array(
    'status' => 'working',
    'message' => 'PHP is working',
    'time' => date('Y-m-d H:i:s')
));
?>