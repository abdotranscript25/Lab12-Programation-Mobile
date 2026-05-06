# 📍 Lab 12 – Localisation temps réel GPS + Google Maps + PHP/MySQL

## 🎯 Objectif du laboratoire

Créer une application Android complète qui :
- Capture la **position GPS** en temps réel
- Envoie les coordonnées à un **serveur distant** (PHP/MySQL) via Volley
- Stocke les positions dans une **base de données**
- Affiche toutes les positions historiques sur une **carte Google Maps**
- Permet la **communication indirecte** entre les activités via la base de données

---

## 🧠 Concepts clés abordés

| Concept | Description |
|---------|-------------|
| **GPS / LocationManager** | Service Android pour accéder à la position géographique |
| **Volley (StringRequest)** | Bibliothèque réseau pour requêtes POST |
| **Volley (JsonObjectRequest)** | Requête POST avec retour JSON direct |
| **Google Maps API** | Affichage de la carte et des markers |
| **PHP / MySQL** | Backend pour stocker les positions |
| **JSON parsing** | Lecture des données via `JSONArray` et `JSONObject` |
| **Communication indirecte** | Activités communiquant via la base de données |

---

## 🛠️ Technologies utilisées

| Catégorie | Technologie | Version |
|-----------|-------------|---------|
| IDE | Android Studio | Dernière |
| Langage | Java | 8+ |
| Réseau | Volley | 1.2.1 |
| Cartographie | Google Maps SDK | 18.2+ |
| Serveur | XAMPP / WAMP | - |
| Base de données | MySQL | 5.7+ |
| Backend | PHP | 7.4+ |

---

---

## 📂 Structure du projet Android

```
LocalisationTempsReel/
├── java/com.example.localisationtempsreel/
│   ├── MainActivity.java       # GPS + envoi vers serveur
│   └── MapsActivity.java       # Google Maps + markers dynamiques
├── res/
│   ├── layout/
│   │   ├── activity_main.xml   # Layout principal
│   │   └── activity_maps.xml   # Layout Google Maps
│   └── values/
│       └── google_maps_api.xml # Clé API (variable)
├── manifests/
│   └── AndroidManifest.xml     # Permissions + clé API
└── build.gradle                # Volley + Maps SDK
```

---

## 📂 Structure du projet serveur (PHP/MySQL)

```
E:\Xampp\htdocs\geolocalisation\
├── classe/
│   └── Position.php            # Classe métier
├── connexion/
│   └── Connexion.php           # Connexion MySQL (PDO)
├── dao/
│   └── IDao.php                # Interface CRUD
├── service/
│   └── PositionService.php     # Service d'accès données
├── createPosition.php          # INSERT (POST)
└── showPositions.php           # SELECT → JSON (POST)
```

---

## 🗄️ Base de données MySQL

### Structure de la table `position`

| Champ | Type | Description |
|-------|------|-------------|
| `id` | INT | Identifiant unique (auto-incrémenté) |
| `latitude` | DOUBLE | Coordonnée Nord-Sud |
| `longitude` | DOUBLE | Coordonnée Est-Ouest |
| `date_position` | DATETIME | Date et heure d'envoi |
| `imei` | VARCHAR(50) | Identifiant du téléphone (Android ID) |

### Création de la table

