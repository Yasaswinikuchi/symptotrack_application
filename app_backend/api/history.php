<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

require_once 'db.php';

$sql = "SELECT * FROM history ORDER BY created_at DESC";
$result = $conn->query($sql);

$history = array();

if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $history[] = $row;
    }
}

echo json_encode($history);
$conn->close();
?>
