<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db.php';

// Mock auth: just use user ID 1
$userId = 1;

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $sql = "SELECT id, name, email FROM users WHERE id = $userId";
    $result = $conn->query($sql);
    
    if ($result && $result->num_rows > 0) {
        echo json_encode($result->fetch_assoc());
    } else {
        http_response_code(404);
        echo json_encode(["error" => "User not found"]);
    }
} else if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $inputJSON = file_get_contents('php://input');
    $input = json_decode($inputJSON, TRUE);
    
    $name = isset($input['name']) ? $input['name'] : null;
    $email = isset($input['email']) ? $input['email'] : null;
    $password = isset($input['password']) ? $input['password'] : null;
    
    if ($name && $email) {
        if (!empty($password)) {
            $hashed = password_hash($password, PASSWORD_DEFAULT);
            $stmt = $conn->prepare("UPDATE users SET name=?, email=?, password=? WHERE id=?");
            $stmt->bind_param("sssi", $name, $email, $hashed, $userId);
        } else {
            $stmt = $conn->prepare("UPDATE users SET name=?, email=? WHERE id=?");
            $stmt->bind_param("ssi", $name, $email, $userId);
        }
        
        if ($stmt->execute()) {
            echo json_encode(["success" => true, "message" => "Profile updated"]);
        } else {
            http_response_code(500);
            echo json_encode(["error" => "Failed to update profile"]);
        }
        $stmt->close();
    } else {
        http_response_code(400);
        echo json_encode(["error" => "Name and email are required"]);
    }
}

$conn->close();
?>
