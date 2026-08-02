<?php
require_once 'db.php';
$conn->query("ALTER TABLE appointments ADD COLUMN appointment_date DATE AFTER problem_description");
echo "Done";
?>
