package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Réplique prononcée par un participant explicitement déclaré
 * dans l'agrégat Dialogue.
 *
 * <p>La réplique ne déduit jamais l'identité de son locuteur :
 * elle référence uniquement sa clé de participant.
 */
public record DialogueReply(
        DialogueElementKey key,
        DialogueParticipantKey participantKey,
        String text,
        DialogueRules rules
) implements DialogueElement {

    public DialogueReply {
        Objects.requireNonNull(
                key,
                "La clé de la réplique ne peut pas être null."
        );

        Objects.requireNonNull(
                participantKey,
                "La clé du participant de la réplique ne peut pas être null."
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
