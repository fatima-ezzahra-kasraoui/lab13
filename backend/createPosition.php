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

$latitude  = isset($_POST['latitude'])  ? trim($_POST['latitude'])  : null;
$longitude = isset($_POST['longitude']) ? trim($_POST['longitude']) : null;
$date      = isset($_POST['date'])      ? trim($_POST['date'])      : null;
$imei      = isset($_POST['imei'])      ? trim($_POST['imei'])      : null;

if (!$latitude || !$longitude || !$date || !$imei) {
    echo json_encode(["success" => false, "message" => "Données manquantes"]);
    exit;
}

// Validation basique des coordonnées
if (!is_numeric($latitude) || !is_numeric($longitude)) {
    echo json_encode(["success" => false, "message" => "Coordonnées invalides"]);
    exit;
}

try {
    $stmt = $conn->prepare(
        "INSERT INTO positions (latitude, longitude, date, imei)
         VALUES (:latitude, :longitude, :date, :imei)"
    );
    $stmt->bindParam(':latitude',  $latitude);
    $stmt->bindParam(':longitude', $longitude);
    $stmt->bindParam(':date',      $date);
    $stmt->bindParam(':imei',      $imei);
    $stmt->execute();

    echo json_encode([
        "success" => true,
        "message" => "Position enregistrée",
        "id"      => $conn->lastInsertId()
    ]);
} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => "Erreur : " . $e->getMessage()]);
}
