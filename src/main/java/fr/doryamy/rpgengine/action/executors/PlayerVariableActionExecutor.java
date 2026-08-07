package fr.doryamy.rpgengine.action.executors;

import fr.doryamy.rpgengine.action.ActionExecutor;
import fr.doryamy.rpgengine.action.assignment.Assignment;
import fr.doryamy.rpgengine.action.assignment.AssignmentExecutor;
import fr.doryamy.rpgengine.action.assignment.AssignmentParser;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.repository.PlayerVariableRepository;
import fr.doryamy.rpgengine.util.RpgLogger;
import org.bukkit.entity.Player;

/**
 * Executor responsable de la modification
 * des variables d'un joueur.
 * <p>
 * Cette classe interprète une expression
 * d'affectation, calcule la nouvelle valeur,
 * puis la sauvegarde dans le dépôt des
 * variables joueur.
 * <p>
 * Cette action correspond au provider
 * "PLAYER".
 */
public final class PlayerVariableActionExecutor implements ActionExecutor {

    private final PlayerVariableRepository repository;
    private final AssignmentParser parser;
    private final AssignmentExecutor executor;

    /**
     * Construit un executor utilisant
     * le dépôt des variables joueur.
     *
     * @param repository dépôt des variables
     */
    public PlayerVariableActionExecutor(
            PlayerVariableRepository repository
    ) {
        this.repository = repository;
        this.parser = new AssignmentParser();
        this.executor = new AssignmentExecutor();
    }

    @Override
    public String getProvider() {
        return "PLAYER";
    }

    /**
     * Exécute une modification d'une variable
     * du joueur.
     * <p>
     * Le traitement se déroule en plusieurs étapes :
     * <p>
     * 1. analyse de l'expression ;
     * 2. lecture de la valeur actuelle ;
     * 3. calcul de la nouvelle valeur ;
     * 4. sauvegarde de la nouvelle valeur.
     *
     * @param context contexte d'exécution
     * @param action  action à appliquer
     */
    @Override
    public void execute(
            TriggerContext context,
            Action action
    ) {
        Assignment assignment;
        Player player = context.getPlayer();
        String playerUuid = player.getUniqueId().toString();

        try {
            assignment = parser.parse(action.getExpression());
        } catch (IllegalArgumentException e) {
            RpgLogger.error(
                    "Action PLAYER invalide : "
                            + action.getExpression()
                            + " | "
                            + e.getMessage()
            );
            return;
        }

        String currentValue = repository.get(playerUuid, assignment.getKey());
        String newValue;

        try {
            newValue = executor.execute(currentValue, assignment);
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