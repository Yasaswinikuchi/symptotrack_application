<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["error" => "Method not allowed"]);
    exit();
}

require_once 'db.php';

// Mock auth: user ID 1
$userId = 1;

$stmt = $conn->prepare("DELETE FROM users WHERE id=?");
$stmt->bind_param("i", $userId);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Account deleted successfully"]);
} else {
    http_response_code(500);
    echo json_encode(["error" => "Failed to delete account"]);
}

$stmt->close();
$conn->close();
?>
