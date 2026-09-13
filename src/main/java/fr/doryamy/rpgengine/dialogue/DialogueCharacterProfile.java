package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/** Profil global de personnage, éventuellement lié à un PNJ Citizens. */
public record DialogueCharacterProfile(
        DialogueCharacterProfileKey key,
        String displayName,
        String portraitResource,
        String citizensNpcId
) {
    public DialogueCharacterProfile {
        Objects.requireNonNull(key, "key");
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Le nom affiché du profil ne peut pas être vide.");
        }
        displayName = displayName.trim();
        if (portraitResource != null) {
            portraitResource = portraitResource.trim();
            if (portraitResource.isEmpty()) portraitResource = null;
        }
        if (citizensNpcId != null) {
            citizensNpcId = citizensNpcId.trim();
            if (citizensNpcId.isEmpty()) citizensNpcId = null;
        }
    }
}
