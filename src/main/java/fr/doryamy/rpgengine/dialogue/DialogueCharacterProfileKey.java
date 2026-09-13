package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

public record DialogueCharacterProfileKey(String value) {

    public DialogueCharacterProfileKey {
        Objects.requireNonNull(value, "value");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "La clé du profil de personnage ne peut pas être vide."
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
