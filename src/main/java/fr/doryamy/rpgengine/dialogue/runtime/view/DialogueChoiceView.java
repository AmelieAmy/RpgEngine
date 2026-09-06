package fr.doryamy.rpgengine.dialogue.runtime.view;

/**
 * Représentation visible d'un choix actuellement
 * proposé au joueur.
 *
 * <p>La clé constitue l'identité stable du choix.
 * La position représente uniquement son ordre
 * d'affichage dans l'interaction courante.
 *
 * @param key      identité métier stable du choix
 * @param position ordre d'affichage
 * @param label    texte affiché au joueur
 */
public record DialogueChoiceView(
        String key,
        int position,
        String label
) {

    public DialogueChoiceView {

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "La clé du choix présenté ne peut pas être vide."
            );
        }

        if (position < 0) {
            throw new IllegalArgumentException(
                    "La position du choix présenté "
                            + "ne peut pas être négative."
            );
        }

        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException(
                    "Le texte du choix présenté "
                            + "ne peut pas être vide."
            );
        }

        key = key.trim();
        label = label.trim();
    }
}