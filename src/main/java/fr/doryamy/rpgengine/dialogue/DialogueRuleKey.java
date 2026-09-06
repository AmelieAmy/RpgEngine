package fr.doryamy.rpgengine.dialogue;

/**
 * Identité stable d'une occurrence de Condition
 * ou d'Action appartenant à un élément de dialogue.
 */
public record DialogueRuleKey(
        String value
) {

    public DialogueRuleKey {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "La clé d'une règle de dialogue "
                            + "ne peut pas être vide."
            );
        }

        value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}