<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

$lat = isset($_GET['lat']) ? $_GET['lat'] : 17.4375;
$lon = isset($_GET['lon']) ? $_GET['lon'] : 78.4482;

$query = '[out:json];(node["amenity"="hospital"](around:5000,'.$lat.','.$lon.');node["amenity"="clinic"](around:5000,'.$lat.','.$lon.'););out body;>;out skel qt;';

$url = "https://overpass-api.de/api/interpreter?data=" . urlencode($query);

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
curl_setopt($ch, CURLOPT_USERAGENT, 'SymptoTrack/1.0');
// Disable SSL verification for local dev if needed, though better to keep it if CA is available
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

$response = curl_exec($ch);
$httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

if ($response === FALSE || $httpcode !== 200) {
    http_response_code(500);
    echo json_encode(["error" => "Failed to fetch from Overpass API"]);
    exit;
}

echo $response;
?>
