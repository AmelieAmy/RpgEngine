# Dialogue de référence — V2

Ce document décrit le scénario de référence utilisé pour tester le Dialogue Engine de RPGEngine.

Son objectif est de fournir un test d'intégration reproductible après toute évolution importante du moteur.

Le scénario couvre notamment :

* la création d'un dialogue ;
* la création et la modification de nodes ;
* la définition du node de départ ;
* les transitions `AUTO` ;
* les transitions `CHOICE` ;
* les transitions `END` ;
* les conditions de transition ;
* les actions de transition ;
* la validation des expressions ;
* la persistance SQLite ;
* la validation structurelle du graphe ;
* la création et la gestion d'une `DialogueSession` ;
* la sélection d'un choix joueur ;
* la réévaluation des conditions lors d'un choix ;
* la modification de l'état joueur ;
* le déclenchement du dialogue depuis un PNJ ;
* l'intégration avec le Trigger Engine et le Rule Engine.

---

# 1. Scénario de référence

Le PNJ utilisé pour ce test est le **Chief**, identifié par Citizens avec l'ID :

```text
210
```

Le dialogue porte la clé :

```text
chief_chickens
```

Le scénario narratif est le suivant :

```text
Chief:
"Bonjour aventurier !"

        │
        ▼

Chief:
"Es tu prêt a relever un nouveau défi ?"

        │
        ├────────────────────────────────────┐
        │                                    │
        ▼                                    ▼
Joueur: "Oui"                    Joueur: "Peut être plus tard..."
        │                                    │
        ▼                                    ▼
Chief:                              Chief:
"Mes poules se sont                "Très bien. Reviens me voir
échappées !"                        si tu changes d'avis."
        │                                    │
        ▼                                   END
Chief:
"Il faut les trouver et
les remettre dans le poulailler."

        │
        ├────────────────────────────────────┐
        │                                    │
        ▼                                    ▼
Joueur: "J'accepte"          Joueur: "Faites le vous-même !"
        │                                    │
        ▼                                    ▼
Action:                             Chief:
chickens_quest=started              "Je vois que je devrai
        │                           me débrouiller seul..."
        ▼                                    │
Chief:                                       END
"Merci aventurier !"
        │
        ▼
Chief:
"Reviens me voir quand elles
seront toutes rentrées."
        │
       END
```

---

# 2. Graphe technique

```text
chief_greeting
        │
        │ AUTO
        ▼
chief_challenge
        │
        ├── CHOICE "Oui"
        │       │
        │       ▼
        │   chief_problem
        │       │
        │       │ AUTO
        │       ▼
        │   chief_explanation
        │       │
        │       ├── CHOICE "J'accepte"
        │       │       │
        │       │       ├── ACTION
        │       │       │   PLAYER
        │       │       │   chickens_quest=started
        │       │       │
        │       │       ▼
        │       │   chief_thanks
        │       │       │
        │       │       │ AUTO
        │       │       ▼
        │       │   chief_return
        │       │       │
        │       │      END
        │       │
        │       └── CHOICE "Faites le vous-même !"
        │               │
        │               ▼
        │       chief_decline_quest
        │               │
        │              END
        │
        └── CHOICE "Peut être plus tard..."
                │
                ▼
        chief_decline_challenge
                │
               END
```

Une condition peut également être ajoutée temporairement à `challenge_accept` afin de tester le filtrage dynamique :

```text
challenge_accept
└── PLAYER | chickens_quest==started
```

---

# 3. Nodes

Le dialogue utilise huit nodes.

| Clé                       | Texte                                                   |
| ------------------------- | ------------------------------------------------------- |
| `chief_greeting`          | Bonjour aventurier !                                    |
| `chief_challenge`         | Es tu prêt a relever un nouveau défi ?                  |
| `chief_problem`           | Mes poules se sont échappées !                          |
| `chief_explanation`       | Il faut les trouver et les remettre dans le poulailler. |
| `chief_thanks`            | Merci aventurier !                                      |
| `chief_return`            | Reviens me voir quand elles seront toutes rentrées.     |
| `chief_decline_challenge` | Très bien. Reviens me voir si tu changes d'avis.        |
| `chief_decline_quest`     | Je vois que je devrai me débrouiller seul...            |

