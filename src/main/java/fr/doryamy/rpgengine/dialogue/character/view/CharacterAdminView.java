package fr.doryamy.rpgengine.dialogue.character.view;

import java.util.List;
import java.util.Objects;

/** Vue complète de la bibliothèque globale de personnages. */
public record CharacterAdminView(
        List<CharacterAdminEntryView> characters,
        List<CharacterAdminTriggerView> triggers
) {
    public CharacterAdminView {
        characters = List.copyOf(Objects.requireNonNull(characters, "characters"));
        triggers = List.copyOf(Objects.requireNonNull(triggers, "triggers"));
    }
}
