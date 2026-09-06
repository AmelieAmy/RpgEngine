package fr.doryamy.rpgengine.dialogue;

/**
 * Identité métier d'un dialogue.
 */
public record DialogueKey(
        String value
) {

    public DialogueKey {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "La clé du dialogue ne peut pas être vide."
            );
        }

        value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}