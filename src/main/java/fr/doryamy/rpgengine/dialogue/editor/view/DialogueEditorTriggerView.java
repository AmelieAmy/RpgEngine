package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.Objects;

/** Projection d'un trigger affiché dans l'éditeur d'un dialogue. */
public record DialogueEditorTriggerView(
        int id,
        String name,
        String type,
        String targetId,
        String displayTarget
) {
    public DialogueEditorTriggerView {
        if (id <= 0) throw new IllegalArgumentException("L'identifiant du trigger doit être positif.");
        name = requireNonBlank(name, "name");
        type = requireNonBlank(type, "type");
        targetId = requireNonBlank(targetId, "targetId");
        displayTarget = requireNonBlank(displayTarget, "displayTarget");
    }

    private static String requireNonBlank(String value, String field) {
        Objects.requireNonNull(value, field);
        String normalized = value.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(field + " ne peut pas être vide.");
        return normalized;
    }
}
