<?php
// init_db.php
$servername = "localhost";
$username = "root";
$password = "";

try {
    $conn = new PDO("mysql:host=$servername", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $sql = file_get_contents("db/setup.sql");
    $conn->exec($sql);
    
    echo "Database and tables created successfully. You are ready to go!";
} catch(PDOException $e) {
    echo "Connection failed: " . $e->getMessage();
}
?>
