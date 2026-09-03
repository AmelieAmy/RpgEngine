package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.DialogueInsertionKind;

/** Intention métier de création/extension d'un embranchement. */
public record CreateDialogueBranchRequest(
        String dialogueKey,
        String transitionKey,
        DialogueInsertionKind insertionKind,
        int position,
        String existingChoiceLabel,
        String newChoiceLabel
) {
}
