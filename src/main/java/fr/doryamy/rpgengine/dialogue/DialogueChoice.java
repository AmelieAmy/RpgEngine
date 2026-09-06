package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Choix appartenant à un embranchement.
 *
 * <p>Un choix possède son texte, sa position
 * d'affichage ainsi que ses propres Conditions
 * et Actions.
 */
public record DialogueChoice(
        DialogueElementKey key,
        String text,
        int position,
        DialogueRules rules
) implements DialogueElement {

    public DialogueChoice {

        Objects.requireNonNull(
                key,
                "La clé du choix ne peut pas être null."
        );

        Objects.requireNonNull(
                rules,
                "Les règles du choix ne peuvent pas être null."
        );

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Le texte d'un choix ne peut pas être vide."
            );
        }

        if (position < 0) {
            throw new IllegalArgumentException(
                    "La position d'un choix ne peut pas être négative."
            );
        }

        text = text.trim();
    }
}