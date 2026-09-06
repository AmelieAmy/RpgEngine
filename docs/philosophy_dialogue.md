# Philosophie des dialogues

## Roles Plugin VS Mod

La construction des dialogues que je qualifierais de "backend" doit être réalisé par le pluging.
Le mod lié à ce plugin doit permettre la visualisation des dialogues dans le jeu. 
C'est aussi le mod qui doit permettre d'administrer les dialogues. 

En résumé, à l'image d'un projet web, le mod serait le front-end tandis que le plugin joue le role du back-end.

## Vision

L'administration des dialogues doit être centré autour des besoins du créateur.

## Le Graph et ses éléments

Le graph est le cœur de la gestion d'un dialogue. Il doit permettre au créateur de créer/modifier/supprimer chaque élément de dialogue indépendamment.

Chaque élément du graph (à l'exception de la ligne et "fin de dialogue") possède :
- Une représentation par un tag ou un symbole.
- Un menu à gauche avec différentes options associées

### La réplique Joueur
```
    ┌────────┐ ╔════════════════════════════════════╗
[+] │ Joueur │ ║                              ┌────┐║
    └────────┘ ║ Ici le texte de la réplique  │ ✎ │║
               ║                              └────┘║
               ╚════════════════════════════════════╝
```
```
Le menu [+] doit contenir les options suivantes :
- Condition
- Action
- Supprimer
```

### La réplique PNJ
```
    ┌───────┐ ╔════════════════════════════════════╗
[+] │  PNJ  │ ║                              ┌────┐║
    └───────┘ ║ Ici le texte de la réplique  │ ✎ │║
              ║                              └────┘║
              ╚════════════════════════════════════╝
```
```
Le menu [+] doit contenir les options suivantes :
- Condition
- Action
- Supprimer
```

### L'embranchement
```
       │
       ^
[+]  /   \
    <     > ─────────────────────────────────┐
     \   /                                   │
       v                                     │
       │                                     │
  ┌────┴────┐ ╔═════════════════╗       ┌────┴────┐ ╔══════════════════╗
  │ Choix 1 │ ║ Texte 1er choix ║       │ Choix 2 │ ║ Texte 2eme choix ║
  └────┬────┘ ╚═════════════════╝       └────┬────┘ ╚══════════════════╝
       │                                     │
```
```
Le menu [+] doit contenir les options suivantes :
- Ajouter une branche
- Supprimer
```
```
L'option "Supprimer" amène à une fenêtre contenant les options suivantes :
- Supprimer le choix 1
- Supprimer le choix 2 (et ainsi de suite pour tout les choix disponibles)
- Supprimer tout l'embranchement
```

### Le choix d'embranchement
```
        │
        ^
      /   \
     <     > ─────────
      \   /             
        v              
        │              
   ┌────┴────┐ ╔═══════════════════════════════╗
[+]│ Choix N │ ║                         ┌────┐║
   └────┬────┘ ║ Ici le texte du choix   │ ✎ │║
        │      ║                         └────┘║
        │      ╚═══════════════════════════════╝
```
```
Le menu [+] doit contenir les options suivantes :
- Condition
- Action
- Supprimer
```

### La condition
```
       /────────────────────────────────/
      /                      ┌────┐    /
[+]  /   Ici la condition    │ ✎ │   /
    /                        └────┘  /
   /────────────────────────────────/
```
```
Le menu [+] doit contenir l'option suivante :
- Supprimer
```

### L'action
```
       /────────────────────────\
     /                   ┌────┐   \
[+]  |   Ici l'action    │ ✎ │    |
     \                   └────┘   /
       \────────────────────────/
```
```
Le menu [+] doit contenir l'option suivante :
- Supprimer
```

### La fin de dialogue
```
┌────────┴────────┐
│ Fin de dialogue │
└─────────────────┘
```
Cet élément n'est pas manipulable par le créateur. 
Il est automatiquement implémenté à la création d'un dialogue, puis à chaque nouvelle branche.

### La ligne
```
    ○
    │              
┌───┴───┐
│  Tag  │
└───┬───┘
```
La ligne représente le fil conducteur entre tous les élements du dialogue. 
Elle n'est pas manipulable par le créateur et ne fait que représentater la liaison entre les différents éléments.
Elle commence par un rond et finit par une "Fin de dialogue" pour chaque embranchement.

