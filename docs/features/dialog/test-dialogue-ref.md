# Dialogue de référence — V3

Ce document décrit le scénario de référence utilisé pour tester l’intégration complète de RPGEngine.

Il remplace le scénario V2 basé sur les variables joueur pour la progression de quête. À partir de la V3, **FTB Quests devient la source de vérité pour l’état des quêtes**.

Le scénario valide désormais le Dialogue Engine, le Trigger Engine, le Rule Engine, l’interface NeoForge, le bridge plugin ↔ mod, la lecture de l’état FTB Quests, le démarrage d’une quête FTB Quests et la persistance SQLite.

---

# 1. Environnement de référence

PNJ Citizens :

```text
210
```

Dialogue principal :

```text
chief_chickens
```

Quête FTB Quests :

```text
5ADADA0BEE4C4B2C
```

États RPGEngine exposés pour FTB Quests :

```text
NOT_STARTED
ACTIVE
COMPLETED
UNAVAILABLE
```

`UNAVAILABLE` est un état technique et ne doit normalement pas piloter la narration.

---

# 2. Règle d’architecture

FTB Quests est la source de vérité pour la progression de la quête. RPGEngine ne doit plus maintenir une variable parallèle du type :

```text
chickens_quest=started
```

Le flux attendu est :

```text
FTB Quests
    │
    ▼
FtbQuestBridge          (mod NeoForge)
    │
    ▼
RpgEngineBridgeApi
    │ réflexion
    ▼
NeoForgeBridge          (plugin)
    │
    ▼
QuestService
    ├── QuestConditionProvider
    └── QuestActionExecutor
```

Le Dialogue Engine reste indépendant de FTB Quests.

---

# 3. Scénario narratif

## 3.1 Quête NOT_STARTED

```text
Chief:
"Bonjour aventurier !"

        │
        │ CONTINUE
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
        │ CONTINUE                           END
        ▼
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
Action QUEST:                        Chief:
START:5ADADA0BEE4C4B2C               "Je vois que je devrai
        │                             me débrouiller seul..."
        ▼                                    │
Chief:                                      END
"Merci aventurier !"
        │
        │ CONTINUE
        ▼
Chief:
"Reviens me voir quand elles
seront toutes rentrées."
        │
       END
```

Après `J'accepte`, l’état FTB attendu est :

```text
ACTIVE
```

## 3.2 Quête ACTIVE

Le dialogue d’introduction `chief_chickens` ne doit plus être déclenché par son trigger `NOT_STARTED`.

Un dialogue de suivi dédié doit être utilisé, par exemple :

```text
chief_chickens_active
```

Scénario de référence :

```text
Chief:
"Re-bonjour aventurier !"

        │
        │ CONTINUE
        ▼

Chief:
"As-tu retrouvé toutes les poules ?"

        │
        ├─────────────────────────────┐
        │                             │
        ▼                             ▼
Joueur: "Oui"                 Joueur: "Pas encore..."
        │                             │
        ▼                             ▼
Traitement futur               Chief:
de complétion FTB              "Elles sont dures à attraper,
        │                       hein ? C'est pour ça
        ▼                       que c'est un défi !"
Dialogue de fin                       │
                                     END
```

La complétion contrôlée de la quête FTB n’est pas encore considérée comme stabilisée dans cette version du scénario. Le test `ACTIVE` vérifie principalement le routage narratif selon l’état FTB.

## 3.3 Quête COMPLETED

Un dialogue dédié peut être utilisé, par exemple :

```text
chief_chickens_completed
```

Scénario de référence :

```text
Chief:
"Ho, c'est toi aventurier !"

        │
        │ CONTINUE
        ▼

Chief:
"Désolé mais je n'ai pas de nouveaux défis pour toi pour l'instant.
Reviens me voir dans quelques jours !"

        │
       END
```

---

# 4. Graphe technique — chief_chickens

