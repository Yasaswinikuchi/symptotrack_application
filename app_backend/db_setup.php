<?php
$host = 'localhost';
$user = 'root';
$pass = '';

$conn = new mysqli($host, $user, $pass);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$sql = "CREATE DATABASE IF NOT EXISTS symto_db";
if ($conn->query($sql) === TRUE) {
    echo "Database created successfully or already exists.\n";
} else {
    echo "Error creating database: " . $conn->error . "\n";
}

$conn->select_db("symto_db");

$tableSql = "CREATE TABLE IF NOT EXISTS history (
    id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    symptoms TEXT NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    full_response LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)";

if ($conn->query($tableSql) === TRUE) {
    echo "Table history created successfully or already exists.\n";
} else {
    echo "Error creating table: " . $conn->error . "\n";
}

$userTableSql = "CREATE TABLE IF NOT EXISTS users (
    id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)";

if ($conn->query($userTableSql) === TRUE) {
    echo "Table users created successfully or already exists.\n";
    $checkUser = $conn->query("SELECT * FROM users WHERE email = 'alex.johnson@example.com'");
    if ($checkUser->num_rows == 0) {
        $password = password_hash('password123', PASSWORD_DEFAULT);
        $conn->query("INSERT INTO users (name, email, password) VALUES ('Alex Johnson', 'alex.johnson@example.com', '$password')");
    }
} else {
    echo "Error creating table: " . $conn->error . "\n";
}

$appointmentSql = "CREATE TABLE IF NOT EXISTS appointments (
    id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT(6) UNSIGNED NOT NULL,
    problem_description TEXT,
    time_slot VARCHAR(50),
    hospital_name VARCHAR(150) NOT NULL,
    payment_id VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
)";

if ($conn->query($appointmentSql) === TRUE) {
    echo "Table appointments created successfully or already exists.\n";
} else {
    echo "Error creating table: " . $conn->error . "\n";
}

$conn->close();
?>