Le node de départ est :

```text
chief_greeting
```

---

# 4. Transitions

Le dialogue utilise dix transitions.

| Clé                      | Source                    | Type     | Cible                     | Label                  |
| ------------------------ | ------------------------- | -------- | ------------------------- | ---------------------- |
| `greeting_to_challenge`  | `chief_greeting`          | `AUTO`   | `chief_challenge`         | —                      |
| `challenge_accept`       | `chief_challenge`         | `CHOICE` | `chief_problem`           | Oui                    |
| `challenge_decline`      | `chief_challenge`         | `CHOICE` | `chief_decline_challenge` | Peut être plus tard... |
| `problem_to_explanation` | `chief_problem`           | `AUTO`   | `chief_explanation`       | —                      |
| `quest_accept`           | `chief_explanation`       | `CHOICE` | `chief_thanks`            | J'accepte              |
| `quest_decline`          | `chief_explanation`       | `CHOICE` | `chief_decline_quest`     | Faites le vous-même !  |
| `thanks_to_return`       | `chief_thanks`            | `AUTO`   | `chief_return`            | —                      |
| `return_end`             | `chief_return`            | `END`    | —                         | —                      |
| `decline_challenge_end`  | `chief_decline_challenge` | `END`    | —                         | —                      |
| `decline_quest_end`      | `chief_decline_quest`     | `END`    | —                         | —                      |

---

# 5. Création du dialogue

## Dialogue

```text
/rpg dialog create chief_chickens Les poules du Chef
```

---

## Nodes

```text
/rpg dialog node add chief_chickens chief_greeting Bonjour aventurier !

/rpg dialog node add chief_chickens chief_challenge Es tu prêt a relever un nouveau défi ?

/rpg dialog node add chief_chickens chief_problem Mes poules se sont échappées !

/rpg dialog node add chief_chickens chief_explanation Il faut les trouver et les remettre dans le poulailler.

/rpg dialog node add chief_chickens chief_thanks Merci aventurier !

/rpg dialog node add chief_chickens chief_return Reviens me voir quand elles seront toutes rentrées.

/rpg dialog node add chief_chickens chief_decline_challenge Très bien. Reviens me voir si tu changes d'avis.

/rpg dialog node add chief_chickens chief_decline_quest Je vois que je devrai me débrouiller seul...
```

---

## Node de départ

```text
/rpg dialog start chief_chickens chief_greeting
```

---

# 6. Création des transitions

## Introduction

```text
/rpg dialog transition add chief_chickens chief_greeting greeting_to_challenge

/rpg dialog transition set chief_chickens greeting_to_challenge AUTO chief_challenge
```

---

## Premier choix

Acceptation :

```text
/rpg dialog transition add chief_chickens chief_challenge challenge_accept

/rpg dialog transition set chief_chickens challenge_accept CHOICE chief_problem Oui
```

Refus :

```text
/rpg dialog transition add chief_chickens chief_challenge challenge_decline

/rpg dialog transition set chief_chickens challenge_decline CHOICE chief_decline_challenge Peut être plus tard...
```

---

## Présentation du problème

```text
/rpg dialog transition add chief_chickens chief_problem problem_to_explanation

/rpg dialog transition set chief_chickens problem_to_explanation AUTO chief_explanation
```

---

## Second choix

Acceptation :

```text
/rpg dialog transition add chief_chickens chief_explanation quest_accept

/rpg dialog transition set chief_chickens quest_accept CHOICE chief_thanks J'accepte
```

Refus :

```text
/rpg dialog transition add chief_chickens chief_explanation quest_decline

/rpg dialog transition set chief_chickens quest_decline CHOICE chief_decline_quest Faites le vous-même !
```

---

## Fin positive

```text
/rpg dialog transition add chief_chickens chief_thanks thanks_to_return

/rpg dialog transition set chief_chickens thanks_to_return AUTO chief_return
```

Puis :

```text
/rpg dialog transition add chief_chickens chief_return return_end
```

`return_end` reste de type `END`.

---

## Fin après refus du défi

