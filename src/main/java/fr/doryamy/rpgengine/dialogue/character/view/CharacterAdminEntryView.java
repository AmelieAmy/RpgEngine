package fr.doryamy.rpgengine.dialogue.character.view;

/** Projection d'un personnage de la bibliothèque pour l'administration. */
public record CharacterAdminEntryView(
        String key,
        String name,
        String portraitResource,
        String citizensNpcId,
        String citizensNpcName
) {}
