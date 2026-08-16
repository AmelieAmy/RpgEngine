package fr.doryamy.rpgengine.dialogue.presentation;

import java.util.List;

/**
 * État visible courant d'un dialogue.
 *
 * <p>Cette vue est produite par le Dialogue Engine puis transmise
 * à un {@link DialoguePresenter}. Elle ne contient que les données
 * nécessaires à la présentation.
 *
 * @param speaker         locuteur éventuel
 * @param text            texte actuellement affiché
 * @param interactionType interaction attendue du joueur
 * @param choices         choix actuellement disponibles
 */
public record DialogueView(
        String speaker,
        String text,
        DialogueInteractionType interactionType,
        List<DialogueChoiceView> choices
) {

    /**
     * Normalise les choix sous forme de liste non nulle
     * et immuable.
     */
    public DialogueView {
        choices =
                choices == null
                        ? List.of()
                        : List.copyOf(
                        choices
                );
    }
}