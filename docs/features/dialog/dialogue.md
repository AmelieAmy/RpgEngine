# Dialogue Engine — V2

## Introduction

Le système de dialogue de RPGEngine permet de construire et d'exécuter des conversations sous la forme de **graphes narratifs**.

L'idée est volontairement simple :

```text
Dialogue
   │
   ▼
Node
   │
   ▼
Transition
   │
   ├── Conditions
   ├── Actions
   │
   ▼
Node suivant
```

Un **node** contient ce que le PNJ dit.

Une **transition** décrit comment quitter ce node.

Elle peut :

* continuer automatiquement ;
* proposer un choix au joueur ;
* terminer le dialogue.

Une transition peut également posséder des **conditions** et des **actions**.

Cela permet au dialogue de réagir à l'état du jeu sans implémenter lui-même cette logique.

Par exemple :

```text
"Es-tu prêt ?"
       │
       ├── "Oui"
       │      │
       │      ├── condition
       │      ├── action
       │      ▼
       │   node suivant
       │
       └── "Non"
              │
             END
```

Le système de dialogue est donc avant tout un **interpréteur de graphe** utilisant les autres composants de RPGEngine.

---

# Philosophie

Le Dialogue Engine respecte le principe suivant :

> Le moteur ne déduit jamais la structure d'un dialogue.
>
> Il exécute uniquement ce qui est explicitement décrit.
>
> Toute ambiguïté est considérée comme une erreur de conception.

Le moteur ne décide donc jamais qu'un dialogue est terminé simplement parce qu'un node ne possède plus de contenu intéressant.

La fin doit être représentée explicitement par une transition :

```text
END
```

De la même manière, le moteur ne choisit jamais arbitrairement entre plusieurs transitions automatiques disponibles.

Le graphe doit toujours décrire explicitement le comportement attendu.

---

# Architecture générale

Le système peut être séparé en plusieurs responsabilités :

```text
                    DialogueRepository
                           │
                           ▼
                        Dialogue
                           │
                           ▼
                   DialogueValidator
                           │
                           ▼
                    DialogueRunner
                     /           \
                    /             \
       DialogueNavigator     DialogueSessionManager
              │
              ▼
       ConditionManager
              
DialogueRunner
      │
      ▼
 ActionManager
```

Chaque composant possède un rôle distinct.

---

# 1. Le modèle

## Dialogue

`Dialogue` représente l'agrégat complet d'un dialogue.

Il contient :

```text
key
name
startNodeKey
nodes
transitions
```

La clé constitue l'identité métier du dialogue.

Exemple :

```text
chief_chickens
```

Le dialogue fournit également des opérations de navigation structurelle telles que :

```java
findNode(...)
findTransition(...)
getStartNode()
getTransitionsFrom(...)
```

Il connaît donc sa propre structure, mais ne contient aucune logique d'exécution.

---

## DialogueNode

`DialogueNode` représente une unité narrative.

Il contient uniquement :

```text
key
text
```

Exemple :

```text
key  = chief_greeting
text = Bonjour aventurier !
```

Un node ne connaît :

* ni ses transitions ;
* ni ses conditions ;
* ni ses actions ;
* ni le joueur ;
* ni le runtime.

Cette séparation est volontaire.

Le node représente uniquement **ce qui est dit**.

---

## DialogueTransition

`DialogueTransition` représente un passage depuis un node.

Elle contient notamment :

```text
key
sourceNodeKey
targetNodeKey
type
label
position
conditions
actions
```

Exemple :

```text
key       = challenge_accept
source    = chief_challenge
target    = chief_problem
type      = CHOICE
label     = Oui
position  = 1
```

### Clé et position

La `key` constitue l'identité métier de la transition.

Exemple :

```text
challenge_accept
```

La `position` représente uniquement son ordre parmi les transitions issues du même node.

Elle ne constitue pas son identité.

---

# 2. Types de transition

La V2 définit trois types de transition.

## AUTO

Une transition `AUTO` est suivie automatiquement dès qu'elle est disponible.

```text
chief_greeting
      │
     AUTO
      │
      ▼
chief_challenge
```

Elle possède :

```text
source
target
```

mais aucun label joueur.

---

## CHOICE

Une transition `CHOICE` représente un choix proposé au joueur.

```text
"Es-tu prêt ?"

1 - Oui
2 - Peut être plus tard...
```

Chaque choix est une transition indépendante.

Exemple :

```text
challenge_accept
type   = CHOICE
target = chief_problem
label  = Oui
```

---

