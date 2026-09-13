package fr.doryamy.rpgengine.dialogue.character.view;

/** Projection légère d'un trigger sélectionnable par la bibliothèque de personnages. */
public record CharacterAdminTriggerView(
        int id,
        String name,
        String type,
        String targetId
) {
}
