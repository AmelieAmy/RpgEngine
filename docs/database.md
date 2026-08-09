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

Le système de dialogues utilise un modèle narratif orienté graphe.

Un dialogue n'est pas représenté comme une simple liste de lignes.

Il est composé :

* de nodes narratifs ;
* de transitions reliant ces nodes ;
* de conditions pouvant contrôler les transitions ;
* d'actions exécutées lors du passage d'une transition.

```text
Dialogue
   │
   ├── DialogueNode
   │
   └── DialogueTransition
           │
           ├── Conditions
           └── Actions
```

Cette architecture permet au système de dialogue de réutiliser directement le Rule Engine existant.

Les conditions sont évaluées par `ConditionManager`.

Les actions sont exécutées par `ActionManager`.

Le système de dialogue ne possède donc aucune logique parallèle de conditions ou d'actions.

---

## Table dialogue

| Colonne         | Type    | Rôle                |
| --------------- | ------- | ------------------- |
| `id`            | INTEGER | Identifiant interne |
| `key`           | TEXT    | Clé métier unique   |
| `name`          | TEXT    | Nom lisible         |
| `start_node_id` | INTEGER | Node de départ      |

Exemple :

```text
key  = chief_intro
name = Chef du village - Introduction
```

`start_node_id` indique le point d'entrée du graphe narratif.

Cette valeur peut temporairement être absente pendant l'édition d'un dialogue.

Un dialogue ne peut cependant pas être considéré comme exécutable tant qu'un node de départ valide n'est pas défini.

Cette cohérence est vérifiée par le système de validation des dialogues.

---

## Table dialogue_node

Un node représente une unité narrative atomique.

Il contient un seul texte.

| Colonne       | Type    | Rôle                         |
| ------------- | ------- | ---------------------------- |
| `id`          | INTEGER | Identifiant interne          |
| `dialogue_id` | INTEGER | Dialogue propriétaire        |
| `key`         | TEXT    | Clé du node dans le dialogue |
| `text`        | TEXT    | Contenu narratif             |

La combinaison :

```text
(dialogue_id, key)
```

est unique.

Exemple :

```text
key  = offer
text = Peux-tu m'aider à retrouver mon marteau ?
```

Un node ne contient :

* aucune condition ;
* aucune action ;
* aucune information de navigation.

La navigation appartient exclusivement aux transitions.

---

## Table dialogue_transition

Une transition représente un passage explicite entre deux nodes.

| Colonne          | Type    | Rôle                         |
| ---------------- | ------- | ---------------------------- |
| `id`             | INTEGER | Identifiant                  |
| `dialogue_id`    | INTEGER | Dialogue propriétaire        |
| `source_node_id` | INTEGER | Node de départ               |
| `target_node_id` | INTEGER | Node cible                   |
| `type`           | TEXT    | Type de transition           |
| `label`          | TEXT    | Texte présenté pour un choix |
| `position`       | INTEGER | Ordre de présentation        |

Les transitions disponibles sont actuellement :

```text
AUTO
CHOICE
END
```

### AUTO

Une transition `AUTO` poursuit automatiquement le dialogue.

```text
Node A
   │
   ▼
Node B
```

Elle possède obligatoirement une cible et aucun label.

### CHOICE

Une transition `CHOICE` représente une décision proposée au joueur.

```text
"Acceptes-tu ?"

├── Oui
└── Non
```

Elle possède :

* une cible ;
* un label ;
* une position permettant d'ordonner les choix.

### END

Une transition `END` termine explicitement le dialogue.

```text
Node
 │
 ▼
END
```

Elle ne possède :

* aucune cible ;
* aucun label.

Un node sans transition ne signifie jamais implicitement que le dialogue est terminé.

Il représente une structure incomplète ou invalide.

---

## Conditions des transitions

La table :

```text
dialogue_transition_condition
```

associe des conditions à une transition.

Structure :

| Colonne         | Type    | Rôle                    |
| --------------- | ------- | ----------------------- |
| `id`            | INTEGER | Identifiant             |
| `transition_id` | INTEGER | Transition propriétaire |
| `provider`      | TEXT    | Source de données       |
| `expression`    | TEXT    | Expression à vérifier   |

Ces données sont reconstruites sous forme d'objets `Condition`.

Leur évaluation est ensuite déléguée au `ConditionManager` existant.

Le système de dialogue ne possède donc aucun évaluateur spécifique.

---

## Actions des transitions

La table :

```text
dialogue_transition_action
```

associe des actions à une transition.

Structure :

| Colonne         | Type    | Rôle                    |
| --------------- | ------- | ----------------------- |
| `id`            | INTEGER | Identifiant             |
| `transition_id` | INTEGER | Transition propriétaire |
| `provider`      | TEXT    | Executor                |
| `expression`    | TEXT    | Expression de l'action  |
| `position`      | INTEGER | Ordre d'exécution       |

Ces données sont reconstruites sous forme d'objets `Action`.

Leur exécution est déléguée au `ActionManager`.

---

## Exemple

```text
Node : offer

"Peux-tu m'aider ?"
```

peut posséder :

```text
Transition 1

type   = CHOICE
label  = Oui
target = accept
```

avec :

```text
Action

PLAYER
quest_started=true
```

et :

```text
Transition 2

type   = CHOICE
label  = Non
target = goodbye
```

Le runtime devient :

```text
DialogueRunner
        │
        ▼
DialogueNode
        │
        ▼
DialogueTransition
        │
        ├── ConditionManager
        │
        ▼
   choix disponible
        │
        ▼
   joueur choisit
        │
        ├── ActionManager
        │
        ▼
    node suivant
```

---

## Validation

SQLite garantit l'intégrité locale des relations et certaines contraintes élémentaires.

La cohérence globale du graphe appartient à `DialogueValidator`.

Il vérifiera notamment :

* l'existence du node de départ ;
* l'existence des sources et cibles ;
* l'appartenance des nodes au dialogue ;
* l'existence explicite de fins ;
* les nodes inaccessibles ;
* les éventuelles boucles automatiques infinies.

Le moteur ne déduit jamais la structure d'un dialogue.

Il exécute uniquement ce qui est explicitement décrit.

Toute ambiguïté est considérée comme une erreur de conception.

---

## V8 - Graphe narratif

Classe :

```text
V8_DialogGraphSystem
```

Cette migration remplace le système linéaire introduit par V7.

L'ancienne table :

```text
dialogue_line
```

est supprimée.

Elle est remplacée par :

```text
dialogue_node

dialogue_transition

dialogue_transition_condition

dialogue_transition_action
```

Cette évolution transforme le dialogue d'une simple séquence de textes en un graphe narratif capable d'exploiter directement les conditions et les actions du Rule Engine.

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
