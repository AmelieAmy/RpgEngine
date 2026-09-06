package fr.doryamy.rpgengine.dialogue.runtime.view;

import java.util.List;
import java.util.Objects;

/**
 * État visible courant d'un dialogue.
 *
 * <p>Cette projection ne contient aucune logique métier.
 */
public record DialogueView(
        String speaker,
        String text,
        DialogueInteractionType interactionType,
        List<DialogueChoiceView> choices
) {

    public DialogueView {

        Objects.requireNonNull(
                interactionType,
                "interactionType"
        );

        Objects.requireNonNull(
                choices,
                "choices"
        );

        choices =
                List.copyOf(
                        choices
                );
    }
}