# RPGEngine Documentation

Bienvenue dans la documentation de RPGEngine.

RPGEngine est un moteur de gameplay RPG **data-driven** pour Minecraft.

Son objectif est de fournir une architecture flexible permettant de créer
des mécaniques RPG complexes sans devoir coder chaque comportement directement
en Java.

---

# Vision du projet

RPGEngine repose sur un principe simple :

```
Quand...
    Trigger

Si...
    Conditions

Alors...
    Actions
```

Chaque comportement du jeu est représenté par une règle.

Exemples :

- parler à un PNJ ;
- démarrer une quête ;
- modifier une réputation ;
- lancer un événement ;
- déclencher une cinématique ;
- distribuer une récompense.

Le moteur ne connaît pas le scénario.
Il exécute des règles définies par les données.

---

# Fonctionnement général

Une règle RPG suit ce cycle :

```
Minecraft Event

        |
        v

Trigger

        |
        v

Conditions

        |
        v

Actions
```

Exemple :

```
Le joueur clique sur un PNJ

        |

Le joueur possède la réputation nécessaire

        |

Afficher un dialogue
Donner une quête
Modifier une variable
```

---

# Principes d'architecture

RPGEngine applique plusieurs principes :

## Séparation moteur / contenu

Le code Java contient le moteur.

Les données contiennent le gameplay.

Cela permet :

- d'ajouter du contenu sans modifier le code ;
- de créer des scénarios complexes ;
- de faciliter la maintenance.

---

## Architecture modulaire

Chaque composant possède une responsabilité claire :

| Composant | Responsabilité |
|---|---|
| Listener | Capture les événements Minecraft |
| TriggerManager | Recherche les règles à exécuter |
| ConditionManager | Vérifie les conditions |
| ActionManager | Exécute les actions |
| Provider | Fournit des données |
| Executor | Réalise une action |
| Repository | Accès aux données |

---

# Fonctionnalités actuelles

## Système de triggers

Support actuel :

- PNJ Citizens
- événements Minecraft

---

## Système de conditions

Conditions basées sur :

- variables joueur ;
- expressions ;
- comparateurs.

Exemples :

```
PLAYER
met_chief == true
```

```
PLAYER
level >= 10
```

---

## Système d'actions

Actions actuellement disponibles :

### MESSAGE

Affiche un message au joueur.

Exemple :

```
Bienvenue aventurier !
```

---

### PLAYER

Modification de variables joueur.

Exemples :

```
met_chief=true

gold+=10

reputation-=5
```

---

## Base de données

RPGEngine utilise SQLite.

Les données persistées comprennent :

- triggers ;
- conditions ;
- actions ;
- variables joueur ;
- progression.

Le schéma évolue grâce à un système de migrations.

---

# Fonctionnalités prévues

- système de quêtes ;
- objectifs de quêtes ;
- dialogues ;
- réputation ;
- factions ;
- récompenses ;
- intégration FTB Quests ;
- intégration WorldGuard ;
- intégration MythicMobs ;
- événements dynamiques.

---

# Documentation

```
docs/
│
├── diagrams/
│   │
│   ├── action-flow.puml
│   ├── architecture.puml
│   ├── condition-flow.puml
│   ├── database.puml
│   └── trigger-flow.puml
│
├── engine/
│   │
│   ├── overview.md               <-- Fonctionnement résumé
│   ├── triggers.md               <-- Fonctionnement des triggers
│   ├── conditions.md             <-- Système de conditions
│   └── actions.md                <-- Système d'actions
│
├── README.md                     <-- Point d'entrée documentation
├── philosophy.md                 <-- Vision et objectifs du projet
├── database.md                   <-- Modèle de données SQLite
└── loggin.md                     <-- Politique de journalisation
```

## Architecture

Comprendre la structure interne :

[architecture.md](architecture.md)

---

## Packages

Description du code source :

[packages.md](packages.md)

---

## Moteur de règles

Fonctionnement du système central :

[engine.md](engine.md)

---

## Base de données

Structure SQLite :

[database.md](database.md)

---

## Triggers

Système d'événements :

[triggers.md](core-engine/triggers.md)

---

## Conditions

Système de validation :

[conditions.md](core-engine/conditions.md)

---

## Actions

Système d'exécution :

[actions.md](core-engine/actions.md)

---

## Philosophie

Vision globale du projet :

[philosophy.md](philosophy.md)

---

## Décisions techniques

Choix d'architecture :

[design-decisions.md](design-decisions.md)

---

# Etat du projet

RPGEngine est actuellement en développement actif.

L'objectif est de construire un moteur RPG extensible capable de gérer
des systèmes complets de gameplay tout en restant indépendant des plugins
externes.