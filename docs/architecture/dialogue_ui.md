# Dialogue UI — Architecture

> **Statut : architecture de référence**
>
> **Cible : Minecraft 1.21.1 / NeoForge 21.1.228 / Arclight 1.0.2-SNAPSHOT+0769551**

## 1. Objectif

La V2 du Dialogue Engine porte toute la logique serveur :

- graphe ;
- validation ;
- navigation ;
- conditions ;
- actions ;
- sessions ;
- choix ;
- fins explicites.

La Dialogue UI ajoute uniquement une **couche de présentation cliente**.

```text
RPGEngine Plugin
      │
      │ logique métier
      ▼
Dialogue Engine
      │
      ▼
Presentation / Bridge
      │
      ▼
RPGEngine NeoForge Mod
      │
      ▼
DialogueScreen
```

> **Le serveur décide. Le client affiche et transmet les interactions.**

Le mod client ne devient jamais une source de vérité.

---

# 2. Environnement cible

```text
Minecraft : 1.21.1
NeoForge  : 21.1.228
Arclight  : 1.0.2-SNAPSHOT+0769551
Java      : 21
```

Architecture serveur :

```text
Arclight
├── plugins/
│   └── RPGEngine Plugin
└── mods/
    └── RPGEngine NeoForge Mod
```

Le mod est développé nativement pour NeoForge.

Architectury et FTB Library ne sont pas des dépendances RPGEngine par défaut.

---

# 3. Répartition des responsabilités

## RPGEngine Plugin

Le plugin reste responsable de :

```text
persistance
DialogueRepository
DialogueService
DialogueValidator
DialogueNavigator
DialogueRunner
DialogueSessionManager
ConditionManager
ActionManager
TriggerManager
```

Il ne dépend pas directement de NeoForge.

## RPGEngine NeoForge Mod

Le mod est responsable de :

```text
networking
DialogueScreen
rendu client
inputs
animations
HUD futur
```

Il ne réimplémente pas le Dialogue Engine.

---

# 4. Bridge plugin ↔ mod

Le bridge bidirectionnel a été **validé en conditions réelles sous Arclight**.

```text
PLUGIN
  ⇄
MOD SERVEUR
  ⇄
MOD CLIENT
```

Chemin serveur → client :

```text
RPGEngine Plugin
      ↓
NeoForgeBridge
      ↓
RpgEngineBridgeApi
      ↓
PacketDistributor
      ↓
client NeoForge
```

Chemin client → serveur → plugin :

```text
client NeoForge
      ↓
payload
      ↓
ServerPayloadHandler
      ↓
RpgEngineBridgeApi
      ↓
callback plugin
      ↓
RPGEngine Plugin
```

Règles figées :

- aucune dépendance compile-time du plugin vers NeoForge ;
- réflexion confinée à la couche bridge ;
- API du mod minimale ;
- callback plugin enregistré dans le mod ;
- callback supprimé à l'arrêt du plugin ;
- communications adressées à un joueur précis ;
- l'UUID d'un retour client provient du contexte réseau serveur ;
- le client ne fournit jamais lui-même l'identité du joueur.

Les payloads et commandes `BridgeTest...` sont temporaires.

---

# 5. Présentation et transport

Deux responsabilités restent séparées.

```text
DialogueRunner
      ↓
DialoguePresenter
      ↓
DialogueView
      ↓
DialogueBridge
      ↓
payload NeoForge
      ↓
DialogueScreen
```

`DialoguePresenter` décrit **quoi afficher**.

`DialogueBridge` décrit **comment l'envoyer au client**.

Le `DialogueRunner` ne doit plus dépendre directement de `Player.sendMessage(...)` pour l'affichage final.

---

# 6. Modèle d'affichage

Le client ne reçoit jamais l'objet métier `Dialogue` ni le graphe complet.

Il reçoit uniquement l'état visible courant.

```text
DialogueView
├── speaker éventuel
├── text
├── interactionType
└── choices
```

```text
DialogueChoiceView
├── position
└── label
```

Le client ne reçoit pas :

```text
conditions
actions
transitions cachées
nodes futurs
variables joueur
```

Le `speaker` reste optionnel tant que sa source métier n'est pas définie.

---

# 7. Modes d'interaction

Les types de transition métier restent :

```text
AUTO
CHOICE
END
```

La couche UI les traduit en modes d'interaction :

```text
AUTO   → CONTINUE
CHOICE → CHOICE
END    → CLOSE
```

Conceptuellement :

```java
public enum DialogueInteractionType {
    CONTINUE,
    CHOICE,
    CLOSE
}
```

## AUTO → CONTINUE

Une transition `AUTO` n'implique plus une progression visuelle instantanée.

```text
"Bonjour aventurier !"
        ↓
   [ Suite ]
```

Le bouton `Suite` demande au serveur de poursuivre.

```text
Suite
  ↓
CONTINUE_DIALOGUE
  ↓
DialogueRunner.advance(playerUuid)
  ↓
réévaluation serveur
  ↓
node suivant
```

Plus tard, un timer pourra déclencher la même opération automatiquement sans changer le moteur.

## CHOICE → CHOICE

```text
"Es tu prêt ?"

[ Oui ]
[ Peut être plus tard... ]
```

Un choix reste une décision narrative réelle.

```text
SELECT_DIALOGUE_CHOICE(position)
        ↓
DialogueRunner.choose(playerUuid, position)
```

Le serveur réévalue toujours les transitions disponibles avant d'accepter le choix.

## END → CLOSE

Le dernier node reste visible.

```text
"Reviens me voir quand elles seront toutes rentrées."

[ Terminer ]
```

Le bouton `Terminer` avertit le serveur que le joueur a fini de lire.

