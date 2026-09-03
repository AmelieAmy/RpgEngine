package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.DialogueInsertionKind;

/**
 * Intention serveur d'insertion d'une réplique PNJ
 * depuis l'éditeur graphique.
 */
public record InsertDialogueNpcReplyRequest(
        String dialogueKey,
        String transitionKey,
        DialogueInsertionKind insertionKind,
        int position,
        String text
) {
}
