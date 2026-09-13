package fr.doryamy.rpgengine.dialogue.runtime.view;

import java.util.Objects;

/**
 * Participant visible dans l'état courant d'un dialogue.
 *
 * <p>La clé constitue l'identité stable du participant dans
 * la scène de présentation. Le type décrit son rôle visuel,
 * le nom correspond uniquement au libellé présenté au joueur
 * et portraitResource référence éventuellement un asset logique
 * géré par RPGEngine.
 */
public record DialogueParticipantView(
        String key,
        DialogueParticipantType type,
        String displayName,
        String portraitResource
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

        if (portraitResource != null) {
            portraitResource = portraitResource.trim();
            if (portraitResource.isEmpty()) {
                portraitResource = null;
            }
        }

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
