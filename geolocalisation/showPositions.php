<?php
header('Content-Type: application/json; charset=utf-8');

if ($_SERVER["REQUEST_METHOD"] != "POST") {
    http_response_code(405);
    echo json_encode(["ok" => false, "error" => "POST required"]);
    exit;
}

include_once __DIR__ . '/service/PositionService.php';

try {
    $service = new PositionService();
    $positions = $service->getAll();
    echo json_encode(["ok" => true, "positions" => $positions]);
} catch(Exception $e) {
    http_response_code(500);
    echo json_encode(["ok" => false, "error" => $e->getMessage()]);
}
?>