package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.Objects;

/**
 * Projection du point de départ système
 * d'un dialogue.
 */
public record DialogueEditorStartView(
        String key
) implements DialogueEditorElementView {

    public DialogueEditorStartView {

        key = Objects.requireNonNull(
                key,
                "key"
        ).trim();

        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                    "La clé du Start ne peut pas être vide."
            );
        }
    }
}