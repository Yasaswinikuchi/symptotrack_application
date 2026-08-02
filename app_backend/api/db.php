<?php
$host = 'localhost';
$user = 'root';
$pass = '';
$dbname = 'symto_db';

try {
    mysqli_report(MYSQLI_REPORT_STRICT | MYSQLI_REPORT_ERROR);
    $conn = new mysqli($host, $user, $pass, $dbname);
} catch (mysqli_sql_exception $e) {
    die(json_encode(["error" => "Connection failed: " . $e->getMessage()]));
}
?>
