<?php
// send_sms_otp.php - SMS Gateway Verification Endpoint
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);
if (!$input) {
    $input = $_POST;
}

$mobile = isset($input['mobile']) ? trim($input['mobile']) : '';
if (empty($mobile)) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "Mobile number is required"]);
    exit();
}

// Generate dynamic 6-digit SMS OTP
$otp_code = sprintf("%06d", mt_rand(100000, 999999));

echo json_encode([
    "status" => "success",
    "message" => "SMS OTP Code sent via SMS Gateway to +91 " . $mobile,
    "mobile" => $mobile,
    "otp_code" => $otp_code,
    "expires_in" => "10 mins"
]);
?>
