package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Liaison structurelle entre deux éléments
 * d'un dialogue.
 *
 * <p>Une liaison ne porte aucune logique métier.
 */
public record DialogueLink(
        DialogueElementKey source,
        DialogueElementKey target
) {

    public DialogueLink {

        Objects.requireNonNull(
                source,
                "La source d'une liaison ne peut pas être null."
        );

        Objects.requireNonNull(
                target,
                "La cible d'une liaison ne peut pas être null."
        );
    }
}