```text
/rpg dialog transition add chief_chickens chief_decline_challenge decline_challenge_end
```

---

## Fin après refus de la mission

```text
/rpg dialog transition add chief_chickens chief_decline_quest decline_quest_end
```

---

# 7. Vérification du graphe

```text
/rpg dialog info chief_chickens
```

Le résultat attendu doit notamment indiquer :

```text
Nodes : 8
Transitions : 10
```

Le node de départ doit être :

```text
chief_greeting
```

Les clés des transitions doivent également être visibles dans l'affichage.

---

# 8. Modification d'un node

La V2 permet de modifier le texte d'un node existant.

Exemple :

```text
/rpg dialog node text chief_chickens chief_greeting Bonjour à nouveau aventurier !
```

Vérifier :

```text
/rpg dialog info chief_chickens
```

Puis restaurer éventuellement le texte de référence :

```text
/rpg dialog node text chief_chickens chief_greeting Bonjour aventurier !
```

---

# 9. Prévisualisation

```text
/rpg dialog preview chief_chickens
```

Résultat attendu :

```text
Bonjour aventurier !
```

La prévisualisation :

* ne crée aucune `DialogueSession` ;
* n'exécute aucune action ;
* n'évalue aucune navigation ;
* ne parcourt pas le graphe.

---

# 10. Déclenchement depuis le PNJ

Le PNJ de référence possède l'identifiant Citizens :

```text
210
```

Le trigger de test peut être configuré directement en base de données.

## Trigger

```sql
INSERT INTO trigger (
    name,
    type,
    target_id,
    enabled
)
VALUES (
    'chief_chickens_dialog',
    'NPC',
    '210',
    1
);
```

Récupérer son identifiant :

```sql
SELECT
    id,
    name,
    type,
    target_id,
    enabled
FROM trigger
WHERE name = 'chief_chickens_dialog';
```

---

## Action DIALOG

En remplaçant `<trigger_id>` :

```sql
INSERT INTO action (
    trigger_id,
    provider,
    expression,
    position
)
VALUES (
    <trigger_id>,
    'DIALOG',
    'chief_chickens',
    1
);
```

Le flux attendu est :

```text
Clic PNJ 210
     │
     ▼
NPCListener
     │
     ▼
TriggerManager
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

---

# 11. Test du chemin positif

Cliquer sur le PNJ `210`.

Résultat attendu :

```text
Bonjour aventurier !

Es tu prêt a relever un nouveau défi ?

Choix disponibles :
1 - Oui
2 - Peut être plus tard...
```

Choisir :

```text
/rpg dialog choose 1
```

Résultat attendu :

```text
Mes poules se sont échappées !

Il faut les trouver et les remettre dans le poulailler.

Choix disponibles :
1 - J'accepte
2 - Faites le vous-même !
```

Choisir :

```text
/rpg dialog choose 1
```

Résultat attendu :

```text
Merci aventurier !

Reviens me voir quand elles seront toutes rentrées.
```

La session doit ensuite être supprimée par `return_end`.

---

# 12. Test du premier refus

Relancer le dialogue.

Au premier choix :

```text
/rpg dialog choose 2
```

Résultat attendu :

```text
Très bien. Reviens me voir si tu changes d'avis.
```

Puis :

```text
decline_challenge_end
```

doit terminer la session.

---

# 13. Test du second refus

Relancer le dialogue.

Premier choix :

```text
/rpg dialog choose 1
```

Second choix :

```text
/rpg dialog choose 2
```

Résultat attendu :

```text
Je vois que je devrai me débrouiller seul...
```

Puis :

```text
decline_quest_end
```

doit terminer la session.

---

# 14. Test des actions de transition

La transition :

```text
quest_accept
```

est utilisée pour tester l'exécution d'une action.

Ajouter :

```text
/rpg dialog transition action add chief_chickens quest_accept PLAYER chickens_quest=started
```

Vérifier :

```text
/rpg dialog transition action list chief_chickens quest_accept
```

Résultat attendu :

```text
Actions de la transition 'quest_accept' :
1 - PLAYER | chickens_quest=started
```

---

## Exécution runtime

Relancer le dialogue puis choisir :

```text
/rpg dialog choose 1
```

puis :

```text
/rpg dialog choose 1
```

La transition `quest_accept` doit exécuter :

```text
PLAYER | chickens_quest=started
```

avant de poursuivre vers :

```text
chief_thanks
```

Vérifier la base :

```sql
SELECT
    player_uuid,
    key,
    value
