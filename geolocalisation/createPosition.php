<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

require_once(__DIR__ . "/service/PositionService.php");

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    
    $latitude = isset($_POST['latitude']) ? $_POST['latitude'] : null;
    $longitude = isset($_POST['longitude']) ? $_POST['longitude'] : null;
    $date_position = isset($_POST['date_position']) ? $_POST['date_position'] : null;
    $imei = isset($_POST['imei']) ? $_POST['imei'] : null;

    if ($latitude && $longitude && $date_position && $imei) {
        
        $position = new Position(null, $latitude, $longitude, $date_position, $imei);
        $service = new PositionService();
        $result = $service->create($position);
        
        if ($result > 0) {
            echo json_encode([
                "success" => true,
                "message" => "Position enregistrée avec succès",
                "id" => $result
            ]);
        } else {
            echo json_encode([
                "success" => false,
                "message" => "Erreur lors de l'enregistrement"
            ]);
        }
    } else {
        echo json_encode([
            "success" => false,
            "message" => "Données manquantes"
        ]);
    }
} else {
    echo json_encode([
        "success" => false,
        "message" => "Méthode non autorisée. Utilisez POST."
    ]);
}
?>