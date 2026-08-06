<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

require_once 'db.php';

$user_id = 1; // mock user

$stmt = $conn->prepare("SELECT * FROM appointments WHERE user_id = ? ORDER BY id DESC");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$appointments = [];
while ($row = $result->fetch_assoc()) {
    $appointments[] = $row;
}

echo json_encode(["success" => true, "data" => $appointments]);
$stmt->close();
$conn->close();
?>
