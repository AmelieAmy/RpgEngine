package fr.doryamy.rpgengine.dialogue.editor;

import java.util.Objects;

/** Données de présentation résolues d'un trigger de dialogue. */
public record DialogueTriggerPresentation(
        int id,
        String name,
        String type,
        String targetId,
        String displayTarget
) {
    public DialogueTriggerPresentation {
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
