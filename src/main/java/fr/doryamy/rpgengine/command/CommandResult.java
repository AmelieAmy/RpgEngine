package fr.doryamy.rpgengine.command;

import java.util.Objects;

/**
 * Représente le résultat d'une opération
 * déclenchée depuis le système d'administration.
 *
 * Un résultat indique :
 * - si l'opération a réussi ;
 * - le message métier associé.
 *
 * Les erreurs techniques restent journalisées
 * séparément via RpgLogger.
 */
public final class CommandResult {

    private final boolean success;
    private final String message;

    private CommandResult(
            boolean success,
            String message
    ) {
        this.success = success;
        this.message = Objects.requireNonNull(message);
    }

    /**
     * Crée un résultat de succès.
     *
     * @param message message associé
     * @return résultat réussi
     */
    public static CommandResult success(
            String message
    ) {
        return new CommandResult(
                true,
                message
        );
    }

    /**
     * Crée un résultat d'échec.
     *
     * @param message raison de l'échec
     * @return résultat échoué
     */
    public static CommandResult failure(
            String message
    ) {
        return new CommandResult(
                false,
                message
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}