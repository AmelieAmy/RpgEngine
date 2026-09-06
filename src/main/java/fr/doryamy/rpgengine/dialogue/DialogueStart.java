package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Point de départ système d'un dialogue.
 *
 * <p>Un graphe possède exactement un Start.
 * Il n'est jamais manipulé directement
 * par le créateur.
 */
public record DialogueStart(
        DialogueElementKey key
) implements DialogueElement {

    public DialogueStart {
        Objects.requireNonNull(
                key,
                "La clé du Start ne peut pas être null."
        );
    }
}