## END

Une transition `END` termine explicitement le dialogue.

```text
chief_return
     │
    END
```

Elle ne possède :

* ni cible ;
* ni label.

La fin d'un dialogue est donc toujours explicite.

---

# 3. Conditions

Une transition peut posséder zéro ou plusieurs conditions.

Une transition n'est disponible que si toutes ses conditions sont satisfaites.

```text
DialogueTransition
        │
        ▼
DialogueNavigator
        │
        ▼
ConditionManager
        │
        ▼
ConditionProvider
```

Le Dialogue Engine ne connaît pas la signification des conditions.

Il délègue leur évaluation au `ConditionManager`.

---

## Exemple PLAYER

Une condition peut vérifier une variable joueur :

```text
PLAYER | chickens_quest==started
```

Le provider `PLAYER` récupère la variable du joueur puis utilise le moteur d'expressions pour effectuer la comparaison.

---

## Syntaxe des conditions

Les conditions utilisent le `ExpressionParser` du moteur.

Par exemple :

```text
chickens_quest==started
```

La syntaxe est validée dès l'administration du dialogue.

Une expression invalide ne doit donc pas être persistée.

Exemple invalide :

```text
chickens_quest=started
```

Exemple valide :

```text
chickens_quest==started
```

Il faut distinguer :

```text
Condition
chickens_quest==started
```

de :

```text
Action
chickens_quest=started
```

La première compare une valeur.

La seconde affecte une valeur.

---

# 4. Actions

Une transition peut posséder zéro ou plusieurs actions.

Les actions sont exécutées lorsque la transition est suivie.

```text
Transition sélectionnée
        │
        ▼
   ActionManager
        │
        ▼
   ActionExecutor
        │
        ▼
   Node suivant
```

Les actions sont ordonnées par leur position.

Le Dialogue Engine ne connaît pas leur signification.

Il délègue leur exécution à `ActionManager`.

---

## Exemple

La transition :

```text
quest_accept
```

peut posséder :

```text
PLAYER | chickens_quest=started
```

Lorsque le joueur accepte la quête :

```text
CHOICE "J'accepte"
        │
        ▼
quest_accept
        │
        ▼
ActionManager
        │
        ▼
PLAYER
chickens_quest=started
        │
        ▼
chief_thanks
```

Le dialogue peut ainsi modifier l'état du jeu sans dépendre directement du système de variables joueur.

---

# 5. DialogueValidator

`DialogueValidator` vérifie qu'un graphe est cohérent avant son exécution.

Le principe est important :

> Le validateur détecte les erreurs. Il ne les corrige jamais automatiquement.

Il vérifie notamment :

* la présence du node de départ ;
* l'existence du node de départ ;
* l'unicité des clés de nodes ;
* l'unicité des clés de transitions ;
* l'existence des sources ;
* l'existence des cibles ;
* les contraintes propres aux transitions `AUTO` ;
* les contraintes propres aux transitions `CHOICE` ;
* les contraintes propres aux transitions `END` ;
* la validité des positions ;
* l'absence de positions dupliquées pour un même node ;
* la présence d'au moins une fin explicite ;
* l'accessibilité des nodes depuis le node de départ ;
* l'absence de boucle infinie composée uniquement de transitions `AUTO`.

Un dialogue invalide n'est pas démarré.

---

# 6. DialogueNavigator

`DialogueNavigator` détermine les transitions actuellement disponibles.

Son rôle peut être résumé ainsi :

```text
Node courant
     │
     ▼
Transitions sortantes
     │
     ▼
Conditions
     │
     ▼
Transitions disponibles
```

Il utilise `ConditionManager` pour filtrer les transitions.

Une transition dont une condition échoue n'est donc pas disponible.

Le navigateur ne choisit pas à la place du joueur et n'exécute aucune action.

---

# 7. DialogueSession

Un dialogue peut s'étendre sur plusieurs interactions.

Le moteur doit donc conserver son état entre deux commandes ou événements.

`DialogueSession` représente cet état temporaire.

Elle conserve notamment :

```text
playerUuid
dialogue
currentNodeKey
TriggerContext
```

Le dialogue complet fait partie de la session.

Ainsi, lorsqu'un joueur effectue un choix, le moteur peut reprendre le graphe sans devoir recharger le dialogue depuis SQLite.

---

# 8. DialogueSessionManager

`DialogueSessionManager` gère les sessions actives.

Il permet essentiellement de :

```text
créer une session
retrouver une session
supprimer une session
```

Les sessions sont indexées par UUID joueur.

