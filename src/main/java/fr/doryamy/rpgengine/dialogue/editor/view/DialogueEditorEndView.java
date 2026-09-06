package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.Objects;

/**
 * Projection d'une fin système
 * d'une branche de dialogue.
 */
public record DialogueEditorEndView(
        String key
) implements DialogueEditorElementView {

    public DialogueEditorEndView {

        key = Objects.requireNonNull(
                key,
                "key"
        ).trim();

        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                    "La clé de la fin ne peut pas être vide."
            );
        }
    }
}