package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.List;
import java.util.Objects;

/**
 * Entrée résumée d'un dialogue dans l'administration.
 */
public record DialogueAdminEntryView(
        String key,
        String name,
        List<DialogueAdminTriggerView> triggers
) {

    public DialogueAdminEntryView {

        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(triggers, "triggers");

        if (key.isBlank()) {
            throw new IllegalArgumentException(
                    "La clé du dialogue ne peut pas être vide."
            );
        }

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom du dialogue ne peut pas être vide."
            );
        }

        key = key.trim();
        name = name.trim();
        triggers = List.copyOf(triggers);

        if (triggers.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "La liste des triggers contient une entrée nulle."
            );
        }
    }
}