FROM player_variable
WHERE player_uuid = '<UUID_DU_JOUEUR>'
  AND key = 'chickens_quest';
```

Résultat attendu :

```text
<UUID> | chickens_quest | started
```

Ce test valide :

```text
DialogueTransition
        │
        ▼
ActionManager
        │
        ▼
PlayerVariableActionExecutor
        │
        ▼
player_variable
```

---

# 15. Test de suppression d'une action

Lister :

```text
/rpg dialog transition action list chief_chickens quest_accept
```

Supprimer :

```text
/rpg dialog transition action remove chief_chickens quest_accept 1
```

Vérifier :

```text
/rpg dialog transition action list chief_chickens quest_accept
```

Résultat attendu :

```text
Aucune action sur la transition 'quest_accept'.
```

Remettre ensuite l'action de référence :

```text
/rpg dialog transition action add chief_chickens quest_accept PLAYER chickens_quest=started
```

---

# 16. Test des conditions de transition

Pour tester le filtrage dynamique, ajouter temporairement une condition à :

```text
challenge_accept
```

Commande :

```text
/rpg dialog transition condition add chief_chickens challenge_accept PLAYER chickens_quest==started
```

Vérifier :

```text
/rpg dialog transition condition list chief_chickens challenge_accept
```

Résultat attendu :

```text
Conditions de la transition 'challenge_accept' :
1 - PLAYER | chickens_quest==started
```

---

# 17. Validation syntaxique des conditions

La commande d'administration doit utiliser le même `ExpressionParser` que le runtime.

Une condition invalide :

```text
/rpg dialog transition condition add chief_chickens challenge_accept PLAYER chickens_quest=started
```

doit être refusée.

La syntaxe correcte est :

```text
/rpg dialog transition condition add chief_chickens challenge_accept PLAYER chickens_quest==started
```

La différence est volontaire :

```text
Action
chickens_quest=started
```

```text
Condition
chickens_quest==started
```

---

# 18. Condition fausse

Supprimer la variable joueur :

```sql
DELETE FROM player_variable
WHERE player_uuid = '<UUID_DU_JOUEUR>'
  AND key = 'chickens_quest';
```

Cliquer sur le PNJ `210`.

Résultat attendu :

```text
Bonjour aventurier !

Es tu prêt a relever un nouveau défi ?

Choix disponibles :
2 - Peut être plus tard...
```

Le choix :

```text
1 - Oui
```

ne doit pas apparaître.

---

## Tentative de contournement

Exécuter malgré tout :

```text
/rpg dialog choose 1
```

Résultat attendu :

```text
Aucun choix correspondant n'est disponible.
```

Le runtime doit réévaluer les conditions lors de la sélection.

La condition ne doit donc pas pouvoir être contournée en saisissant manuellement une position masquée.

---

## Choix disponible

```text
/rpg dialog choose 2
```

doit fonctionner normalement et conduire vers :

```text
chief_decline_challenge
```

puis `END`.

---

# 19. Condition vraie

Définir la variable :

```sql
DELETE FROM player_variable
WHERE player_uuid = '<UUID_DU_JOUEUR>'
  AND key = 'chickens_quest';

INSERT INTO player_variable (
    player_uuid,
    key,
    value
)
VALUES (
    '<UUID_DU_JOUEUR>',
    'chickens_quest',
    'started'
);
```

Vérifier :

```sql
SELECT
    player_uuid,
    key,
    value
FROM player_variable
WHERE player_uuid = '<UUID_DU_JOUEUR>'
  AND key = 'chickens_quest';
```

Résultat attendu :

```text
<UUID> | chickens_quest | started
```

Cliquer sur le PNJ `210`.

Résultat attendu :

```text
Bonjour aventurier !

Es tu prêt a relever un nouveau défi ?