Le manager ne contient aucune logique narrative.

---

# 9. DialogueRunner

`DialogueRunner` constitue le runtime principal.

Il orchestre les différents composants sans reprendre leur responsabilité.

Le démarrage suit globalement :

```text
DialogueRunner.start()
        │
        ▼
DialogueValidator
        │
        ▼
création DialogueSession
        │
        ▼
node de départ
        │
        ▼
DialogueNavigator
```

À chaque node, le Runner examine les transitions disponibles.

---

## Transition AUTO

Si une transition `AUTO` est disponible :

```text
node
 │
 ▼
AUTO
 │
 ├── actions
 │
 ▼
node suivant
```

le moteur poursuit automatiquement.

---

## Transition CHOICE

Si des transitions `CHOICE` sont disponibles :

```text
node
 │
 ▼
CHOICE
 │
 ▼
attente joueur
```

la session reste active.

Le joueur sélectionne ensuite une transition.

---

## Transition END

Une transition `END` :

```text
node
 │
 ▼
END
 │
 ▼
fin session
```

termine explicitement la conversation.

---

# 10. Sélection d'un choix

La V2 utilise actuellement la commande :

```text
/rpg dialog choose <position>
```

Exemple :

```text
/rpg dialog choose 1
```

Le `DialogueRunner` récupère la session active du joueur puis réévalue les transitions disponibles.

Cette réévaluation est importante.

Un joueur ne peut pas contourner une condition simplement en saisissant manuellement la position d'une transition masquée.

```text
choose 1
   │
   ▼
session
   │
   ▼
DialogueNavigator
   │
   ▼
ConditionManager
   │
   ▼
transition réellement disponible ?
```

La commande `choose` constitue l'interface runtime minimale de la V2.

Elle pourra plus tard être remplacée par une interface graphique, des composants de chat ou un autre système d'interaction sans modifier le cœur du moteur.

---

# 11. DialogueRepository

`DialogueRepository` constitue la frontière entre SQLite et le modèle métier.

Il sait notamment :

```text
create
delete
findByKey
findAll
save
```

Le repository charge un dialogue sous la forme d'un agrégat complet :

```text
Dialogue
├── DialogueNode
└── DialogueTransition
      ├── Condition
      └── Action
```

Le runtime n'effectue donc pas lui-même de requêtes SQL.

---

## Sauvegarde

Lors d'une modification du graphe, `DialogueService` construit une nouvelle version de l'agrégat puis demande :

```java
repository.save(updatedDialogue);
```

La sauvegarde du graphe est transactionnelle.

Le repository restaure également l'état `autoCommit` de la connexion après l'opération.

---

# 12. DialogueService

`DialogueService` constitue le point d'entrée métier pour l'administration.

Les commandes ne manipulent jamais directement SQLite.

```text
Command
   │
   ▼
DialogueService
   │
   ▼
Dialogue
   │
   ▼
DialogueRepository
```

Le service permet notamment de :

```text
créer un dialogue
supprimer un dialogue
lister les dialogues
charger un dialogue

ajouter un node
modifier le texte d'un node
définir le node de départ

ajouter une transition
configurer une transition

ajouter une action
lister les actions
supprimer une action

ajouter une condition
lister les conditions
supprimer une condition
```

Les modifications produisent de nouveaux agrégats plutôt que de modifier directement les objets existants.

---

# 13. Commandes V2

## Dialogues

Créer :

```text
/rpg dialog create <key> <name>
```

Supprimer :

```text
/rpg dialog delete <dialogueKey>
```

Lister :

```text
/rpg dialog list
```

Inspecter :

```text
/rpg dialog info <dialogueKey>
```

Prévisualiser :

```text
/rpg dialog preview <dialogueKey>
```

---

## Nodes

Ajouter :

```text
/rpg dialog node add <dialogueKey> <nodeKey> <text>
```

Modifier le texte :

```text
/rpg dialog node text <dialogueKey> <nodeKey> <text>
```

Définir le node de départ :

```text
/rpg dialog start <dialogueKey> <nodeKey>
```

---

## Transitions

Créer une transition :

```text
/rpg dialog transition add <dialogueKey> <sourceNodeKey> <transitionKey>
```

Une nouvelle transition est initialement créée sous la forme `END`.

Elle peut ensuite être configurée atomiquement.

### END

```text
/rpg dialog transition set <dialogueKey> <transitionKey> END
```

### AUTO

```text
/rpg dialog transition set <dialogueKey> <transitionKey> AUTO <targetNodeKey>
```

