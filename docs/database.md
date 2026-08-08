# Base de données

## Objectif

RPGEngine utilise SQLite pour stocker de manière persistante les données nécessaires au moteur RPG.

La base de données contient actuellement :

* les triggers ;
* leurs conditions ;
* leurs actions ;
* les variables persistantes des joueurs ;
* la version du schéma de données.

Cette approche permet à RPGEngine de rester **data-driven** : le comportement RPG est décrit par des données plutôt que codé directement dans les scénarios Java.

---

# SQLite

RPGEngine utilise une base SQLite locale.

Le fichier est créé dans le dossier de données du plugin :

```text
plugins/RPGEngine/rpgengine.db
```

La connexion est gérée par :

```text
DatabaseManager
```

SQLite a été choisi notamment pour :

* sa simplicité de déploiement ;
* l'absence de serveur de base de données externe ;
* la possibilité d'utiliser des relations ;
* les contraintes d'intégrité ;
* les migrations versionnées ;
* les requêtes structurées.

---

# Architecture

Le système de persistance suit cette organisation :

```text
RPGEngine
    │
    ▼
DatabaseManager
    │
    ├── MigrationManager
    │
    └── Connection SQLite
             │
             ▼
         Repositories
             │
             ▼
           Tables
```

Le package `database` gère uniquement :

* la connexion ;
* le schéma ;
* les migrations.

L'accès métier aux données appartient aux repositories.

---

# DatabaseManager

`DatabaseManager` gère le cycle de vie de la connexion SQLite.

Ses responsabilités sont :

1. créer le dossier de données si nécessaire ;
2. ouvrir le fichier SQLite ;
3. activer les contraintes de clés étrangères ;
4. lancer les migrations ;
5. exposer la connexion aux composants qui en ont besoin ;
6. fermer proprement la connexion.

---

## Clés étrangères

SQLite nécessite l'activation explicite des contraintes de clés étrangères pour chaque connexion.

RPGEngine exécute donc :

```sql
PRAGMA foreign_keys = ON;
```

à l'ouverture de la base.

Cela permet à SQLite de vérifier les relations déclarées dans le schéma.

---

# MigrationManager

`MigrationManager` est responsable de l'évolution du schéma de données.

Au démarrage :

```text
Connexion SQLite
        │
        ▼
MigrationManager
        │
        ▼
Lecture schema_version
        │
        ▼
Migrations manquantes
        │
        ▼
Application dans l'ordre
```

Seules les migrations dont la version est supérieure à la version actuelle sont exécutées.

---

# Table schema_version

Cette table indique la version actuellement installée du schéma.

Structure :

```text
schema_version

version INTEGER
```

Exemple :

```text
version = 6
```

signifie que les migrations V1 à V6 ont été appliquées.

---

# Principe des migrations

Chaque évolution du schéma possède une migration dédiée.

Les migrations implémentent :

```text
Migration
```

qui définit :

```java
int getVersion();

void apply(Connection connection);
```

Une migration représente une transformation précise du schéma.

---

# Règle importante

Une migration déjà déployée sur une base utilisée ne doit plus être modifiée.

Si le schéma doit évoluer, une nouvelle migration doit être créée.

Exemple :

```text
V6 déjà déployée

Nouvelle évolution
        │
        ▼
Créer V7
```

Cela permet à une ancienne installation d'être automatiquement mise à jour vers la dernière version.

---

# Historique actuel des migrations

## V1 - Initialisation

Classe :

```text
V1_Init
```

Création de la table :

```text
trigger
```

Cette migration introduit le premier composant fondamental du moteur : le déclencheur d'une règle.

---

## V2 - Système d'actions

Classe :

```text
V2_ActionSystem
```

Création de :

```text
action
```

Une action est associée à un trigger par :

```text
action.trigger_id
        │
        ▼
trigger.id
```

Cette migration permet à un trigger de produire des effets.

---

## V3 - Système de conditions

