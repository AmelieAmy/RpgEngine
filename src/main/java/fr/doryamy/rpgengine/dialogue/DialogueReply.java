package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Réplique prononcée par un PNJ ou par le joueur.
 *
 * <p>Une réplique possède ses propres Conditions
 * et Actions.
 */
public record DialogueReply(
        DialogueElementKey key,
        DialogueReplySpeaker speaker,
        String text,
        DialogueRules rules
) implements DialogueElement {

    public DialogueReply {

        Objects.requireNonNull(
                key,
                "La clé de la réplique ne peut pas être null."
        );

        Objects.requireNonNull(
                speaker,
                "Le locuteur de la réplique ne peut pas être null."
        );

        Objects.requireNonNull(
                rules,
                "Les règles de la réplique ne peuvent pas être null."
        );

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Le texte d'une réplique ne peut pas être vide."
            );
        }

        text = text.trim();
    }
}