### CHOICE

```text
/rpg dialog transition set <dialogueKey> <transitionKey> CHOICE <targetNodeKey> <label>
```

---

# 14. Administration des actions

Ajouter :

```text
/rpg dialog transition action add <dialogueKey> <transitionKey> <provider> <expression>
```

Exemple :

```text
/rpg dialog transition action add chief_chickens quest_accept PLAYER chickens_quest=started
```

Lister :

```text
/rpg dialog transition action list <dialogueKey> <transitionKey>
```

Supprimer :

```text
/rpg dialog transition action remove <dialogueKey> <transitionKey> <position>
```

---

# 15. Administration des conditions

Ajouter :

```text
/rpg dialog transition condition add <dialogueKey> <transitionKey> <provider> <expression>
```

Exemple :

```text
/rpg dialog transition condition add chief_chickens challenge_accept PLAYER chickens_quest==started
```

Lister :

```text
/rpg dialog transition condition list <dialogueKey> <transitionKey>
```

Supprimer :

```text
/rpg dialog transition condition remove <dialogueKey> <transitionKey> <position>
```

---

# 16. Prévisualisation

La commande :

```text
/rpg dialog preview <dialogueKey>
```

permet d'inspecter le point d'entrée narratif d'un dialogue sans l'exécuter réellement.

Une preview :

* ne crée pas de session ;
* n'évalue pas les conditions ;
* n'exécute pas les actions ;
* ne parcourt pas le graphe.

Elle permet simplement de vérifier le node de départ.

---

# 17. Intégration avec le Rule Engine

L'un des principes fondamentaux de la V2 est que le Dialogue Engine ne possède pas son propre moteur de règles.

Il utilise celui de RPGEngine.

```text
                 DialogueTransition
                    /          \
                   /            \
            Conditions         Actions
                │                 │
                ▼                 ▼
       ConditionManager      ActionManager
                │                 │
                ▼                 ▼
      ConditionProvider     ActionExecutor
```

Cela évite de créer deux systèmes différents pour représenter la logique du jeu.

---

# 18. Expression Engine partagé

La V2 utilise une seule instance conceptuelle de la grammaire d'expressions.

`ExpressionParser` est injecté à la fois dans :

```text
DialogueService
```

et :

```text
ConditionManager
```

Cela signifie que la syntaxe acceptée lors de la création d'une condition est la même que celle utilisée lors de son exécution.

```text
              ExpressionParser
               /            \
              /              \
     DialogueService     ConditionManager
     administration        runtime
```

`ExpressionEvaluator` est utilisé par `ConditionManager` pour effectuer la comparaison.

Cette architecture évite de dupliquer les règles de parsing dans le système de dialogue.

---

# 19. Intégration avec les triggers

Un dialogue peut être déclenché comme n'importe quelle autre action du moteur.

Le provider/executor :

```text
DIALOG
```

permet à `ActionManager` de démarrer un dialogue.

Le chemin complet peut donc être :

```text
Interaction joueur
       │
       ▼
NPCListener
       │
       ▼
TriggerManager
       │
       ├── ConditionManager
       │
       ▼
ActionManager
       │
       ▼
DIALOG
       │
       ▼
DialogActionExecutor
       │
       ▼
DialogueRunner
```

Le dialogue n'a donc pas besoin de connaître Citizens, les listeners ou le système de triggers.

---

# 20. Composition dans RpgEngine

`RpgEngine` constitue le point de composition de ces composants.

La V2 assemble notamment :

```text
Repositories
    │
    ├── PlayerVariableRepository
    └── DialogueRepository

Expression Engine
    │
    ├── ExpressionParser
    └── ExpressionEvaluator

Services
    │
    └── DialogueService

Rule Engine
    │
    ├── ConditionManager
    └── ActionManager

Dialogue Runtime
    │
    ├── DialogueValidator
    ├── DialogueNavigator
    ├── DialogueSessionManager
    └── DialogueRunner

Executors
    │
    ├── MESSAGE
    ├── PLAYER
    └── DIALOG

Administration
    │
    └── CommandManager

External adapters
    │
    └── NPCListener
```

`RpgEngine` assemble ces composants mais ne contient aucune logique narrative.

---

# 21. Flux complet

Le fonctionnement complet de la V2 peut être représenté ainsi :

