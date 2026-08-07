# Le système d'actions

## Objectif

Le système d'actions est responsable de l'exécution des effets produits par une règle du moteur RPG.

Une action ne contient aucune logique métier. Elle décrit uniquement **ce qui doit être exécuté**.

Exemple :

```text
MESSAGE
Bienvenue aventurier !
```

ou

```text
PLAYER
gold+=10
```

Le moteur interprète ensuite cette description et délègue son exécution au composant approprié.

---

# Cycle d'exécution

Lorsqu'un trigger est validé, `TriggerManager` transmet chacune de ses actions à `ActionManager`.

```
TriggerManager
        │
        ▼
ActionManager
        │
        ▼
ActionExecutor
        │
        ▼
Effet dans le jeu
```

Le `TriggerContext` est transmis durant toute cette chaîne afin que chaque executor dispose des informations nécessaires (joueur, type de trigger, cible, attributs…).

---

# ActionManager

`ActionManager` orchestre l'exécution des actions.

Il possède un registre d'`ActionExecutor`, indexés par leur provider.

Exemple :

```
MESSAGE
        │
        ▼
MessageActionExecutor

PLAYER
        │
        ▼
PlayerVariableActionExecutor
```

Le manager ne connaît jamais le fonctionnement interne des executors.

Sa responsabilité est uniquement :

* sélectionner le bon executor ;
* lui transmettre le contexte ;
* lui transmettre l'action.

Cette séparation permet d'ajouter de nouveaux types d'actions sans modifier le moteur.

---

# ActionExecutor

`ActionExecutor` est le contrat commun de toutes les actions.

Chaque implémentation est responsable d'un seul provider.

Exemple :

```
MESSAGE

PLAYER

QUEST

DIALOG
```

Chaque executor applique uniquement la logique correspondant à son provider.

---

# Executors disponibles

## MESSAGE

Le provider `MESSAGE` envoie simplement le contenu de l'expression au joueur.

Exemple :

```
provider = MESSAGE

expression = Bienvenue aventurier !
```

Produit :

```
Bienvenue aventurier !
```

---

## PLAYER

Le provider `PLAYER` modifie une variable du joueur.

Exemple :

```
provider = PLAYER

expression = gold+=10
```

Cette action :

* analyse l'expression ;
* lit la valeur actuelle ;
* calcule la nouvelle valeur ;
* sauvegarde le résultat.

---

# Le sous-système d'affectation

Les modifications de variables utilisent un sous-système dédié.

```
PlayerVariableActionExecutor
            │
            ▼
AssignmentParser
            │
            ▼
Assignment
            │
            ▼
AssignmentExecutor
            │
            ▼
Nouvelle valeur
```

Ce sous-système est totalement indépendant :

* de Bukkit ;
* de SQLite ;
* des variables joueur.

Il peut être réutilisé pour tout système manipulant des affectations.

---

## AssignmentParser

Analyse une expression d'affectation.

Exemple :

```
gold+=10
```

devient :

```
key      = gold

operator = ADD

value    = 10
```

---

## AssignmentExecutor

Applique une affectation sur une valeur existante.

Exemple :

```
Valeur actuelle :

10

Expression :

+=5
```

Résultat :

```
15
```

Cette classe ne connaît pas l'origine des données.

Elle applique uniquement la logique des opérateurs.

---

## AssignmentOperator

Le moteur supporte actuellement les opérateurs suivants :

```
=

+=

-=

*=

/=
```

L'ajout d'un nouvel opérateur nécessite uniquement :

* une nouvelle valeur dans `AssignmentOperator` ;
* son implémentation dans `AssignmentExecutor`.

---

# Ajouter un nouvel ActionExecutor

Pour ajouter une nouvelle action :

1. créer une implémentation de `ActionExecutor` ;
2. retourner le provider correspondant ;
3. enregistrer l'executor dans `ActionManager`.

Exemple :

```
Provider :

DIALOG
```

↓

```
DialogActionExecutor
```

Aucune modification de `ActionManager` n'est nécessaire.

---

# Philosophie

Le système d'actions applique plusieurs principes fondamentaux de RPGEngine.

## Responsabilité unique

Chaque classe possède une responsabilité clairement définie.

* `ActionManager` orchestre.
* `ActionExecutor` applique un effet.
* `AssignmentParser` analyse une expression.
* `AssignmentExecutor` calcule une nouvelle valeur.
* `PlayerVariableRepository` lit et écrit les données.

Aucune classe ne cumule plusieurs responsabilités.

---

## Ouvert à l'extension

Le moteur ne connaît pas les providers existants.

L'ajout d'un nouveau provider consiste uniquement à enregistrer un nouvel executor.

Le cœur du moteur reste inchangé.

---

## Data-driven

Les actions sont entièrement décrites dans la base de données.

Le moteur n'exécute pas du code spécifique à une quête ou à un scénario.

Il interprète des données.

Cette approche permet de créer de nouveaux comportements sans modifier le code Java.

---

# Résumé

Le système d'actions est responsable de l'application des effets d'une règle.

Il repose sur une architecture simple :

```
TriggerManager
        │
        ▼
ActionManager
        │
        ▼
ActionExecutor
        │
        ▼
Effet dans le jeu
```

Cette architecture est conçue pour être facilement extensible tout en conservant un moteur indépendant du contenu du jeu.
