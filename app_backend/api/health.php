<?php
header('Content-Type: application/json');

$response = [
    'status' => 'ok',
    'message' => 'SymptoTrack Backend is running',
    'timestamp' => time()
];

echo json_encode($response);
?>
