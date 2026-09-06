package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.model.Action;

import java.util.Objects;

/**
 * Occurrence identifiable d'une Action
 * appartenant à un élément de dialogue.
 */
public record DialogueActionEntry(
        DialogueRuleKey key,
        Action action
) {

    public DialogueActionEntry {

        Objects.requireNonNull(
                key,
                "La clé de l'action ne peut pas être null."
        );

        Objects.requireNonNull(
                action,
                "L'action ne peut pas être null."
        );
    }
}