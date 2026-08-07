# Le système de triggers

## Objectif

Le système de triggers constitue le point d'entrée du moteur RPG.

Son rôle est de transformer un événement provenant de Minecraft ou d'une intégration externe en une règle du moteur.

Le moteur ne réagit jamais directement aux événements Bukkit.

Les listeners construisent un `TriggerContext`, puis le transmettent au moteur.

---

# Vue d'ensemble

Le fonctionnement général est le suivant :

```text
Minecraft / Plugin externe
            │
            ▼
        Listener
            │
            ▼
     TriggerContext
            │
            ▼
     TriggerManager
            │
            ▼
    TriggerRepository
            │
            ▼
     Liste<Trigger>
            │
            ▼
   Evaluation des conditions
            │
            ▼
   Exécution des actions
```

Le moteur ne dépend donc pas directement de Bukkit.

Les listeners jouent uniquement le rôle d'adaptateurs.

---

# TriggerContext

`TriggerContext` représente le contexte d'exécution d'un trigger.

Il est construit par un listener puis transmis au moteur.

Cette classe est immuable.

---

## Informations communes

Chaque contexte contient toujours :

* le joueur ;
* le type de trigger ;
* l'identifiant de la cible.

Exemple :

```text
player     = Steve
triggerType = NPC
targetId    = 210
```

Ces informations sont disponibles pour tous les providers et tous les executors.

---

## Attributs

Chaque événement peut également transporter des informations supplémentaires.

Exemple :

```text
location
block
entity
clickedFace
```

Ces données sont stockées dans les attributs du contexte.

Elles permettent d'étendre le moteur sans modifier la structure de `TriggerContext`.

---

## Construction

Les contextes sont créés grâce au Builder.

Exemple :

```java
TriggerContext context =
        TriggerContext.builder()
                .player(player)
                .triggerType(TriggerType.NPC)
                .targetId(String.valueOf(npc.getId()))
                .build();
```

Le Builder garantit qu'un contexte valide est toujours transmis au moteur.

---

# TriggerType

`TriggerType` décrit la catégorie d'un déclencheur.

Exemples :

```text
NPC

BLOCK

ENTITY

COMMAND

REGION
```

Chaque listener est généralement responsable d'un ou plusieurs `TriggerType`.

---

# TriggerManager

`TriggerManager` orchestre entièrement le traitement d'un événement.

Il ne contient aucune logique métier propre aux quêtes, dialogues ou scénarios.

Son rôle consiste à :

1. rechercher les triggers concernés ;
2. évaluer leurs conditions ;
3. exécuter leurs actions.

---

# Recherche des triggers

La première étape consiste à rechercher les règles correspondant au contexte courant.

```text
TriggerRepository
        │
        ▼
TriggerType

+

TargetId
```

Exemple :

```text
NPC

210
```

Le repository retourne tous les triggers correspondant à cette cible.

---

# Evaluation des conditions

Toutes les conditions sont évaluées avant toute exécution d'action.

```text
Trigger
        │
        ▼
ConditionManager
        │
        ▼
true / false
```

Cette étape produit la liste des triggers valides.

---

# Evaluation en deux phases

Le moteur fonctionne volontairement en deux étapes.

## Première phase

Toutes les conditions sont évaluées.

Aucune action n'est exécutée.

```text
Trigger A

↓

true

Trigger B

↓

false

Trigger C

↓

true
```

Le moteur construit alors :

```text
A

C
```

---

## Deuxième phase

Les actions des triggers retenus sont exécutées.

```text
Trigger A
        │
        ▼
Actions

Trigger C
        │
        ▼
Actions
```

Cette approche garantit que les actions d'un trigger ne peuvent pas modifier le résultat des conditions d'un autre trigger pendant le même événement.

Le résultat est donc indépendant de l'ordre d'exécution.

---

# Exécution complète

Une fois les triggers sélectionnés :

```text
Trigger
      │
      ├── Conditions
      │
      └── Actions
```

Le traitement est :

```text
Trigger
    │
    ▼
ConditionManager
    │
    ▼
ActionManager
```

---

# Les listeners

Les listeners Bukkit ne contiennent aucune logique RPG.

Leur responsabilité est uniquement :

* recevoir un événement Minecraft ;
* construire un `TriggerContext` ;
* appeler `TriggerManager`.

Exemple :

```text
NPCRightClickEvent
        │
        ▼
NPCListener
        │
        ▼
TriggerContext
        │
        ▼
TriggerManager
```

Cette séparation permet d'intégrer facilement d'autres plugins sans modifier le cœur du moteur.

---

# Les intégrations externes

Les intégrations externes (Citizens, WorldGuard, etc.) ne contiennent pas la logique métier.

Elles fournissent simplement des événements.

Le moteur reste indépendant de ces plugins.

---

# Exemple complet

Un joueur clique sur un PNJ Citizens.

Le listener construit :

```text
player      = Steve

triggerType = NPC

targetId    = 210
```

Le moteur recherche tous les triggers :

```text
type = NPC

target_id = 210
```

Supposons deux règles.

Premier trigger :

```text
Condition

dialogue_stage==false
```

Deuxième trigger :

```text
Condition

dialogue_stage==1
```

Si la variable vaut :

```text
false
```

le moteur :

* valide le premier trigger ;
* rejette le second.

Ensuite seulement il exécute les actions du premier trigger.

Si celui-ci modifie :

```text
dialogue_stage=1
```

cela n'influence pas l'évaluation du second trigger, qui a déjà été réalisée.

---

# Ajout d'un nouveau TriggerType

Pour ajouter un nouveau type de trigger :

1. ajouter une valeur dans `TriggerType` ;
2. créer un listener ou une intégration ;
3. construire un `TriggerContext` ;
4. appeler `TriggerManager`.

Aucune modification de `TriggerManager` n'est nécessaire.

---

# Philosophie

Le système de triggers applique plusieurs principes fondamentaux de RPGEngine.

## Adaptation

Les listeners adaptent les événements externes au moteur.

Ils ne contiennent aucune logique métier.

---

## Orchestration

`TriggerManager` coordonne le traitement.

Il ne connaît ni Bukkit, ni Citizens, ni les scénarios du jeu.

---

## Séparation des responsabilités

Chaque composant possède un rôle précis.

* Listener → reçoit un événement.
* TriggerContext → transporte le contexte.
* TriggerRepository → recherche les règles.
* ConditionManager → valide les conditions.
* ActionManager → exécute les actions.

---

## Data-driven

Le moteur ne connaît jamais les règles du jeu.

Il interprète les triggers décrits dans la base de données.

---

## Extensibilité

L'ajout d'un nouveau type de trigger consiste uniquement à produire un nouveau `TriggerContext`.

Le cœur du moteur reste inchangé.

---

# Résumé

Le système de triggers est le point d'entrée de RPGEngine.

Il relie les événements du jeu au moteur de règles.

Son architecture repose sur une chaîne simple :

```text
Listener
    │
    ▼
TriggerContext
    │
    ▼
TriggerManager
    │
    ├── TriggerRepository
    ├── ConditionManager
    └── ActionManager
```

Cette architecture garantit que le moteur reste indépendant de Minecraft tout en étant facilement extensible à de nouveaux types de triggers et à de nouvelles intégrations.
