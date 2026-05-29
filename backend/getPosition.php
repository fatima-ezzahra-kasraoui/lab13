<?php
header('Content-Type: application/json');

$host    = "localhost";
$db_name = "map_project";
$username = "root";
$password = "";

try {
    $conn = new PDO("mysql:host=$host;dbname=$db_name;charset=utf8", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => "Connexion impossible : " . $e->getMessage()]);
    exit;
}

try {
    $stmt = $conn->prepare("SELECT * FROM positions ORDER BY date DESC");
    $stmt->execute();
    $positions = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "success"   => true,
        "count"     => count($positions),
        "positions" => $positions
    ]);
} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => "Erreur : " . $e->getMessage()]);
}
