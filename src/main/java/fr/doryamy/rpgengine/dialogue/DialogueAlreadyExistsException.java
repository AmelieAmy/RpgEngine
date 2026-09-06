package fr.doryamy.rpgengine.dialogue;

/**
 * Signale qu'une création tente d'utiliser
 * une identité déjà persistée.
 */
public final class DialogueAlreadyExistsException
        extends RuntimeException {

    public DialogueAlreadyExistsException(
            DialogueKey key
    ) {
        super(
                "Un dialogue existe déjà avec la clé : "
                        + key
        );
    }
}