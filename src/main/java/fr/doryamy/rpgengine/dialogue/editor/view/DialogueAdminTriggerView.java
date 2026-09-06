package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.Objects;

/**
 * Projection administrative d'un trigger associé à un dialogue.
 */
public record DialogueAdminTriggerView(
        int id,
        String name,
        String type,
        String targetId,
        String displayTarget
) {

    public DialogueAdminTriggerView {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "L'identifiant du trigger doit être positif."
            );
        }

        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(displayTarget, "displayTarget");

        if (name.isBlank()
                || type.isBlank()
                || targetId.isBlank()
                || displayTarget.isBlank()) {
            throw new IllegalArgumentException(
                    "Les données du trigger d'administration ne peuvent pas être vides."
            );
        }

        name = name.trim();
        type = type.trim();
        targetId = targetId.trim();
        displayTarget = displayTarget.trim();
    }
}
