package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Fin système d'une branche de dialogue.
 *
 * <p>Une fin est maintenue automatiquement
 * par le moteur et n'est jamais manipulée
 * directement par le créateur.
 */
public record DialogueEnd(
        DialogueElementKey key
) implements DialogueElement {

    public DialogueEnd {
        Objects.requireNonNull(
                key,
                "La clé de la fin ne peut pas être null."
        );
    }
}