package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.DialogueDeletionKind;

/** Requête serveur-authoritative de suppression d'un élément du graphe. */
public record DeleteDialogueElementRequest(
        String dialogueKey,
        String transitionKey,
        DialogueDeletionKind deletionKind,
        int position
) {
}
