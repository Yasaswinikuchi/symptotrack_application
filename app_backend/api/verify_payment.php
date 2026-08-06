<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$keySecret = 'CkAEAJmlCOE3gga77CUFO9yS';

$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);

$razorpay_order_id = isset($input['razorpay_order_id']) ? $input['razorpay_order_id'] : '';
$razorpay_payment_id = isset($input['razorpay_payment_id']) ? $input['razorpay_payment_id'] : '';
$razorpay_signature = isset($input['razorpay_signature']) ? $input['razorpay_signature'] : '';

if (empty($razorpay_order_id) || empty($razorpay_payment_id) || empty($razorpay_signature)) {
    http_response_code(400);
    echo json_encode(["error" => "Missing parameters"]);
    exit();
}

$generated_signature = hash_hmac('sha256', $razorpay_order_id . "|" . $razorpay_payment_id, $keySecret);

if ($generated_signature === $razorpay_signature) {
    echo json_encode(["success" => true, "message" => "Payment verified successfully"]);
} else {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Payment verification failed"]);
}
?>
