# Le système de conditions

## Objectif

Le système de conditions détermine si une règle RPG peut être exécutée.

Une condition ne contient pas directement la valeur à vérifier. Elle décrit :

* une source de données (`provider`) ;
* une expression à évaluer.

Exemple :

```text
provider = PLAYER

expression = reputation>=50
```

Le moteur demande d’abord au provider de résoudre la valeur de `reputation`, puis compare cette valeur avec `50`.

---

# Cycle d'évaluation

Lorsqu’un trigger est trouvé, `TriggerManager` transmet ses conditions à `ConditionManager`.

```text
TriggerManager
        │
        ▼
ConditionManager
        │
        ├── ExpressionParser
        │
        ├── ConditionProvider
        │
        └── ExpressionEvaluator
```

Le `TriggerContext` est transmis durant toute l’évaluation afin que les providers puissent accéder au contexte complet de l’événement.

---

# ConditionManager

`ConditionManager` orchestre l’évaluation des conditions.

Ses responsabilités sont :

1. sélectionner le bon `ConditionProvider` ;
2. analyser l’expression ;
3. demander au provider de résoudre la valeur actuelle ;
4. transmettre cette valeur à `ExpressionEvaluator`.

`ConditionManager` ne connaît pas la provenance réelle des données.

Il ne sait pas si une valeur provient :

* d’une variable joueur ;
* d’une quête ;
* d’une permission ;
* d’un item ;
* d’une intégration externe.

Cette responsabilité appartient aux providers.

---

# Evaluation multiple

Une règle peut posséder plusieurs conditions.

Actuellement, toutes les conditions doivent être valides.

```text
Condition 1
     │
     ▼
true
     │
     ▼
Condition 2
     │
     ▼
true
     │
     ▼
Règle valide
```

L’évaluation utilise un comportement de court-circuit.

Dès qu’une condition est invalide :

```text
false
```

l’évaluation s’arrête immédiatement et la règle est rejetée.

Un trigger sans condition est toujours considéré comme valide.

---

# ConditionProvider

`ConditionProvider` est le contrat commun permettant au moteur de récupérer une donnée.

Un provider répond à la question :

> Quelle est la valeur actuelle associée à cette clé dans ce contexte ?

Il ne décide jamais si la condition est vraie ou fausse.

Exemple :

```text
provider = PLAYER
key = reputation
```

Le provider pourrait retourner :

```text
75
```

L’évaluation de :

```text
reputation>=50
```

est ensuite réalisée par `ExpressionEvaluator`.

---

# TriggerContext

Les providers reçoivent un `TriggerContext`.

Celui-ci contient les informations relatives à l’événement courant, notamment :

* le joueur ;
* le type de trigger ;
* l’identifiant de la cible ;
* les attributs supplémentaires de l’événement.

Ainsi, les providers ne sont plus limités aux seules informations du joueur.

Une future condition pourra par exemple utiliser :

* un bloc ;
* une entité ;
* une région ;
* une position ;
* une donnée fournie par une intégration externe.

Sans modifier le contrat de `ConditionProvider`.

---

# Providers disponibles

## PLAYER

Le provider `PLAYER` lit les variables persistantes associées au joueur courant.

Il utilise :

```text
PlayerVariableRepository
```

Exemple :

```text
provider = PLAYER

expression = met_chief==true
```

Le provider résout :

```text
met_chief
```

à partir de la base de données du joueur.

---

## Valeur par défaut

Lorsqu’une variable joueur n’existe pas ou contient une valeur vide, le provider `PLAYER` l’interprète actuellement comme :

```text
false
```

Cela permet d’écrire directement :

```text
met_chief==false
```

sans initialiser explicitement la variable pour chaque joueur.

---

# Le sous-système d'expression

L’analyse des conditions repose sur un sous-système autonome.

```text
Expression textuelle
        │
        ▼
ExpressionParser
        │
        ▼
Expression
        │
        ▼
ExpressionEvaluator
        │
        ▼
true / false
```

Ce sous-système est indépendant :

* de Bukkit ;
* de SQLite ;
* des providers ;
* des scénarios RPG.

---

# Expression

`Expression` représente une comparaison analysée.

Exemple :

```text
gold>=100
```

devient :

```text
key      = gold

operator = GREATER_OR_EQUAL

value    = 100
```

`Expression` ne contient aucune logique métier.

---

# ExpressionParser

`ExpressionParser` transforme une chaîne de caractères en objet `Expression`.

Exemple :

```text
reputation>=50
```

devient :

```text
key      = reputation
operator = GREATER_OR_EQUAL
value    = 50
```

L’ordre de détection des opérateurs est important.

Les opérateurs composés sont évalués avant leurs variantes simples.

Par exemple :

```text
>=
```

doit être détecté avant :

```text
>
```

