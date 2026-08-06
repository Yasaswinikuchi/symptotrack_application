<?php
header("Access-Control-Allow-Origin: *");
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

$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);

$symptoms = isset($input['symptomsText']) ? $input['symptomsText'] : '';
$age = isset($input['age']) ? $input['age'] : 'Unknown';
$gender = isset($input['gender']) ? $input['gender'] : 'Unknown';
$conditions = isset($input['conditions']) ? $input['conditions'] : [];

if (empty($symptoms)) {
    http_response_code(400);
    echo json_encode(["error" => "Symptoms text is required"]);
    exit();
}

$apiKey = 'AIzaSyAbhUCaS0HCdPSzrqfI4fKqi3F12-WX2RE';
$apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" . $apiKey;

$prompt = "You are an AI medical assistant. Analyze the following symptoms and provide a JSON response. 
Patient Profile: Age $age, Gender $gender. Pre-existing conditions: " . implode(", ", $conditions) . "
Symptoms: $symptoms

Respond strictly with a JSON object in this format (no markdown code blocks, just raw JSON):
{
  \"riskLevel\": \"Low\" | \"Medium\" | \"High\",
  \"riskDescription\": \"Based on your symptoms...\",
  \"conditions\": [
    {\"name\": \"Condition Name\", \"matchPercentage\": \"85% Match\", \"description\": \"Short description\"}
  ],
  \"recommendations\": [
    {\"title\": \"Rest and Hydrate\", \"description\": \"Description...\", \"icon\": \"alert\" | \"check\"}
  ]
}";

$data = [
    "contents" => [
        [
            "parts" => [
                ["text" => $prompt]
            ]
        ]
    ]
];

$ch = curl_init($apiUrl);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: application/json'));
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false); // For local XAMPP without SSL config

$response = curl_exec($ch);
$httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

if(curl_errno($ch)){
    http_response_code(500);
    echo json_encode(["error" => curl_error($ch)]);
    curl_close($ch);
    exit();
}

curl_close($ch);

if ($httpcode == 200) {
    $result = json_decode($response, true);
    if(isset($result['candidates'][0]['content']['parts'][0]['text'])) {
        $aiText = $result['candidates'][0]['content']['parts'][0]['text'];
        // Clean up markdown if any
        $aiText = str_replace('```json', '', $aiText);
        $aiText = str_replace('```', '', $aiText);
        $aiText = trim($aiText);
        
        $jsonResponse = json_decode($aiText, true);
        if ($jsonResponse) {
            require_once 'db.php';
            $risk_level = isset($jsonResponse['riskLevel']) ? $jsonResponse['riskLevel'] : 'Unknown';
            $type = "Symptom Check";
            
            $stmt = $conn->prepare("INSERT INTO history (type, symptoms, risk_level, full_response) VALUES (?, ?, ?, ?)");
            $stmt->bind_param("ssss", $type, $symptoms, $risk_level, $aiText);
            $stmt->execute();
            $stmt->close();
            $conn->close();

            echo json_encode($jsonResponse);
        } else {
            echo json_encode(["error" => "Failed to parse AI JSON", "raw" => $aiText]);
        }
    } else {
        echo json_encode(["error" => "Unexpected AI response structure"]);
    }
} else {
    http_response_code($httpcode);
    echo $response;
}
?>
