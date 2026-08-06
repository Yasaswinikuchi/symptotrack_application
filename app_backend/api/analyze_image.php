<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

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

$imageBase64 = isset($input['image']) ? $input['image'] : '';

if (empty($imageBase64)) {
    http_response_code(400);
    echo json_encode(["error" => "Image is required"]);
    exit();
}

$apiKey = 'AIzaSyAbhUCaS0HCdPSzrqfI4fKqi3F12-WX2RE';
$apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" . $apiKey;

$prompt = "You are an AI medical assistant. Examine the uploaded image. 
First, determine if it is a list of medicines or a prescription.
If it is NOT a medicine list or prescription, you MUST return exactly this JSON and nothing else:
{
  \"valid\": false, 
  \"message\": \"Please upload a valid medicine list or prescription.\"
}

If it IS a valid medicine list or prescription, analyze it and explain why each medicine is used, when it should be taken, and how it should be used. 
Respond strictly with a JSON object in this format (no markdown code blocks, just raw JSON):
{
  \"valid\": true,
  \"riskLevel\": \"Info\",
  \"riskDescription\": \"Analysis of your medications.\",
  \"conditions\": [
    {\"name\": \"Medicine Name\", \"matchPercentage\": \"Prescribed\", \"description\": \"Why it is used, when to take it, and how.\"}
  ],
  \"recommendations\": [
    {\"title\": \"General Advice\", \"description\": \"Take as prescribed and consult your doctor for changes.\", \"icon\": \"check\"}
  ]
}";

$data = [
    "contents" => [
        [
            "parts" => [
                ["text" => $prompt],
                [
                    "inlineData" => [
                        "mimeType" => "image/jpeg",
                        "data" => $imageBase64
                    ]
                ]
            ]
        ]
    ]
];

$ch = curl_init($apiUrl);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: application/json'));
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

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
        $aiText = str_replace('```json', '', $aiText);
        $aiText = str_replace('```', '', $aiText);
        $aiText = trim($aiText);
        
        $jsonResponse = json_decode($aiText, true);
        if ($jsonResponse) {
            if (isset($jsonResponse['valid']) && $jsonResponse['valid'] === true) {
                require_once 'db.php';
                $type = "Medicine Scan";
                $symptoms = "Scanned Prescription / Medicine List";
                $risk_level = "Info";
                
                $stmt = $conn->prepare("INSERT INTO history (type, symptoms, risk_level, full_response) VALUES (?, ?, ?, ?)");
                $stmt->bind_param("ssss", $type, $symptoms, $risk_level, $aiText);
                $stmt->execute();
                $stmt->close();
                $conn->close();
            }
            echo json_encode($jsonResponse);
        } else {
            echo json_encode(["error" => "Failed to parse AI JSON", "raw" => $aiText]);
        }
    } else {
        echo json_encode(["error" => "Unexpected AI response"]);
    }
} else {
    http_response_code($httpcode);
    echo json_encode(["error" => "API returned HTTP " . $httpcode, "details" => $response]);
}
?>