---

# ComparisonOperator

Les opérateurs actuellement supportés sont :

| Opérateur | Signification     |
| --------- | ----------------- |
| `==`      | égal              |
| `!=`      | différent         |
| `>`       | supérieur         |
| `>=`      | supérieur ou égal |
| `<`       | inférieur         |
| `<=`      | inférieur ou égal |

---

# ExpressionEvaluator

`ExpressionEvaluator` compare une valeur courante avec une expression.

Il ne connaît pas l’origine de la donnée.

Il reçoit uniquement :

```text
Valeur actuelle

+

Expression
```

et retourne :

```text
true

ou

false
```

---

# Détection dynamique des types

Les valeurs sont stockées sous forme de chaînes, mais l’evaluator détermine automatiquement leur type.

L’ordre actuel est :

```text
BOOLEAN

puis

INTEGER

puis

DOUBLE

puis

STRING
```

---

## Booléens

Exemple :

```text
currentValue = true

expectedValue = false
```

Les opérateurs supportés sont :

```text
==

!=
```

---

## Entiers

Exemple :

```text
10>=5
```

Tous les opérateurs de comparaison sont supportés.

---

## Nombres décimaux

Exemple :

```text
10.5>8.2
```

Tous les opérateurs de comparaison sont supportés.

---

## Chaînes de caractères

Exemple :

```text
rank==guardian
```

Les chaînes supportent actuellement uniquement :

```text
==

!=
```

Les opérateurs :

```text
>

>=

<

<=
```

ne sont pas définis pour les chaînes et produisent une condition invalide.

---

# Exemple complet

Condition en base :

```text
provider = PLAYER

expression = reputation>=50
```

Supposons que la variable du joueur soit :

```text
reputation = 75
```

Le traitement est :

```text
ConditionManager
        │
        ▼
ExpressionParser
        │
        ▼
key = reputation
operator = >=
value = 50
        │
        ▼
PlayerConditionProvider
        │
        ▼
75
        │
        ▼
ExpressionEvaluator
        │
        ▼
75 >= 50
        │
        ▼
true
```

La règle peut alors poursuivre son exécution.

---

# Ajouter un nouveau ConditionProvider

Pour ajouter une nouvelle source de données :

1. créer une implémentation de `ConditionProvider` ;
2. retourner le nom du provider ;
3. implémenter la résolution des clés ;
4. enregistrer le provider dans `ConditionManager`.

Exemple futur :

```text
provider = QUEST
```

↓

```text
QuestConditionProvider
```

Ou :

```text
provider = PERMISSION
```

↓

```text
PermissionConditionProvider
```

Aucune modification de `ConditionManager` n’est nécessaire.

---

# Philosophie

Le système de conditions applique plusieurs principes fondamentaux de RPGEngine.

## Séparation entre résolution et évaluation

Un provider récupère une donnée.

```text
ConditionProvider
        │
        ▼
Valeur
```

L’evaluator compare cette donnée.

```text
Valeur
        │
        ▼
ExpressionEvaluator
        │
        ▼
true / false
```

Ces responsabilités ne doivent jamais être mélangées.

---

## Responsabilité unique

Chaque composant possède un rôle précis.

* `ConditionManager` orchestre.
* `ConditionProvider` résout une valeur.
* `ExpressionParser` analyse une expression.
* `ExpressionEvaluator` compare les valeurs.
* `Repository` accède aux données.

---

## Ouvert à l'extension

Le cœur du moteur ne connaît pas les providers disponibles.

Ajouter une nouvelle source de données consiste à enregistrer un nouveau `ConditionProvider`.

Le système existant reste inchangé.

---

## Data-driven

Les conditions sont décrites dans la base de données.

Exemple :

```text
PLAYER

reputation>=50
```

Le code Java ne contient aucune logique spécifique à une quête ou à un scénario.

Le moteur interprète simplement les données configurées.

---

# Symétrie avec le système d'actions

Les conditions et les actions suivent une architecture volontairement similaire.

```text
CONDITIONS                  ACTIONS

ConditionManager            ActionManager

ConditionProvider           ActionExecutor

ExpressionParser            AssignmentParser

ExpressionEvaluator         AssignmentExecutor
```

Les conditions observent l’état du jeu.

Les actions modifient l’état du jeu.

Les deux systèmes utilisent le même `TriggerContext`.

---

# Résumé

Le système de conditions répond à la question :

> La règle peut-elle être exécutée maintenant ?

Son architecture est :

```text
TriggerManager
        │
        ▼
ConditionManager
        │
        ▼
ConditionProvider
        │
        ▼
Valeur actuelle
        │
        ▼
ExpressionEvaluator
        │
        ▼
true / false
```

Cette architecture permet d’ajouter de nouvelles sources de données sans modifier le cœur du moteur.
