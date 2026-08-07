# Logging

## Objectif

RPGEngine centralise ses messages techniques à travers `RpgLogger`.

L’objectif est de séparer clairement :

* les informations destinées aux joueurs ;
* les informations destinées aux administrateurs ;
* les informations destinées au développement.

Un message technique ne doit jamais être affiché directement à un joueur.

---

# Niveaux de logs

RPGEngine utilise quatre niveaux.

## DEBUG

Le niveau `DEBUG` est destiné au développement et au diagnostic détaillé.

Il est affiché uniquement lorsque :

```yaml
debug: true
```

est configuré dans `config.yml`.

Exemples :

```text
Variable joueur modifiée : gold=150
```

```text
Trigger NPC trouvé : 210
```

```text
Condition validée : reputation>=50
```

Le mode debug peut produire un volume important de messages et doit généralement rester désactivé sur un serveur en production.

---

## INFO

Le niveau `INFO` représente le fonctionnement normal du moteur.

Exemples :

```text
RPGEngine démarré.
```

```text
SQLite connecté.
```

```text
Migration V6 appliquée.
```

Ces messages peuvent être visibles en permanence.

---

## WARN

Le niveau `WARN` indique une situation inhabituelle, mais non bloquante.

Le moteur peut continuer à fonctionner.

Exemples :

```text
Configuration optionnelle absente.
```

```text
Intégration Citizens indisponible.
```

```text
Valeur inconnue remplacée par une valeur par défaut.
```

Un warning mérite généralement l’attention de l’administrateur mais ne signifie pas que le moteur est inutilisable.

---

## ERROR

Le niveau `ERROR` indique qu’une opération n’a pas pu être exécutée correctement.

Exemples :

```text
Provider de condition inconnu : QUEST
```

```text
Expression invalide : reputation>>
```

```text
Provider d'action inconnu : DIALOG
```

```text
Erreur d'accès à SQLite.
```

Une erreur doit contenir suffisamment de contexte pour permettre à l’administrateur ou au développeur d’identifier la configuration responsable.

---

# Messages joueur

Les joueurs ne doivent recevoir que des informations liées au gameplay.

Exemples appropriés :

```text
Bienvenue aventurier !
```

```text
Quête acceptée.
```

```text
Vous avez reçu 100 pièces d'or.
```

Les messages techniques suivants ne doivent jamais être affichés au joueur :

```text
Provider inconnu
```

```text
Expression invalide
```

```text
SQLException
```

```text
ActionExecutor manquant
```

Ces informations doivent être envoyées à `RpgLogger`.

---

# Séparation des responsabilités

La règle générale est :

```text
Gameplay
    ↓
Joueur

Technique
    ↓
RpgLogger
```

Ainsi :

* un `ActionExecutor` peut envoyer un message au joueur si ce message constitue l’effet de gameplay attendu ;
* une erreur rencontrée par cet executor doit être envoyée au logger.

---

# RpgLogger

`RpgLogger` fournit les méthodes suivantes :

```java
RpgLogger.debug(...)
RpgLogger.info(...)
RpgLogger.warn(...)
RpgLogger.error(...)
```

Tous les composants de RPGEngine doivent utiliser ces méthodes plutôt que d’accéder directement au logger Bukkit lorsqu’un niveau correspondant existe.

---

# Exemples

## Condition invalide

Mauvais :

```java
context.getPlayer().sendMessage(
        "Provider inconnu : QUEST"
);
```

Correct :

```java
RpgLogger.error(
        "Provider de condition inconnu : QUEST"
);
```

---

## Message de gameplay

Correct :

```java
context.getPlayer()
        .sendMessage(
                "Bienvenue aventurier !"
        );
```

Ce message constitue directement l’effet de l’action `MESSAGE`.

---

## Trace de développement

```java
RpgLogger.debug(
        "Variable joueur modifiée : reputation=50"
);
```

Cette information n’est utile qu’au développement et au diagnostic.

---

# Informations de contexte

Lorsqu’une erreur concerne une règle, le message devrait autant que possible indiquer les données utiles au diagnostic.

Exemple :

```text
Provider d'action inconnu : QUEST | expression=start:village_intro
```

ou :

```text
Expression de condition invalide : reputation>>50 | opérateur inconnu
```

L’objectif est de pouvoir identifier la configuration incorrecte sans devoir reproduire l’erreur avec un debugger.

---

# Principe directeur

Un joueur ne doit jamais voir les détails internes du moteur.

Une erreur de configuration appartient à la console.

Une information de gameplay appartient au joueur.
