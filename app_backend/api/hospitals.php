<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

$lat = isset($_GET['lat']) ? floatval($_GET['lat']) : 13.0382;
$lon = isset($_GET['lon']) ? floatval($_GET['lon']) : 80.0494;

// 100% Local Self-Contained Healthcare Facility Dataset (No External APIs)
$localFacilities = [
    [
        "id" => 101,
        "name" => "Saveetha Medical College Hospital",
        "type" => "General Hospital & Emergency Care",
        "phone" => "+91 44 3059 4462",
        "fee" => 350,
        "wait" => 12,
        "rating" => "4.8",
        "lat" => $lat + 0.004,
        "lon" => $lon + 0.003
    ],
    [
        "id" => 102,
        "name" => "Thirumazhisai Community Medicare",
        "type" => "Multi-Specialty Clinic & Triage",
        "phone" => "+91 44 2829 4000",
        "fee" => 280,
        "wait" => 15,
        "rating" => "4.5",
        "lat" => $lat - 0.007,
        "lon" => $lon + 0.008
    ],
    [
        "id" => 103,
        "name" => "R.K. Multi-Specialty Hospital",
        "type" => "Emergency, Pediatrics & General",
        "phone" => "+91 44 3634 5617",
        "fee" => 420,
        "wait" => 9,
        "rating" => "4.6",
        "lat" => $lat + 0.012,
        "lon" => $lon - 0.009
    ],
    [
        "id" => 104,
        "name" => "City Heart & Vascular Center",
        "type" => "Cardiology & Vascular Emergency",
        "phone" => "+91 44 2829 3333",
        "fee" => 450,
        "wait" => 8,
        "rating" => "4.9",
        "lat" => $lat - 0.015,
        "lon" => $lon - 0.012
    ],
    [
        "id" => 105,
        "name" => "Urgent Care & Diagnostics Clinic",
        "type" => "Dermatology, ENT & Fever Care",
        "phone" => "+91 44 4545 7777",
        "fee" => 320,
        "wait" => 18,
        "rating" => "4.3",
        "lat" => $lat + 0.018,
        "lon" => $lon + 0.015
    ]
];

// Calculate Haversine Spherical Distance for each local facility
function calculateHaversineDistance($lat1, $lon1, $lat2, $lon2) {
    $earthRadius = 6371; // km
    $dLat = deg2rad($lat2 - $lat1);
    $dLon = deg2rad($lon2 - $lon1);
    $a = sin($dLat / 2) * sin($dLat / 2) +
         cos(deg2rad($lat1)) * cos(deg2rad($lat2)) *
         sin($dLon / 2) * sin($dLon / 2);
    $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
    return round($earthRadius * $c, 1);
}

$elements = [];
foreach ($localFacilities as $facility) {
    $dist = calculateHaversineDistance($lat, $lon, $facility['lat'], $facility['lon']);
    $elements[] = [
        "type" => "node",
        "id" => $facility['id'],
        "lat" => $facility['lat'],
        "lon" => $facility['lon'],
        "tags" => [
            "name" => $facility['name'],
            "amenity" => "hospital",
            "healthcare" => $facility['type'],
            "phone" => $facility['phone'],
            "fee" => $facility['fee'],
            "wait" => $facility['wait'],
            "rating" => $facility['rating'],
            "distance" => $dist
        ]
    ];
}

echo json_encode([
    "version" => 0.6,
    "generator" => "SymptoTrack Local Geospatial Engine",
    "elements" => $elements
]);
?>