```text
chief_greeting
        │
        │ AUTO → interaction UI CONTINUE
        ▼
chief_challenge
        │
        ├── CHOICE "Oui"
        │       │
        │       ▼
        │   chief_problem
        │       │
        │       │ AUTO → interaction UI CONTINUE
        │       ▼
        │   chief_explanation
        │       │
        │       ├── CHOICE "J'accepte"
        │       │       │
        │       │       ├── ACTION
        │       │       │   QUEST
        │       │       │   START:5ADADA0BEE4C4B2C
        │       │       │
        │       │       ▼
        │       │   chief_thanks
        │       │       │
        │       │       │ AUTO → interaction UI CONTINUE
        │       │       ▼
        │       │   chief_return
        │       │       │
        │       │      END → interaction UI CLOSE
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

Important :

```text
AUTO   ≠ progression instantanée
AUTO   = aucune décision narrative
UI     = bouton "Suite"
```

À terme, une transition `AUTO` pourra évoluer vers un passage automatique après timer.

---

# 5. Nodes — chief_chickens

| Clé | Texte |
|---|---|
| `chief_greeting` | Bonjour aventurier ! |
| `chief_challenge` | Es tu prêt a relever un nouveau défi ? |
| `chief_problem` | Mes poules se sont échappées ! |
| `chief_explanation` | Il faut les trouver et les remettre dans le poulailler. |
| `chief_thanks` | Merci aventurier ! |
| `chief_return` | Reviens me voir quand elles seront toutes rentrées. |
| `chief_decline_challenge` | Très bien. Reviens me voir si tu changes d'avis. |
| `chief_decline_quest` | Je vois que je devrai me débrouiller seul... |

Node de départ :

```text
chief_greeting
```

---

# 6. Transitions — chief_chickens

| Clé | Source | Type | Cible | Label |
|---|---|---|---|---|
| `greeting_to_challenge` | `chief_greeting` | `AUTO` | `chief_challenge` | — |
| `challenge_accept` | `chief_challenge` | `CHOICE` | `chief_problem` | Oui |
| `challenge_decline` | `chief_challenge` | `CHOICE` | `chief_decline_challenge` | Peut être plus tard... |
| `problem_to_explanation` | `chief_problem` | `AUTO` | `chief_explanation` | — |
| `quest_accept` | `chief_explanation` | `CHOICE` | `chief_thanks` | J'accepte |
| `quest_decline` | `chief_explanation` | `CHOICE` | `chief_decline_quest` | Faites le vous-même ! |
| `thanks_to_return` | `chief_thanks` | `AUTO` | `chief_return` | — |
| `return_end` | `chief_return` | `END` | — | — |
| `decline_challenge_end` | `chief_decline_challenge` | `END` | — | — |
| `decline_quest_end` | `chief_decline_quest` | `END` | — | — |

---

# 7. Création du dialogue

## Dialogue

```text
/rpg dialog create chief_chickens Les poules du Chef
```

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

## Node de départ

```text
/rpg dialog start chief_chickens chief_greeting
```

---

# 8. Création des transitions

```text
/rpg dialog transition add chief_chickens chief_greeting greeting_to_challenge
/rpg dialog transition set chief_chickens greeting_to_challenge AUTO chief_challenge

/rpg dialog transition add chief_chickens chief_challenge challenge_accept
/rpg dialog transition set chief_chickens challenge_accept CHOICE chief_problem Oui

/rpg dialog transition add chief_chickens chief_challenge challenge_decline
/rpg dialog transition set chief_chickens challenge_decline CHOICE chief_decline_challenge Peut être plus tard...

/rpg dialog transition add chief_chickens chief_problem problem_to_explanation
/rpg dialog transition set chief_chickens problem_to_explanation AUTO chief_explanation

/rpg dialog transition add chief_chickens chief_explanation quest_accept
/rpg dialog transition set chief_chickens quest_accept CHOICE chief_thanks J'accepte

/rpg dialog transition add chief_chickens chief_explanation quest_decline
/rpg dialog transition set chief_chickens quest_decline CHOICE chief_decline_quest Faites le vous-même !

/rpg dialog transition add chief_chickens chief_thanks thanks_to_return
/rpg dialog transition set chief_chickens thanks_to_return AUTO chief_return

