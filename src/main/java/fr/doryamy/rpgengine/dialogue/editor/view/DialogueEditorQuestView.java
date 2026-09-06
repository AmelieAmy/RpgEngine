package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.Objects;

/** Projection d'une quête disponible dans l'éditeur. */
public record DialogueEditorQuestView(
        String id,
        String name,
        String description
) {
    public DialogueEditorQuestView {
        id = requireNonBlank(id, "id");
        name = requireNonBlank(name, "name");
        description = Objects.requireNonNull(description, "description");
    }

    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " ne peut pas être vide.");
        }
        return normalized;
    }
}
