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

$email = isset($input['email']) ? trim($input['email']) : '';
$new_password = isset($input['new_password']) ? trim($input['new_password']) : '';

if (empty($email) || empty($new_password)) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "Missing email or password"]);
    exit();
}

// Check if user exists
$stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    http_response_code(404);
    echo json_encode(["status" => "error", "message" => "No account found with this email"]);
    $stmt->close();
    $conn->close();
    exit();
}
$stmt->close();

// Update password
$hashed_password = password_hash($new_password, PASSWORD_DEFAULT);
$update_stmt = $conn->prepare("UPDATE users SET password = ? WHERE email = ?");
$update_stmt->bind_param("ss", $hashed_password, $email);

if ($update_stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Password reset successfully"]);
} else {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => "Failed to update password"]);
}

$update_stmt->close();
$conn->close();
?>
