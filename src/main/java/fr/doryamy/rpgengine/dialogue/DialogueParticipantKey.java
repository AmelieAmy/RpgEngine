package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

public record DialogueParticipantKey(String value) {

    public DialogueParticipantKey {
        Objects.requireNonNull(value, "value");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "La clé du participant ne peut pas être vide."
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
