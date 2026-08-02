<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db.php';

$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);

$id = isset($input['id']) ? intval($input['id']) : 0;

if ($id <= 0) {
    http_response_code(400);
    echo json_encode(["error" => "Invalid ID"]);
    exit();
}

$stmt = $conn->prepare("DELETE FROM history WHERE id = ?");
$stmt->bind_param("i", $id);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Record deleted successfully"]);
} else {
    http_response_code(500);
    echo json_encode(["error" => "Failed to delete record"]);
}

$stmt->close();
$conn->close();
?>
