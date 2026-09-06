package fr.doryamy.rpgengine.action.executors;

import fr.doryamy.rpgengine.action.ActionExecutor;
import fr.doryamy.rpgengine.action.assignment.Assignment;
import fr.doryamy.rpgengine.action.assignment.AssignmentExecutor;
import fr.doryamy.rpgengine.action.assignment.AssignmentParser;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.repository.PlayerVariableRepository;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.Objects;
import java.util.UUID;

/**
 * Executor responsable de la modification
 * des variables d'un joueur.
 *
 * Cette classe interprète une expression
 * d'affectation, calcule la nouvelle valeur,
 * puis la sauvegarde dans le dépôt des
 * variables joueur.
 *
 * Cette action correspond au provider
 * "PLAYER".
 */
public final class PlayerVariableActionExecutor
        implements ActionExecutor {

    private final PlayerVariableRepository repository;
    private final AssignmentParser parser;
    private final AssignmentExecutor executor;

    /**
     * Construit l'executor des variables joueur.
     *
     * @param repository dépôt des variables joueur
     * @param parser analyseur des affectations
     * @param executor moteur d'exécution des affectations
     */
    public PlayerVariableActionExecutor(
            PlayerVariableRepository repository,
            AssignmentParser parser,
            AssignmentExecutor executor
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "PlayerVariableRepository ne peut pas être null."
        );

        this.parser = Objects.requireNonNull(
                parser,
                "AssignmentParser ne peut pas être null."
        );

        this.executor = Objects.requireNonNull(
                executor,
                "AssignmentExecutor ne peut pas être null."
        );
    }

    @Override
    public String getProvider() {
        return "PLAYER";
    }

    /**
     * Exécute une modification d'une variable
     * du joueur.
     *
     * Le traitement se déroule en plusieurs étapes :
     *
     * 1. analyse de l'expression ;
     * 2. lecture de la valeur actuelle ;
     * 3. calcul de la nouvelle valeur ;
     * 4. sauvegarde de la nouvelle valeur.
     *
     * @param context contexte d'exécution
     * @param action action à appliquer
     */
    @Override
    public void execute(
            TriggerContext context,
            Action action
    ) {
        Objects.requireNonNull(
                context,
                "TriggerContext ne peut pas être null."
        );

        Objects.requireNonNull(
                action,
                "Action ne peut pas être null."
        );

        Assignment assignment;

        try {
            assignment =
                    parser.parse(
                            action.getExpression()
                    );

        } catch (IllegalArgumentException e) {

            RpgLogger.error(
                    "Action PLAYER invalide : "
                            + action.getExpression()
                            + " | "
                            + e.getMessage()
            );

            return;
        }

        UUID playerUuid =
                context.getPlayer()
                        .getUniqueId();

        String currentValue =
                repository.get(
                        playerUuid,
                        assignment.getKey()
                );

        String newValue;

        try {
            newValue =
                    executor.execute(
                            currentValue,
                            assignment
                    );

        } catch (IllegalArgumentException e) {

            RpgLogger.error(
                    "Impossible d'exécuter l'action PLAYER : "
                            + action.getExpression()
                            + " | "
                            + e.getMessage()
            );

            return;
        }

        repository.set(
                playerUuid,
                assignment.getKey(),
                newValue
        );

        RpgLogger.debug(
                "Variable joueur modifiée : "
                        + assignment.getKey()
                        + "="
                        + newValue
        );
    }
}