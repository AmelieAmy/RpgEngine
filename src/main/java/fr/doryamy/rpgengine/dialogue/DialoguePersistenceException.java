package fr.doryamy.rpgengine.dialogue;

/**
 * Signale une défaillance de l'infrastructure
 * de persistance des dialogues.
 *
 * <p>Cette exception ne signifie jamais
 * qu'un dialogue est simplement absent.
 */
public final class DialoguePersistenceException
        extends RuntimeException {

    public DialoguePersistenceException(
            String message
    ) {
        super(message);
    }

    public DialoguePersistenceException(
            String message,
            Throwable cause
    ) {
        super(
                message,
                cause
        );
    }
}