/rpg dialog transition add chief_chickens chief_return return_end
/rpg dialog transition add chief_chickens chief_decline_challenge decline_challenge_end
/rpg dialog transition add chief_chickens chief_decline_quest decline_quest_end
```

Les trois dernières transitions restent de type `END`.

---

# 9. Vérification du graphe

```text
/rpg dialog info chief_chickens
```

Résultat attendu :

```text
Nodes : 8
Transitions : 10
Node de départ : chief_greeting
```

---

# 10. Prévisualisation

```text
/rpg dialog preview chief_chickens
```

Résultat attendu :

```text
Bonjour aventurier !
```

La preview ne crée aucune `DialogueSession`, n’exécute aucune action, ne parcourt pas le graphe et n’est pas une interaction runtime.

---

# 11. Trigger PNJ — état NOT_STARTED

Le PNJ de référence possède l’ID Citizens :

```text
210
```

Créer le trigger :

```sql
INSERT INTO trigger (
    name,
    type,
    target_id,
    enabled
)
VALUES (
    'chief_chickens_not_started',
    'NPC',
    '210',
    1
);
```

Ajouter l’action DIALOG :

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

Ajouter la condition FTB Quests :

```sql
INSERT INTO condition (
    trigger_id,
    provider,
    expression
)
VALUES (
    <trigger_id>,
    'QUEST',
    '5ADADA0BEE4C4B2C==NOT_STARTED'
);
```

Si l’état retourné est `NOT_STARTED`, l’action `DIALOG` est exécutée.

---

# 12. Action FTB sur quest_accept

Ajouter l’action permanente :

```text
/rpg dialog transition action add chief_chickens quest_accept QUEST START:5ADADA0BEE4C4B2C
```

Vérifier :

```text
/rpg dialog transition action list chief_chickens quest_accept
```

Résultat attendu :

```text
Actions de la transition 'quest_accept' :
1 - QUEST | START:5ADADA0BEE4C4B2C
```

Aucune variable `PLAYER` ne doit mémoriser l’acceptation de cette quête.

---

# 13. Test runtime — chemin positif

État FTB initial :

```text
NOT_STARTED
```

Cliquer sur le PNJ `210`.

Le client NeoForge doit afficher successivement :

```text
Bonjour aventurier !
[Suite]
```

puis :

```text
Es tu prêt a relever un nouveau défi ?

[Oui]
[Peut être plus tard...]
```

Cliquer sur `Oui`, puis sur `Suite`, puis sur `J'accepte`.

La transition `quest_accept` doit exécuter :

```text
QUEST | START:5ADADA0BEE4C4B2C
```

avant de poursuivre vers `chief_thanks`.

Résultat attendu :

```text
Merci aventurier !
[Suite]
```

puis :

```text
Reviens me voir quand elles seront toutes rentrées.
[Terminer]
```

Le bouton `Terminer` doit terminer la session et fermer `DialogueScreen` via le serveur.

---

# 14. Validation de l’état ACTIVE

Après acceptation, l’état FTB attendu est :

```text
ACTIVE
```

Le trigger `chief_chickens_not_started` possède toujours :

```text
5ADADA0BEE4C4B2C==NOT_STARTED
```

Un nouveau clic sur le PNJ ne doit donc plus relancer `chief_chickens`.

Une condition de test :

```text
5ADADA0BEE4C4B2C==ACTIVE
```

doit être vraie.

---

# 15. Test des refus

## Premier refus

Choisir :

```text
Peut être plus tard...
```

Résultat attendu :

```text
Très bien. Reviens me voir si tu changes d'avis.
[Terminer]
```

La quête doit rester `NOT_STARTED`.

## Second refus

Choisir d’abord `Oui`, puis :

```text
Faites le vous-même !
```

Résultat attendu :

```text
Je vois que je devrai me débrouiller seul...
[Terminer]
```

La quête doit rester `NOT_STARTED`.

---

# 16. Test du filtrage dynamique d’une transition

Pour tester la réévaluation des conditions dans `DialogueNavigator`, une condition `QUEST` peut être ajoutée temporairement à une transition `CHOICE` :

```text
/rpg dialog transition condition add chief_chickens challenge_accept QUEST 5ADADA0BEE4C4B2C==NOT_STARTED
```

Lorsque la quête est `NOT_STARTED`, le choix `Oui` doit être disponible.

Lorsque la quête est `ACTIVE`, le choix `Oui` doit disparaître.

Le client ne reçoit que les choix encore autorisés par le serveur, et la sélection est réévaluée au moment du clic.

---

# 17. Test des états QUEST

Expressions de référence :

```text
5ADADA0BEE4C4B2C==NOT_STARTED
5ADADA0BEE4C4B2C==ACTIVE
5ADADA0BEE4C4B2C==COMPLETED
```

`STARTED` n’est pas un état RPGEngine valide.

`UNAVAILABLE` indique qu’une lecture fiable de FTB Quests n’a pas pu être effectuée.

---

# 18. Protocole UI NeoForge validé

Serveur → client :

