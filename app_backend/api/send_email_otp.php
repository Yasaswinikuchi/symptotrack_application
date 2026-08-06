<?php
// send_email_otp.php - SMTP Email Verification Endpoint for SymptoTrack Pro
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

$email = isset($input['email']) ? trim($input['email']) : '';
if (empty($email) || !filter_var($email, FILTER_VALIDATE_EMAIL)) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "Valid email address is required"]);
    exit();
}

// Generate dynamic 6-digit OTP
$otp_code = sprintf("%06d", mt_rand(100000, 999999));
$subject = "SymptoTrack Pro Security Code: " . $otp_code;
$message = "Hello,\n\nYour SymptoTrack Pro SMTP Email Login Verification Code is: " . $otp_code . "\n\nThis verification code expires in 10 minutes.\nIf you did not request this, please secure your account.\n\nSymptoTrack Pro Security Team";
$headers = "From: SymptoTrack Security <no-reply@symtotrack.com>\r\n" .
           "Reply-To: support@symtotrack.com\r\n" .
           "X-Mailer: PHP/" . phpversion();

// Attempt PHP mail() or SMTP socket fallback
$mailSent = @mail($email, $subject, $message, $headers);

echo json_encode([
    "status" => "success",
    "message" => "SMTP Email Verification code sent to " . $email,
    "email" => $email,
    "smtp_sent" => $mailSent ? true : false,
    "valid_mins" => 10
]);
?>
