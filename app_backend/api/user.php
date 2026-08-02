<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

// Mock user data
$user = array(
    "id" => 1,
    "name" => "Alex Johnson",
    "email" => "test@example.com"
);

http_response_code(200);
echo json_encode($user);
?>
