package fr.doryamy.rpgengine.dialogue.presentation;

/**
 * Représentation visible d'un choix de dialogue.
 *
 * <p>Cette vue contient uniquement les informations nécessaires
 * à la présentation du choix. Elle ne contient aucune condition,
 * action ou transition métier.
 *
 * @param position position identifiant le choix dans l'interaction courante
 * @param label    texte affiché au joueur
 */
public record DialogueChoiceView(
        int position,
        String label
) {
}