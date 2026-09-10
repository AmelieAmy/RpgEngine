package fr.doryamy.rpgengine.dialogue.runtime.view;

import java.util.Objects;

/**
 * Participant visible dans l'état courant d'un dialogue.
 *
 * <p>La clé constitue l'identité stable du participant dans
 * la scène de présentation. Le type décrit son rôle visuel et
 * le nom correspond uniquement au libellé présenté au joueur.
 */
public record DialogueParticipantView(
        String key,
        DialogueParticipantType type,
        String displayName
) {

    public DialogueParticipantView {

        key =
                Objects.requireNonNull(
                        key,
                        "key"
                ).trim();

        Objects.requireNonNull(
                type,
                "type"
        );

        displayName =
                Objects.requireNonNull(
                        displayName,
                        "displayName"
                ).trim();

        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                    "La clé d'un participant de dialogue ne peut pas être vide."
            );
        }

        if (displayName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Le nom visible d'un participant de dialogue ne peut pas être vide."
            );
        }
    }
}