Classe :

```text
V3_ConditionSystem
```

Création de :

```text
condition
```

Une condition est également associée à un trigger :

```text
condition.trigger_id
          │
          ▼
      trigger.id
```

Le moteur possède alors son modèle fondamental :

```text
Trigger
   │
   ├── Conditions
   └── Actions
```

---

## V4 - Variables joueur

Classe :

```text
V4_PlayerVariables
```

Création de :

```text
player_variable
```

Cette table introduit un état RPG persistant propre à chaque joueur.

Exemples :

```text
met_chief = true
```

```text
reputation = 50
```

```text
dialogue_chief_stage = 1
```

---

## V5 - Refactorisation des conditions

Classe :

```text
V5_RenameConditionTypeAndValue
```

Renommage :

```text
condition.type
        ↓
condition.provider
```

et :

```text
condition.value
        ↓
condition.expression
```

Cette évolution accompagne l'introduction du système générique de `ConditionProvider`.

---

## V6 - Refactorisation des actions

Classe :

```text
V6_RenameActionTypeAndValue
```

Renommage :

```text
action.type
        ↓
action.provider
```

et :

```text
action.value
        ↓
action.expression
```

Cette évolution accompagne l'introduction du système générique d'`ActionExecutor`.

---

## V7 - Système de dialogues

Classe :

```text
V7_DialogSystem
```

Cette migration introduit les tables :

```text
dialogue
dialogue_line
```

Elle permet aux actions du moteur de référencer des dialogues persistants grâce au provider :

```text
DIALOG
```

Le système de dialogues devient ainsi une extension naturelle du système d'actions sans modifier le cœur de RPGEngine.

---

# Modèle relationnel actuel

La base métier est organisée autour de quatre tables principales :

```text
                  trigger
                 /       \
                /         \
               ▼           ▼
         condition       action


          player_variable
```

`condition` et `action` appartiennent directement à un trigger.

`player_variable` représente un état persistant indépendant, propre à chaque joueur.

---

# Table trigger

La table `trigger` représente un événement capable de déclencher une règle.

Structure :

| Colonne     | Type    | Rôle                    |
| ----------- | ------- | ----------------------- |
| `id`        | INTEGER | Identifiant interne     |
| `name`      | TEXT    | Nom lisible du trigger  |
| `type`      | TEXT    | Type de déclencheur     |
| `target_id` | TEXT    | Identifiant de la cible |
| `enabled`   | INTEGER | Activation du trigger   |

---

## id

`id` est l'identifiant technique du trigger.

Il est utilisé par les tables :

```text
action
condition
```

pour établir leurs relations.

---

## name

`name` permet d'identifier humainement un trigger.

Exemple :

```text
Chef du village - première rencontre
```

Cette information est notamment utile pour :

* l'administration ;
* le debug ;
* les futurs outils de création de contenu.

---

## type

`type` représente la catégorie du déclencheur.

Exemples actuels :

```text
NPC

BLOCK

ENTITY

REGION

COMMAND
```

La valeur correspond à `TriggerType`.

---

## target_id

`target_id` identifie la cible précise du trigger.

Cette colonne utilise volontairement `TEXT`.

Elle peut donc représenter différents types d'identifiants :

```text
NPC
210
```

ou plus tard :

```text
REGION
village_center
```

```text
BLOCK
minecraft:lever
```

Le moteur n'impose pas de format numérique à la cible.

---

## enabled

Permet d'activer ou de désactiver un trigger sans supprimer sa configuration.

```text
1 = actif
0 = inactif
```

---

# Table condition

La table `condition` décrit les conditions associées à un trigger.

Structure actuelle :

| Colonne      | Type    | Rôle                 |
| ------------ | ------- | -------------------- |
| `id`         | INTEGER | Identifiant          |
| `trigger_id` | INTEGER | Trigger propriétaire |
| `provider`   | TEXT    | Source de données    |
| `expression` | TEXT    | Expression à évaluer |

