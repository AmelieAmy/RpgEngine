package fr.doryamy.rpgengine.dialogue;

/**
 * Identité métier d'un élément appartenant
 * au graphe d'un dialogue.
 */
public record DialogueElementKey(
        String value
) {

    public DialogueElementKey {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "La clé d'un élément de dialogue ne peut pas être vide."
            );
        }

        value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}