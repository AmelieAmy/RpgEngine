package fr.doryamy.rpgengine.dialogue;

/**
 * Signale qu'un dialogue demandé
 * n'existe pas.
 */
public final class DialogueNotFoundException
        extends RuntimeException {

    public DialogueNotFoundException(
            DialogueKey key
    ) {
        super(
                "Dialogue introuvable : "
                        + key
        );
    }
}