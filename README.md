# MapApplication — Android + OSMDroid + PHP/MySQL

## Structure du projet

```
MapApplication/
├── app/src/main/
│   ├── java/com/example/mapapplication/
│   │   ├── MainActivity.java      → Suivi GPS + envoi serveur
│   │   └── MapActivity.java       → Carte OSMDroid + marqueurs
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml  → UI avec infos GPS en temps réel
│   │   │   └── activity_map.xml   → Carte plein écran
│   │   ├── drawable/
│   │   │   ├── card_background.xml
│   │   │   └── marker.xml         → Marqueur vectoriel
│   │   └── values/
│   │       ├── strings.xml
│   │       ├── colors.xml
│   │       └── themes.xml
│   └── AndroidManifest.xml
├── backend/
│   ├── schema.sql           → Script de création de la BDD
│   ├── createPosition.php   → API POST pour enregistrer une position
│   └── getPosition.php      → API GET pour récupérer les positions
└── gradle.properties        → useAndroidX=true + Jetifier
```

## Configuration Gradle
- AGP : 8.2.2
- Gradle : 8.9 (compatible Java 21)
- OSMDroid : 6.1.18
- Volley : 1.2.1

## Backend (XAMPP/WAMP)
1. Copier le dossier `backend/` dans `htdocs/map_project/`
2. Importer `schema.sql` dans phpMyAdmin
3. Lancer Apache + MySQL

## Fonctionnalités
- Suivi GPS avec affichage lat/lng/altitude/précision
- Envoi automatique au serveur toutes les 150m ou 1 min
- Carte OSMDroid avec marqueurs cliquables (titre + date)
- Bouton Actualiser pour recharger les positions
- Gestion du cycle de vie de la carte (onResume/onPause/onDestroy)
