# Philosophie de RPGEngine

## Vision

RPGEngine est un moteur de gameplay RPG **data-driven** pour Minecraft.

Son objectif n'est pas de fournir un système de quêtes prédéfini, mais un ensemble de composants permettant de construire n'importe quelle mécanique RPG.

Les règles du jeu sont décrites dans la base de données plutôt que codées directement en Java.

---

## Principe fondamental

Chaque comportement du jeu peut être décrit par une règle.

```
Quand...
    Trigger

Si...
    Conditions

Alors...
    Actions
```

Exemple :

```
Quand :
    Le joueur parle au chef du village

Si :
    Le joueur n'a jamais parlé au chef

Alors :
    Afficher un dialogue
    Donner une quête
    Modifier une variable
```

Le moteur ne connaît pas le scénario.

Il ne fait qu'exécuter des règles.

---

## Une architecture orientée données

Le code Java décrit uniquement le fonctionnement du moteur.

Les contenus (quêtes, dialogues, événements, progression...) sont stockés dans la base de données.

Cette approche permet :

- d'ajouter du contenu sans modifier le code ;
- de séparer le moteur du gameplay ;
- de faciliter la maintenance ;
- de rendre le moteur extensible.

---

## Responsabilité unique

Chaque composant possède une responsabilité clairement définie.

- Listener : reçoit les événements Minecraft.
- TriggerManager : recherche les règles à exécuter.
- ConditionManager : vérifie les conditions.
- ActionManager : exécute les actions.
- Repository : lit ou écrit les données.
- Provider : récupère une information.
- Executor : applique une modification.

Aucun composant ne doit remplir plusieurs rôles.

---

## Composition plutôt qu'héritage

Le moteur privilégie la composition.

Une règle est composée de :

- un ou plusieurs triggers ;
- zéro ou plusieurs conditions ;
- une ou plusieurs actions.

Cette composition rend le système flexible et facilement extensible.

---

## Le moteur avant les fonctionnalités

Chaque nouvelle fonctionnalité doit enrichir le moteur plutôt que le contourner.

Avant d'ajouter une nouvelle classe ou un nouveau système, une question doit être posée :

> Cette fonctionnalité peut-elle être exprimée avec les concepts existants (Trigger, Condition, Action, Provider, Executor) ?

Si oui, elle doit s'intégrer naturellement dans le moteur.

Sinon, il faut envisager l'évolution du moteur avant d'ajouter une exception.

---

## Les quêtes

Une quête n'est pas un élément particulier.

Elle est une utilisation du moteur.

Une quête est une succession d'objectifs.

Chaque objectif est validé par des règles.

Cette approche permet de créer :

- des quêtes ;
- des dialogues ;
- des cinématiques ;
- des événements dynamiques ;
- des scripts ;
- des systèmes de réputation.

Sans modifier le fonctionnement du moteur.

---

## Intégrations externes

Les plugins externes (Citizens, FTB Quests, WorldGuard, etc.) ne doivent jamais contenir la logique du jeu.

Ils servent uniquement à fournir des événements ou à afficher des informations.

La logique métier reste toujours dans RPGEngine.

---

## Évolutivité

Le moteur est conçu pour accueillir de nouveaux composants :

- nouveaux types de triggers ;
- nouveaux providers ;
- nouveaux executors ;
- nouveaux systèmes de progression ;
- nouvelles intégrations.

Sans remettre en cause l'architecture existante.

L'objectif est que chaque évolution enrichisse le moteur sans casser les fonctionnalités déjà en place.