Relation :

```text
condition.trigger_id
          │
          ▼
      trigger.id
```

---

## provider

Le provider indique quelle source doit résoudre la clé présente dans l'expression.

Exemple :

```text
PLAYER
```

---

## expression

Exemple :

```text
dialogue_chief_stage==false
```

Le contenu est interprété par :

```text
ExpressionParser
```

puis évalué par :

```text
ExpressionEvaluator
```

---

# Table action

La table `action` décrit les effets exécutés lorsqu'un trigger est validé.

Structure :

| Colonne      | Type    | Rôle                 |
| ------------ | ------- | -------------------- |
| `id`         | INTEGER | Identifiant          |
| `trigger_id` | INTEGER | Trigger propriétaire |
| `provider`   | TEXT    | Executor à utiliser  |
| `expression` | TEXT    | Données de l'action  |
| `position`   | INTEGER | Ordre d'exécution    |

Relation :

```text
action.trigger_id
        │
        ▼
    trigger.id
```

---

## provider

Détermine quel `ActionExecutor` doit traiter l'action.

Exemples :

```text
MESSAGE
```

```text
PLAYER
```

---

## expression

L'expression dépend du provider.

Exemple `MESSAGE` :

```text
Bienvenue aventurier !
```

Exemple `PLAYER` :

```text
gold+=10
```

---

## position

Lorsque plusieurs actions appartiennent au même trigger, `position` détermine leur ordre.

Exemple :

```text
1 MESSAGE Bienvenue aventurier !
2 MESSAGE Une grande aventure commence...
3 PLAYER  dialogue_chief_stage=1
```

Les actions sont exécutées dans cet ordre.

---

# Table player_variable

Cette table stocke les variables RPG persistantes des joueurs.

Structure :

| Colonne       | Type    | Rôle               |
| ------------- | ------- | ------------------ |
| `id`          | INTEGER | Identifiant        |
| `player_uuid` | TEXT    | UUID du joueur     |
| `key`         | TEXT    | Nom de la variable |
| `value`       | TEXT    | Valeur persistée   |

Une contrainte garantit l'unicité de :

```text
(player_uuid, key)
```

Un joueur ne peut donc posséder qu'une seule valeur pour une clé donnée.

---

# Valeurs génériques

Les valeurs de `player_variable` sont stockées sous forme de texte.

Exemples :

```text
true
```

```text
15
```

```text
12.5
```

```text
guardian
```

L'interprétation de leur type appartient aux systèmes qui les utilisent.

Par exemple, `ExpressionEvaluator` détecte dynamiquement les booléens, entiers, décimaux et chaînes de caractères.

La base de données reste ainsi générique.

---

# Exemple de règle complète

Prenons un PNJ Citizens avec l'identifiant :

```text
210
```

Premier trigger :

```text
trigger

name      = Chef - première rencontre
type      = NPC
target_id = 210
enabled   = 1
```

Condition :

```text
condition

provider   = PLAYER
expression = dialogue_chief_stage==false
```

Actions :

```text
MESSAGE
Bienvenue aventurier !

MESSAGE
Une grande aventure commence...

PLAYER
dialogue_chief_stage=1
```

Le moteur reconstruit alors :

```text
Trigger
   │
   ├── Condition
   │      │
   │      └── PLAYER
   │          dialogue_chief_stage==false
   │
   └── Actions
          │
          ├── MESSAGE
          ├── MESSAGE
          └── PLAYER
```

---

# Système de dialogues

Le système de dialogues utilise deux tables :

```
dialogue
   │
   ▼
dialogue_line
```

Un dialogue représente une ressource narrative identifiable par une clé métier.

Ses lignes sont stockées séparément afin de préserver leur ordre et de permettre l'évolution future du système.

# Table dialogue

