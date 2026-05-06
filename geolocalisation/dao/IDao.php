<?php
interface IDao {
    public function create($objet);
    public function read($id);
    public function readAll();
    public function update($objet);
    public function delete($id);
}
?>