```sql
CREATE TABLE IF NOT EXISTS `position` (
  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `date_position` datetime NOT NULL,
  `imei` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 📱 Fonctionnalités de l'application

| Fonctionnalité | Statut |
|----------------|--------|
| Capture GPS en temps réel | ✅ |
| Envoi au serveur via POST (Volley) | ✅ |
| Stockage en base MySQL | ✅ |
| Google Maps avec markers | ✅ |
| Chargement dynamique des positions | ✅ |
| Centrage sur la position actuelle | ✅ |
| Communication indirecte via DB | ✅ |
| Permissions runtime | ✅ |

---

## 💻 Extraits de code importants

### Envoi de la position — `MainActivity.java`

```java
private void addPosition(final double lat, final double lon) {
    StringRequest request = new StringRequest(
            Request.Method.POST,
            insertUrl,
            response -> { /* Succès */ },
            error -> { /* Erreur */ }
    ) {
        @Override
        protected Map<String, String> getParams() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Map<String, String> params = new HashMap<>();
            params.put("latitude", String.valueOf(lat));
            params.put("longitude", String.valueOf(lon));
            params.put("date_position", sdf.format(new Date()));
            params.put("imei", getDeviceIdentifier());
            return params;
        }
    };
    requestQueue.add(request);
}
```

### Récupération et affichage des markers — `MapsActivity.java`

```java
private void loadMarkersFromServer() {
    JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.POST,
            showUrl,
            null,
            response -> {
                JSONArray positions = response.getJSONArray("positions");
                for (int i = 0; i < positions.length(); i++) {
                    JSONObject pos = positions.getJSONObject(i);
                    double lat = pos.getDouble("latitude");
                    double lon = pos.getDouble("longitude");
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lon))
                            .title("Position #" + (i + 1)));
                }
            },
            error -> { /* Erreur */ }
    );
    requestQueue.add(request);
}
```

---

## 🔧 Configuration de la clé API Google Maps

### 1. Ajouter la clé dans `local.properties`

```properties
MAPS_API_KEY=A..................
```

### 2. Le manifeste utilise la variable

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

---


## 🔧 Installation et exécution

### Prérequis serveur
- XAMPP / WAMP installé
- PHP 7.4+ et MySQL
- Téléphone et PC sur le **même réseau Wi-Fi**

### Étapes

1. Démarrer **XAMPP** (Apache + MySQL)
2. Créer la base de données via **phpMyAdmin** (script SQL ci-dessus)
3. Copier les fichiers PHP dans `E:\Xampp\htdocs\geolocalisation\`
4. Modifier l'IP dans `MainActivity.java` et `MapsActivity.java` :
```java
private final String insertUrl = "http://192.168.X.X/geolocalisation/createPosition.php";
private final String showUrl   = "http://192.168.X.X/geolocalisation/showPositions.php";
```
5. Configurer la clé API Maps dans `local.properties`
6. Lancer l'application sur un **téléphone physique**
7. Accepter les permissions de localisation
8. Activer le GPS sur le téléphone
9. Cliquer sur **"Afficher la carte"** pour voir les markers

---

## Captures D'écrans :

### MainActivity :

<img width="1050" height="796" alt="Main1" src="https://github.com/user-attachments/assets/2f0c64fa-5f3c-41ea-b899-b7211c95dd42" />
<img width="1018" height="809" alt="Main2" src="https://github.com/user-attachments/assets/050424fa-64cc-462e-a61a-88cea29bd028" />
<img width="1047" height="831" alt="Main3" src="https://github.com/user-attachments/assets/ee47f9a9-ab54-41b5-8781-ec28a7dfc2ed" />
<img width="1057" height="834" alt="Main4" src="https://github.com/user-attachments/assets/b25badf2-6260-4779-94ae-fdf139e9194d" />
<img width="1000" height="338" alt="Main5" src="https://github.com/user-attachments/assets/91c227f6-578a-45ab-89b7-700a319d9824" />


### MapsActivity :

<img width="1011" height="843" alt="MapsActivity" src="https://github.com/user-attachments/assets/1d46d3dc-0bdb-4e9d-9cfb-754b8e71dff5" />
<img width="1099" height="827" alt="MapsActivity2" src="https://github.com/user-attachments/assets/90243d0b-210f-45cd-a614-f277dbcc290a" />
<img width="1127" height="630" alt="MapsActivity3" src="https://github.com/user-attachments/assets/bf639a73-6fbd-48f2-9664-2bf8219b7459" />


### Manifest :

<img width="903" height="733" alt="Manifest1" src="https://github.com/user-attachments/assets/df4fcd3c-ee74-4407-b260-f243ffb852dc" />
<img width="890" height="680" alt="Manifest2" src="https://github.com/user-attachments/assets/f8f9beb5-0329-4493-acc8-f89b5a3bbe4a" />


### Base de Données et stockage des coordonnées de localisation :

<img width="991" height="392" alt="DB" src="https://github.com/user-attachments/assets/5c54bf36-9f35-427a-b4ea-2e10a7a1ee18" />


### activity_main (Layout 1) : 

<img width="1242" height="739" alt="Lay1" src="https://github.com/user-attachments/assets/220c114f-03c5-46a6-8021-ca3fa2e6c8d5" />

### Résultats de test :

<img width="348" height="725" alt="permission_GPS" src="https://github.com/user-attachments/assets/d0a91ac5-9e32-46a3-b716-93654a3709e3" />

<img width="342" height="727" alt="test1" src="https://github.com/user-attachments/assets/e33028ce-0c03-4d69-a7c5-10bade439ecc" />

<img width="351" height="121" alt="test2" src="https://github.com/user-attachments/assets/8403c3cf-f1d6-468e-99bd-c208aa7d9b4c" />


## Démonstration : 



https://github.com/user-attachments/assets/5ccd61f7-aeca-4bf6-b77d-428b58ab4da4




## 🐛 Résolution des erreurs courantes

| Erreur | Solution |
|--------|----------|
| `Cleartext HTTP traffic not permitted` | Ajouter `android:usesCleartextTraffic="true"` dans le manifeste |
| `Connection refused` | Vérifier que téléphone et PC sont sur le même Wi-Fi |
| `JSON parsing error` | Vérifier que `showPositions.php` retourne un JSON valide |
| Aucun marker affiché | Vérifier que la table `position` contient des données |
| Carte blanche | Vérifier la clé API et l'activation du Maps SDK |

---

## 📚 RÉCAPITULATIF – CE QUE J'AI APPRIS

---

### ✅ Synthèse du laboratoire

Ce laboratoire m'a permis de maîtriser la **communication Android ↔ Serveur** avec Volley, l'intégration de **Google Maps avec des markers dynamiques** chargés depuis une base de données, et la mise en place d'un **backend PHP/MySQL** complet.

---

### 📝 Les 5 points essentiels à retenir

| # | Point clé |
|---|-----------|
| 1 | **`StringRequest` vs `JsonObjectRequest`** : utiliser `JsonObjectRequest` quand le serveur retourne du JSON pour parser directement la réponse |
| 2 | **`getParams()`** : méthode à surcharger dans Volley pour envoyer des paramètres POST |
| 3 | **`android:usesCleartextTraffic="true"`** : obligatoire pour les requêtes HTTP (non-HTTPS) en Android 9+ |
| 4 | **Communication indirecte** : deux `Activity` peuvent échanger des données sans se connaître, via la base de données |
| 5 | **`addMarker()` en boucle** : contrairement au Lab 11, ici on ajoute plusieurs markers (un par position historique) |

---

### 📊 Comparaison Lab 11 vs Lab 12

| Fonctionnalité | Lab 11 | Lab 12 |
|----------------|--------|--------|
| Source des positions | GPS local | GPS → MySQL → Maps |
| Marker | 1 seul, déplacé | Plusieurs, historique complet |
| Réseau | ❌ | ✅ Volley POST |
| Serveur | ❌ | ✅ PHP/MySQL |
| Persistance | ❌ | ✅ Base de données |

---

### 💡 Bonnes pratiques retenues

- [x] Stocker la clé API dans `local.properties` (hors Git)
- [x] Vérifier les permissions runtime avant d'accéder au GPS
- [x] Utiliser `getDeviceIdentifier()` pour identifier l'appareil sans permissions sensibles
- [x] Toujours tester les endpoints PHP avec `curl` ou Postman avant l'intégration
- [x] Activer `usesCleartextTraffic` uniquement en développement (HTTPS en production)

---

### 🎯 Compétences acquises

| Compétence | Niveau |
|------------|--------|
| Envoyer des requêtes POST avec Volley | ✅ Maîtrisé |
| Parser une réponse JSON (`JSONArray`, `JSONObject`) | ✅ Maîtrisé |
| Créer un backend PHP avec PDO | ✅ Maîtrisé |
| Afficher des markers dynamiques sur Google Maps | ✅ Maîtrisé |
| Gérer la communication inter-activités via DB | ✅ Maîtrisé |
| Configurer XAMPP et tester une API REST | ✅ Maîtrisé |

---

### 👨‍💻 Auteur

| Élément | Information |
|---------|-------------|
| **Nom** | El Hachilmi Abdelhamid |
| **GitHub** | [abdotranscript25](https://github.com/abdotranscript25) |
| **Lab** | Programmation Mobile - Lab 12 |

---

### 📅 Version

| Élément | Information |
|---------|-------------|
| **Date** | Mai 2026 |
| **Version** | 1.0 |
| **Statut** | ✅ Finalisé |
