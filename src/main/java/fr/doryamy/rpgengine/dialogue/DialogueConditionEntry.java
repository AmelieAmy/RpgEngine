package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.model.Condition;

import java.util.Objects;

/**
 * Occurrence identifiable d'une Condition
 * appartenant à un élément de dialogue.
 */
public record DialogueConditionEntry(
        DialogueRuleKey key,
        Condition condition
) {

    public DialogueConditionEntry {

        Objects.requireNonNull(
                key,
                "La clé de la condition ne peut pas être null."
        );

        Objects.requireNonNull(
                condition,
                "La condition ne peut pas être null."
        );
    }
}