```text
FINISH_DIALOGUE
      ↓
DialogueRunner.finish(playerUuid)
      ↓
fin de session
      ↓
DISMISS_DIALOGUE
      ↓
fermeture de l'écran
```

`END` ne provoque donc plus une disparition immédiate de l'interface.

---

# 8. API runtime visée

Le runtime doit distinguer trois intentions :

```java
advance(UUID playerUuid)

choose(UUID playerUuid, int position)

finish(UUID playerUuid)
```

Rôle :

```text
advance → progression AUTO
choose  → sélection CHOICE
finish  → confirmation END
```

Les trois opérations doivent revalider l'état serveur avant d'agir.

---

# 9. Protocole Dialogue UI V1

## Serveur → client

```text
OPEN_DIALOGUE
UPDATE_DIALOGUE
DISMISS_DIALOGUE
```

- `OPEN_DIALOGUE` ouvre l'interface ;
- `UPDATE_DIALOGUE` remplace l'état visible ;
- `DISMISS_DIALOGUE` ferme l'interface sur ordre du serveur.

## Client → serveur

```text
CONTINUE_DIALOGUE
SELECT_DIALOGUE_CHOICE
FINISH_DIALOGUE
```

Les payloads clients restent minimaux :

```text
CONTINUE_DIALOGUE
└── aucune identité joueur

SELECT_DIALOGUE_CHOICE
└── position

FINISH_DIALOGUE
└── aucune identité joueur
```

Le joueur est identifié depuis la connexion réseau côté serveur.

---

# 10. Sécurité

Le client est toujours considéré comme non fiable.

Le serveur vérifie notamment :

- qu'une `DialogueSession` existe ;
- que le node courant correspond encore à la session ;
- que le mode d'interaction attendu correspond à la requête ;
- qu'une transition demandée existe ;
- qu'elle est du bon type ;
- que ses conditions sont encore valides.

Le client ne peut pas imposer :

```text
playerUuid
dialogueKey
transitionKey
nodeKey
```

---

# 11. Multijoueur

Chaque joueur possède son état propre.

```text
Player A
├── DialogueSession A
└── DialogueScreen A

Player B
├── DialogueSession B
└── DialogueScreen B
```

Les messages serveur → client sont toujours ciblés.

La présence du mod devra également être suivie par joueur afin de permettre un fallback vanilla éventuel.

Le nombre réduit de joueurs simultanés ne nécessite aucune infrastructure distribuée complexe.

---

# 12. Fallback vanilla

Le fallback doit rester possible architecturalement.

```text
mod présent
   ↓
DialogueScreen

mod absent
   ↓
présentation vanilla
```

La politique finale reste ouverte :

```text
mod obligatoire
ou
fallback autorisé
```

Une évolution naturelle est :

```text
DialoguePresenter
├── VanillaDialoguePresenter
└── ModdedDialoguePresenter
```

---

# 13. Première interface graphique

La première `DialogueScreen` doit rester simple :

- texte lisible ;
- bouton `Suite` pour `CONTINUE` ;
- boutons narratifs pour `CHOICE` ;
- bouton `Terminer` pour `CLOSE` ;
- souris ;
- clavier ;
- mise en page responsive.

Exemple :

```text
┌──────────────────────────────────────────────┐
│                                              │
│  Chief                                       │
│                                              │
│  Es tu prêt à relever un nouveau défi ?      │
│                                              │
│          [ Oui ]                             │
│          [ Peut être plus tard... ]          │
│                                              │
└──────────────────────────────────────────────┘
```

Portraits, animations et effets visuels viendront après validation du flux complet.

---

# 14. Test de référence

`chief_chickens` reste le scénario de non-régression.

```text
clic PNJ 210
     ↓
OPEN_DIALOGUE
     ↓
"Bonjour aventurier !"
[Suite]
     ↓
CONTINUE_DIALOGUE
     ↓
"Es tu prêt..."
[Oui] [Peut être plus tard...]
     ↓
SELECT_DIALOGUE_CHOICE
     ↓
suite du Dialogue Engine
```

Les conditions, actions et sessions doivent continuer à fonctionner sans modification fonctionnelle.

---

# 15. Questions ouvertes

Les points suivants ne sont pas encore figés :

- source exacte du `speaker` ;
- portrait ou modèle du locuteur ;
- handshake et détection du mod client ;
- politique `mod obligatoire` ou `fallback` ;
- structure physique finale des modules ;
- sérialisation exacte des vrais payloads ;
- détails de threading du callback vers Bukkit ;
- timer futur pour `CONTINUE`.

---

# 16. Principes à préserver

1. le plugin reste l'autorité métier ;
2. le mod reste une couche de présentation ;
3. le client reçoit uniquement l'état visible ;
4. le graphe complet reste serveur ;
5. les interactions clientes sont revalidées ;
6. présentation et transport restent séparés ;
7. le bridge plugin ↔ mod reste isolé ;
8. chaque communication est liée à un joueur précis ;
9. `AUTO`, `CHOICE` et `END` restent des concepts métier ;
10. `CONTINUE`, `CHOICE` et `CLOSE` sont des concepts d'interaction UI.

---

# Conclusion

La future interface graphique n'est pas une réécriture du Dialogue Engine.

Elle ajoute une couche au-dessus du moteur existant :

```text
Dialogue Engine
      ↓
DialoguePresenter
      ↓
DialogueView
      ↓
DialogueBridge
      ↓
NeoForge
      ↓
DialogueScreen
```

Le bridge bidirectionnel fonctionne déjà sous Arclight.

La prochaine étape consiste à adapter le runtime pour produire les trois états UI :

```text
CONTINUE
CHOICE
CLOSE
```

puis à les transporter avec le protocole Dialogue UI V1.
