<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["error" => "Method not allowed"]);
    exit();
}

$keyId = 'rzp_test_SqOZwDnHPrqJm0';
$keySecret = 'CkAEAJmlCOE3gga77CUFO9yS';

$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);

$amount = isset($input['amount']) ? $input['amount'] : 50000; // default 500 INR in paise

$orderData = [
    'receipt'         => 'rcptid_' . time(),
    'amount'          => $amount,
    'currency'        => 'INR',
    'payment_capture' => 1
];

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://api.razorpay.com/v1/orders');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
curl_setopt($ch, CURLOPT_POST, 1);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($orderData));
curl_setopt($ch, CURLOPT_USERPWD, $keyId . ':' . $keySecret);
curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: application/json'));

$response = curl_exec($ch);
if (curl_errno($ch)) {
    http_response_code(500);
    echo json_encode(['error' => 'Curl error: ' . curl_error($ch)]);
} else {
    echo $response; // Returns JSON from Razorpay (contains order id)
}
curl_close($ch);
?>