Choix disponibles :
1 - Oui
2 - Peut être plus tard...
```

Cette fois :

```text
/rpg dialog choose 1
```

doit être accepté.

Ce test valide :

```text
player_variable
       │
       ▼
PlayerConditionProvider
       │
       ▼
ConditionManager
       │
       ▼
DialogueNavigator
       │
       ▼
DialogueTransition disponible
       │
       ▼
DialogueRunner
```

---

# 20. Suppression de la condition de test

Après validation, la condition peut être supprimée :

```text
/rpg dialog transition condition remove chief_chickens challenge_accept 1
```

Vérifier :

```text
/rpg dialog transition condition list chief_chickens challenge_accept
```

Résultat attendu :

```text
Aucune condition sur la transition 'challenge_accept'.
```

La condition peut être recréée ultérieurement lorsque le scénario définitif utilisera réellement cet état joueur.

---

# 21. Test de suppression d'un dialogue

Ne pas utiliser `chief_chickens` pour ce test.

Créer un dialogue temporaire :

```text
/rpg dialog create delete_test Dialogue temporaire
```

Vérifier :

```text
/rpg dialog list
```

Puis :

```text
/rpg dialog delete delete_test
```

Vérifier à nouveau :

```text
/rpg dialog list
```

`delete_test` ne doit plus apparaître.

---

# 22. Comportements attendus

Le test de non-régression V2 est considéré comme réussi si :

* le dialogue peut être créé depuis les commandes ;
* ses nodes peuvent être créés ;
* le texte d'un node peut être modifié ;
* son node de départ peut être défini ;
* les transitions peuvent être créées et configurées ;
* les transitions `AUTO` progressent automatiquement ;
* les transitions `CHOICE` sont proposées correctement ;
* les transitions `END` terminent explicitement la session ;
* les actions peuvent être ajoutées, listées et supprimées ;
* les conditions peuvent être ajoutées, listées et supprimées ;
* une expression de condition invalide est refusée ;
* une condition vraie rend la transition disponible ;
* une condition fausse masque la transition ;
* un choix masqué ne peut pas être forcé manuellement ;
* les actions sont exécutées avant le passage au node suivant ;
* une action `PLAYER` modifie bien la variable du joueur ;
* une `DialogueSession` est conservée entre les choix ;
* la session est supprimée après `END` ;
* le PNJ `210` déclenche le dialogue via le Trigger Engine ;
* aucune erreur inattendue n'apparaît dans la console.

---

# 23. Chaîne d'intégration validée

Le scénario `chief_chickens` valide désormais la chaîne suivante :

```text
NPCListener
      │
      ▼
TriggerManager
      │
      ├───────────────┐
      ▼               ▼
ConditionManager   ActionManager
                      │
                      ▼
                 DIALOG Action
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
             DialogueTransition
                      │
                      ▼
                ActionManager
                      │
                      ▼
             Node suivant / END
```

---

# 24. Objectif de non-régression

`chief_chickens` constitue le scénario de référence du Dialogue Engine V2.

Il doit être rejoué après toute modification importante concernant :

* `Dialogue` ;
* `DialogueNode` ;
* `DialogueTransition` ;
* `DialogueRepository` ;
* `DialogueService` ;
* `DialogueValidator` ;
* `DialogueNavigator` ;
* `DialogueRunner` ;
* `DialogueSession` ;
* `DialogueSessionManager` ;
* `ConditionManager` ;
* `ActionManager` ;
* `ExpressionParser` ;
* les providers de conditions ;
* les executors d'actions ;
* les commandes de dialogue ;
* les triggers déclenchant un dialogue.

Le scénario doit rester suffisamment petit pour être rejoué rapidement, tout en couvrant les mécanismes fondamentaux du moteur.

---

# Conclusion

Le test `chief_chickens` ne vérifie plus uniquement qu'un texte peut être affiché.

Il vérifie désormais le cycle complet :

```text
état du jeu
     │
     ▼
conditions
     │
     ▼
dialogue
     │
     ▼
choix joueur
     │
     ▼
actions
     │
     ▼
nouvel état du jeu
```

Ce cycle constitue le test de référence de la V2 du Dialogue Engine.
