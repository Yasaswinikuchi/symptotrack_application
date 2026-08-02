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

$stmt = $conn->prepare("DELETE FROM history");

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "All records deleted successfully"]);
} else {
    http_response_code(500);
    echo json_encode(["error" => "Failed to delete records"]);
}

$stmt->close();
$conn->close();
?>
