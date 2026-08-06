<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db.php';

$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);

$appointment_id = isset($input['id']) ? intval($input['id']) : 0;

if ($appointment_id <= 0) {
    http_response_code(400);
    echo json_encode(["error" => "Invalid ID"]);
    exit();
}

$stmt = $conn->prepare("DELETE FROM appointments WHERE id = ?");
$stmt->bind_param("i", $appointment_id);

if ($stmt->execute()) {
    echo json_encode(["success" => true]);
} else {
    http_response_code(500);
    echo json_encode(["error" => "Failed to delete"]);
}

$stmt->close();
$conn->close();
?>