```text
                    Joueur
                      │
                      ▼
                 NPCListener
                      │
                      ▼
                TriggerManager
                      │
             ┌────────┴────────┐
             │                 │
             ▼                 ▼
     ConditionManager     ActionManager
                               │
                               ▼
                             DIALOG
                               │
                               ▼
                     DialogActionExecutor
                               │
                               ▼
                      DialogueRepository
                               │
                               ▼
                         DialogueRunner
                               │
                               ▼
                       DialogueValidator
                               │
                               ▼
                    DialogueSessionManager
                               │
                               ▼
                      DialogueNavigator
                               │
                               ▼
                       ConditionManager
                               │
                               ▼
                       Transition choisie
                               │
                               ▼
                         ActionManager
                               │
                               ▼
                    Node suivant / END
```

Le dialogue utilise donc le moteur existant au lieu de recréer ses propres systèmes de conditions et d'actions.

---

# 22. Exemple : chief_chickens

Le dialogue de référence `chief_chickens` sert de test d'intégration de la V2.

Il vérifie notamment :

```text
AUTO
CHOICE
END
conditions
actions
sessions
variables joueur
déclenchement NPC
persistance SQLite
```

Exemple d'action :

```text
quest_accept
    │
    └── PLAYER | chickens_quest=started
```

Exemple de condition :

```text
challenge_accept
    │
    └── PLAYER | chickens_quest==started
```

Cela permet de vérifier le cycle :

```text
état du jeu
     │
     ▼
condition
     │
     ▼
dialogue
     │
     ▼
choix joueur
     │
     ▼
action
     │
     ▼
nouvel état du jeu
```

Le scénario complet est documenté séparément dans :

```text
docs/tests/dialogue_reference.md
```

---

# 23. Responsabilités

La séparation finale de la V2 peut être résumée ainsi :

| Composant                | Responsabilité                         |
| ------------------------ | -------------------------------------- |
| `Dialogue`               | représenter le graphe                  |
| `DialogueNode`           | représenter un texte narratif          |
| `DialogueTransition`     | représenter une progression            |
| `DialogueRepository`     | persister l'agrégat                    |
| `DialogueService`        | administrer les dialogues              |
| `DialogueValidator`      | valider la structure                   |
| `DialogueNavigator`      | déterminer les transitions disponibles |
| `DialogueSession`        | représenter l'état runtime d'un joueur |
| `DialogueSessionManager` | gérer les sessions                     |
| `DialogueRunner`         | orchestrer l'exécution                 |
| `ConditionManager`       | évaluer les conditions                 |
| `ActionManager`          | exécuter les actions                   |
| `ExpressionParser`       | analyser les expressions               |
| `ExpressionEvaluator`    | effectuer les comparaisons             |
| `DialogActionExecutor`   | démarrer un dialogue depuis une action |

Cette séparation constitue la base architecturale à préserver lors des évolutions futures.

---

# 24. Ce que la V2 garantit

La V2 fournit désormais un moteur capable de :

* créer et supprimer des dialogues ;
* créer des nodes ;
* modifier leur texte ;
* définir explicitement le point de départ ;
* créer et configurer des transitions ;
* gérer `AUTO`, `CHOICE` et `END` ;
* associer des conditions aux transitions ;
* associer des actions aux transitions ;
* valider les expressions de condition avant leur persistance ;
* filtrer dynamiquement les transitions selon l'état du jeu ;
* exécuter des actions lors de la progression ;
* conserver une conversation dans une session joueur ;
* reprendre un dialogue après un choix ;
* empêcher le contournement des conditions lors d'un choix manuel ;
* valider la cohérence structurelle d'un graphe ;
* persister le graphe complet dans SQLite ;
* déclencher un dialogue depuis le Trigger Engine ;
* utiliser les mêmes `ConditionManager` et `ActionManager` que le reste de RPGEngine ;
* administrer et inspecter les dialogues depuis `/rpg`.

---

# Conclusion

La V2 transforme le système de dialogue en un véritable composant du moteur RPG.

Le point essentiel n'est pas la manière dont le texte est affiché au joueur.

Le point essentiel est la séparation des responsabilités :

```text
DialogueRunner
      │
      ├── DialogueValidator
      ├── DialogueNavigator
      │        │
      │        └── ConditionManager
      │
      ├── DialogueSessionManager
      │
      └── ActionManager
```

Le dialogue ne possède pas son propre système de règles.

Il utilise le moteur.

Cette architecture permet aux évolutions futures — interface graphique, nouveaux types d'actions, nouveaux providers de conditions, quêtes ou autres systèmes narratifs — de s'appuyer sur le même socle sans réécrire le fonctionnement fondamental du Dialogue Engine.
