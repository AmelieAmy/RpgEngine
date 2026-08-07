# Vue d'ensemble du moteur

## Objectif

RPGEngine est un moteur de règles destiné à construire des mécaniques RPG pour Minecraft.

Le moteur ne contient aucun scénario, aucune quête et aucun dialogue prédéfini.

Il exécute uniquement des règles décrites dans la base de données.

Chaque règle suit le même principe :

```text
Quand...
    Trigger

Si...
    Conditions

Alors...
    Actions
```

Le moteur ne connaît jamais le contenu du jeu.

Il applique simplement les règles qui lui sont fournies.

---

# Fonctionnement général

Le cycle complet d'un événement est le suivant :

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
    Liste des Triggers
            │
            ▼
   ConditionManager
            │
            ▼
    Triggers valides
            │
            ▼
     ActionManager
            │
            ▼
    Effets dans le jeu
```

Chaque composant possède une responsabilité unique.

---

# Les listeners

Les listeners reçoivent les événements provenant de Minecraft ou d'un plugin externe.

Exemple :

```text
NPCRightClickEvent

BlockBreakEvent

EntityDeathEvent
```

Ils ne contiennent aucune logique RPG.

Leur seule responsabilité est de construire un `TriggerContext`.

---

# TriggerContext

`TriggerContext` représente le contexte d'exécution d'un événement.

Il contient les informations communes nécessaires au moteur :

* joueur ;
* type de trigger ;
* identifiant de la cible.

Il peut également transporter des attributs spécifiques à l'événement.

Exemple :

```text
player

triggerType

targetId

location

entity

block
```

Le contexte est ensuite transmis à tous les composants du moteur.

---

# TriggerManager

`TriggerManager` est l'orchestrateur principal.

Il coordonne le traitement complet d'un événement.

Son rôle est de :

1. rechercher les triggers correspondants ;
2. vérifier leurs conditions ;
3. exécuter leurs actions.

Il ne contient aucune logique métier propre au gameplay.

---

# ConditionManager

Le système de conditions détermine si une règle est applicable.

Pour chaque condition :

```text
Condition

↓

ConditionProvider

↓

ExpressionEvaluator

↓

true / false
```

Toutes les conditions doivent être validées.

Les détails sont décrits dans **conditions.md**.

---

# ActionManager

Une fois les conditions validées, les actions sont exécutées.

Chaque action est confiée à un `ActionExecutor` spécialisé.

Exemple :

```text
MESSAGE

↓

MessageActionExecutor
```

ou

```text
PLAYER

↓

PlayerVariableActionExecutor
```

Les détails sont décrits dans **actions.md**.

---

# Les repositories

Les repositories constituent la couche d'accès aux données.

Ils lisent et écrivent les informations stockées dans SQLite.

Exemples :

```text
TriggerRepository

ConditionRepository

ActionRepository

PlayerVariableRepository
```

Ils ne contiennent aucune logique métier.

---

# La base de données

Le moteur est entièrement piloté par les données.

Les principales tables sont :

```text
trigger
    │
    ├── condition
    └── action

player_variable
```

Le fonctionnement détaillé est décrit dans **database.md**.

---

# Les principes de conception

RPGEngine repose sur quelques principes simples.

## Responsabilité unique

Chaque composant possède un rôle clairement défini.

```text
Listener
    reçoit un événement

TriggerManager
    orchestre

ConditionManager
    valide

ActionManager
    exécute

Repository
    accède aux données
```

---

## Data-driven

Le moteur ne contient pas les règles du jeu.

Toutes les règles sont décrites dans la base de données.

Le code Java décrit uniquement le fonctionnement du moteur.

---

## Extensibilité

Chaque sous-système est conçu pour être étendu sans modifier le cœur du moteur.

Par exemple :

* ajouter un nouveau `TriggerType` ;
* ajouter un nouveau `ConditionProvider` ;
* ajouter un nouvel `ActionExecutor`.

Le moteur reste inchangé.

---

# Documentation

Les documents suivants détaillent les différents sous-systèmes :

* **triggers.md** : fonctionnement des triggers ;
* **conditions.md** : système de conditions ;
* **actions.md** : système d'actions ;
* **database.md** : modèle de données SQLite ;
* **logging.md** : politique de journalisation.
