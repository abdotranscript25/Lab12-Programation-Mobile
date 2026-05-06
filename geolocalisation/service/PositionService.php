<?php
require_once(__DIR__ . "/../dao/IDao.php");
require_once(__DIR__ . "/../classe/Position.php");
require_once(__DIR__ . "/../connexion/Connexion.php");

class PositionService implements IDao {
    private $connexion;

    public function __construct() {
        $this->connexion = Connexion::getInstance()->getPDO();
    }

    public function create($objet) {
        $sql = "INSERT INTO position (latitude, longitude, date_position, imei) VALUES (?, ?, ?, ?)";
        $stmt = $this->connexion->prepare($sql);
        $stmt->execute([
            $objet->getLatitude(),
            $objet->getLongitude(),
            $objet->getDatePosition(),
            $objet->getImei()
        ]);
        return $this->connexion->lastInsertId();
    }

    public function read($id) {
        $sql = "SELECT * FROM position WHERE id = ?";
        $stmt = $this->connexion->prepare($sql);
        $stmt->execute([$id]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($row) {
            return new Position(
                $row['id'],
                $row['latitude'],
                $row['longitude'],
                $row['date_position'],
                $row['imei']
            );
        }
        return null;
    }

    public function readAll() {
        $sql = "SELECT * FROM position ORDER BY date_position DESC";
        $stmt = $this->connexion->query($sql);
        $positions = [];
        
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $positions[] = new Position(
                $row['id'],
                $row['latitude'],
                $row['longitude'],
                $row['date_position'],
                $row['imei']
            );
        }
        return $positions;
    }

    public function update($objet) {
        $sql = "UPDATE position SET latitude = ?, longitude = ?, date_position = ?, imei = ? WHERE id = ?";
        $stmt = $this->connexion->prepare($sql);
        $stmt->execute([
            $objet->getLatitude(),
            $objet->getLongitude(),
            $objet->getDatePosition(),
            $objet->getImei(),
            $objet->getId()
        ]);
        return $stmt->rowCount();
    }

    public function delete($id) {
        $sql = "DELETE FROM position WHERE id = ?";
        $stmt = $this->connexion->prepare($sql);
        $stmt->execute([$id]);
        return $stmt->rowCount();
    }
}
?>