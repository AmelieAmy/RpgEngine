package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.List;
import java.util.Objects;

/** Projection d'une réplique PNJ ou Joueur pour l'éditeur. */
public record DialogueEditorReplyView(
        String key,
        String speaker,
        String text,
        String characterProfileKey,
        String characterDisplayName,
        List<DialogueEditorConditionView> conditions,
        List<DialogueEditorActionView> actions
) implements DialogueEditorElementView {

    public DialogueEditorReplyView {
        key = requireNonBlank(key, "key");
        speaker = requireNonBlank(speaker, "speaker");
        text = requireNonBlank(text, "text");

        if ("NPC".equals(speaker)) {
            characterProfileKey = requireNonBlank(characterProfileKey, "characterProfileKey");
            characterDisplayName = requireNonBlank(characterDisplayName, "characterDisplayName");
        } else {
            characterProfileKey = normalizeNullable(characterProfileKey);
            characterDisplayName = normalizeNullable(characterDisplayName);
        }

        conditions = List.copyOf(Objects.requireNonNull(conditions, "conditions"));
        actions = List.copyOf(Objects.requireNonNull(actions, "actions"));
    }

    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " ne peut pas être vide.");
        }
        return normalized;
    }

    private static String normalizeNullable(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
