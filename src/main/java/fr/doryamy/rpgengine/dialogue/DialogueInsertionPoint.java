package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Point d'insertion situé sur une liaison existante
 * du graphe.
 */
public record DialogueInsertionPoint(
        DialogueElementKey source,
        DialogueElementKey target
) {

    public DialogueInsertionPoint {

        Objects.requireNonNull(
                source,
                "La source du point d'insertion ne peut pas être null."
        );

        Objects.requireNonNull(
                target,
                "La cible du point d'insertion ne peut pas être null."
        );
    }

    /**
     * Retourne la liaison correspondant
     * à ce point d'insertion.
     */
    public DialogueLink asLink() {
        return new DialogueLink(
                source,
                target
        );
    }
}