package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.Objects;

/**
 * Projection d'un embranchement.
 */
public record DialogueEditorBranchView(
        String key
) implements DialogueEditorElementView {

    public DialogueEditorBranchView {

        key = Objects.requireNonNull(
                key,
                "key"
        ).trim();

        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                    "La clé de l'embranchement ne peut pas être vide."
            );
        }
    }
}