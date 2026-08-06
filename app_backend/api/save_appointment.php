<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db.php';

$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);

$hospital_name = isset($input['hospital_name']) ? $input['hospital_name'] : '';
$payment_id = isset($input['payment_id']) ? $input['payment_id'] : '';
$problem_description = isset($input['problem_description']) ? $input['problem_description'] : '';
$date = isset($input['date']) ? $input['date'] : '';
$time_slot = isset($input['time_slot']) ? $input['time_slot'] : '';
$user_id = 1; // mock user

if (empty($hospital_name) || empty($payment_id) || empty($date)) {
    http_response_code(400);
    echo json_encode(["error" => "Missing parameters"]);
    exit();
}

$stmt = $conn->prepare("INSERT INTO appointments (user_id, hospital_name, payment_id, problem_description, appointment_date, time_slot, status) VALUES (?, ?, ?, ?, ?, ?, 'Booked')");
$stmt->bind_param("isssss", $user_id, $hospital_name, $payment_id, $problem_description, $date, $time_slot);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Appointment saved successfully"]);
} else {
    http_response_code(500);
    echo json_encode(["error" => "Failed to save appointment"]);
}

$stmt->close();
$conn->close();
?>
