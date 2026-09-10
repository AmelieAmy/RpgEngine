package fr.doryamy.rpgengine.dialogue.runtime.view;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * État visible courant d'un dialogue.
 *
 * <p>Cette projection ne contient aucune logique métier.
 *
 * <p>Les participants visibles sont transportés séparément du
 * participant actif. Cette séparation permet à une future scène
 * de dialogue d'afficher plusieurs participants tout en indiquant
 * explicitement lequel prend actuellement la parole.
 */
public record DialogueView(
        List<DialogueParticipantView> participants,
        String activeParticipantKey,
        String text,
        DialogueInteractionType interactionType,
        List<DialogueChoiceView> choices
) {

    public DialogueView {

        Objects.requireNonNull(
                participants,
                "participants"
        );

        Objects.requireNonNull(
                interactionType,
                "interactionType"
        );

        Objects.requireNonNull(
                choices,
                "choices"
        );

        participants =
                List.copyOf(
                        participants
                );

        choices =
                List.copyOf(
                        choices
                );

        Set<String> participantKeys =
                new HashSet<>();

        for (DialogueParticipantView participant : participants) {

            if (!participantKeys.add(
                    participant.key()
            )) {
                throw new IllegalArgumentException(
                        "Plusieurs participants de dialogue utilisent la même clé : "
                                + participant.key()
                );
            }
        }

        if (activeParticipantKey != null) {

            activeParticipantKey =
                    activeParticipantKey.trim();

            if (activeParticipantKey.isEmpty()) {
                throw new IllegalArgumentException(
                        "La clé du participant actif ne peut pas être vide."
                );
            }

            if (!participantKeys.contains(
                    activeParticipantKey
            )) {
                throw new IllegalArgumentException(
                        "Le participant actif "
                                + activeParticipantKey
                                + " n'existe pas dans la scène de dialogue."
                );
            }
        }
    }
}