| Colonne       | Type    | Rôle                    |
|---------------| ------- |-------------------------|
| `id`          | INTEGER | Identifiant interne     |
| `key`         | TEXT    | Clé métier unique       |
| `name`        | TEXT    | Nom lisible du dialogue |

Exemple :

```
provider = DIALOG

expression = chief_intro
```

# Table dialogue_line

| Colonne       | Type     | Rôle                    |
|---------------|----------|-------------------------|
| `id`          | INTEGER  | Identifiant interne     |
| `dialogue_id` | INTEGER  | Dialogue propriétaire   |
| `position`    | INTEGER  | Ordre d'affichage       |
| `text`        | TEXT     | Texte de la ligne       |

Relation :

```
dialogue_line.dialogue_id
     │
     ▼
dialogue.id
```

La combinaison (dialogue_id, position) est unique. Ainsi, deux lignes d'un même dialogue ne peuvent pas occuper la même position.

La suppression d'un dialogue entraîne également la suppression de ses lignes grâce à la relation ON DELETE CASCADE.

Exemple :

```
chief_intro

1 → Bienvenue aventurier ! 
2 → Une grande aventure commence...
```

---

# Repositories

Le package `repository` constitue la couche d'accès aux données métier.

Actuellement :

```text
TriggerRepository

ConditionRepository

ActionRepository

PlayerVariableRepository
```

Les repositories transforment les données SQL en objets Java et inversement.

Ils ne doivent contenir aucune logique de scénario.

---

# Chargement d'un trigger

`TriggerRepository` reconstruit un trigger complet.

```text
TriggerRepository
        │
        ├── lecture trigger
        │
        ├── ConditionRepository
        │       │
        │       └── List<Condition>
        │
        └── ActionRepository
                │
                └── List<Action>
```

Le moteur reçoit ainsi directement :

```text
Trigger
├── Conditions
└── Actions
```

---

# Séparation des responsabilités

La persistance suit une séparation stricte.

```text
DatabaseManager
→ connexion

MigrationManager
→ évolution du schéma

Migration
→ transformation versionnée

Repository
→ accès aux données métier

Manager
→ logique du moteur
```

Aucun `Manager` métier ne doit écrire directement du SQL.

Inversement, les composants du package `database` ne doivent pas décider comment une règle RPG doit se comporter.

---

# Transactions de migration

Une migration doit être considérée comme une opération atomique.

Le résultat attendu est :

```text
Migration entière réussie
        │
        ▼
COMMIT
        │
        ▼
schema_version mise à jour
```

En cas d'erreur :

```text
Erreur SQL
    │
    ▼
ROLLBACK
    │
    ▼
Version inchangée
```

Cela évite qu'une base reste dans un état partiellement migré.

---

# Logging

Les opérations techniques de la base utilisent `RpgLogger`.

Exemples :

```text
INFO
SQLite connecté.
```

```text
INFO
Migration V6 appliquée.
```

```text
ERROR
Echec de la migration V6 : ...
```

```text
ERROR
Impossible de fermer la connexion SQLite : ...
```

Aucune erreur SQL ne doit être envoyée directement au joueur.

Les règles générales sont décrites dans :

```text
logging.md
```

---

# Philosophie

SQLite est une couche de persistance, pas le moteur RPG.

La base décrit :

```text
ce qui existe
```

Les repositories déterminent :

```text
comment le charger
```

Les managers déterminent :

```text
comment l'utiliser
```

Cette séparation permet à RPGEngine de rester :

* data-driven ;
* modulaire ;
* testable ;
* évolutif.

---

# Résumé

Le système de persistance actuel repose sur :

```text
SQLite
  │
  ├── trigger
  │      ├── condition
  │      └── action
  │
  ├── player_variable
  │
  └── schema_version
```

L'évolution du schéma est contrôlée par des migrations versionnées.

Les données métier sont accessibles uniquement via les repositories.

Cette architecture permet de faire évoluer progressivement RPGEngine sans coupler le stockage à la logique RPG.