```text
OpenDialoguePayload
DismissDialoguePayload
```

Client → serveur :

```text
ContinueDialoguePayload
SelectDialogueChoicePayload
FinishDialoguePayload
```

Le client ne transmet jamais l’UUID du joueur, le dialogue courant, le node courant, une cible de transition ou une action à exécuter. Le serveur reste l’autorité sur la session et la progression.

---

# 19. Chaîne d’intégration — dialogue

```text
NPCListener
      │
      ▼
TriggerManager
      │
      ├────────────────────┐
      ▼                    ▼
ConditionManager       ActionManager
      │                    │
      ▼                    ▼
QuestConditionProvider  DIALOG
                           │
                           ▼
                   DialogActionExecutor
                           │
                           ▼
                    DialogueRunner
                           │
                           ▼
                  DialoguePresenter
                           │
                           ▼
                   NeoForgeBridge
                           │
                           ▼
                  RpgEngineBridgeApi
                           │
                           ▼
                    DialogueScreen
```

---

# 20. Chaîne d’intégration — acceptation FTB

```text
DialogueTransition quest_accept
        │
        ▼
ActionManager
        │
        ▼
QuestActionExecutor
        │
        ▼
QuestService.start()
        │
        ▼
FtbQuestService
        │
        ▼
NeoForgeBridge.startQuest()
        │
        ▼
RpgEngineBridgeApi.startQuest()
        │
        ▼
FtbQuestBridge.startQuest()
        │
        ▼
FTB Quests TeamData
        │
        ▼
état ACTIVE
```

---

# 21. Commandes runtime supprimées

Les commandes suivantes ne font plus partie du scénario :

```text
/rpg dialog continue
/rpg dialog choose
/rpg dialog finish
/rpg bridge test
```

Elles ont été remplacées par l’interface NeoForge. Les commandes `/rpg dialog ...` restantes servent à l’administration, à l’édition ou à la prévisualisation.

---

# 22. Test de suppression d’un dialogue

Ne pas utiliser `chief_chickens` :

```text
/rpg dialog create delete_test Dialogue temporaire
/rpg dialog list
/rpg dialog delete delete_test
/rpg dialog list
```

`delete_test` ne doit plus apparaître.

---

# 23. Critères de non-régression V3

Le scénario est réussi si :

- le plugin et le mod démarrent sans erreur de bridge ;
- le dialogue peut être créé et édité depuis les commandes ;
- le graphe est validé correctement ;
- le PNJ `210` déclenche le bon trigger ;
- la condition `QUEST` lit réellement l’état FTB Quests ;
- `NOT_STARTED`, `ACTIVE` et `COMPLETED` sont distingués ;
- une transition `AUTO` affiche un bouton `Suite` ;
- une transition `CHOICE` affiche uniquement les choix autorisés ;
- une transition `END` affiche un bouton `Terminer` ;
- le serveur réévalue les transitions avant progression ;
- `quest_accept` exécute `QUEST | START:5ADADA0BEE4C4B2C` ;
- FTB Quests passe ensuite à `ACTIVE` ;
- le trigger `NOT_STARTED` ne relance plus le dialogue d’introduction ;
- les refus ne démarrent pas la quête ;
- la `DialogueSession` est supprimée à la fin ;
- `DialogueScreen` est fermée par le serveur ;
- aucune variable joueur ne duplique l’état FTB de la quête ;
- aucune erreur inattendue n’apparaît dans les consoles.

---

# 24. Objectif de non-régression

`chief_chickens` reste le scénario de référence de RPGEngine.

Il doit être rejoué après toute modification importante concernant le Dialogue Engine, le Trigger Engine, le Rule Engine, les providers de conditions, les executors d’actions, le bridge plugin ↔ mod, le protocole réseau NeoForge, l’UI de dialogue ou l’intégration FTB Quests.

---

# Conclusion

Le scénario V3 valide désormais le cycle complet :

```text
état FTB Quests
      │
      ▼
condition QUEST
      │
      ▼
trigger PNJ
      │
      ▼
dialogue
      │
      ▼
interaction client
      │
      ▼
transition serveur
      │
      ▼
action QUEST
      │
      ▼
nouvel état FTB Quests
      │
      ▼
nouveau comportement narratif
```

FTB Quests est la source de vérité pour la progression de quête. RPGEngine reste responsable de la narration, des règles, des triggers, des dialogues et de leur présentation.
