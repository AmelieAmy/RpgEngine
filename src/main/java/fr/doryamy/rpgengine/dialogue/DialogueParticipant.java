package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Participant déclaré dans un dialogue.
 *
 * <p>Un PNJ référence un profil global de la bibliothèque.
 * Le joueur est dynamique et ne référence donc aucun profil.
 */
public record DialogueParticipant(
        DialogueParticipantKey key,
        DialogueParticipantType type,
        DialogueCharacterProfileKey characterProfileKey
) {

    public DialogueParticipant {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        if (type == DialogueParticipantType.NPC
                && characterProfileKey == null) {
            throw new IllegalArgumentException(
                    "Un participant PNJ doit référencer un profil de personnage."
            );
        }

        if (type == DialogueParticipantType.PLAYER
                && characterProfileKey != null) {
            throw new IllegalArgumentException(
                    "Un participant joueur ne doit pas référencer "
                            + "un profil de personnage statique."
            );
        }
    }
}
