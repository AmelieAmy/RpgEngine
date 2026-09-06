package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Embranchement d'un dialogue.
 *
 * <p>Un embranchement constitue un point de séparation
 * du graphe vers plusieurs DialogueChoice.
 *
 * <p>Il ne possède lui-même ni texte, ni Condition,
 * ni Action.
 */
public record DialogueBranch(
        DialogueElementKey key
) implements DialogueElement {

    public DialogueBranch {
        Objects.requireNonNull(
                key,
                "La clé de l'embranchement ne peut pas être null."
        );
    }
}