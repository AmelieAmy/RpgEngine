package fr.doryamy.rpgengine.action.executors;

import fr.doryamy.rpgengine.action.ActionExecutor;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.quest.QuestService;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.util.RpgLogger;

/**
 * Executor responsable des actions liées
 * au système de quêtes.
 *
 * <p>Cette action correspond au provider
 * {@code QUEST}.
 *
 * <p>L'expression suit actuellement la syntaxe :
 * <pre>
 * START:&lt;questId&gt;
 * </pre>
 *
 * <p>Exemple :
 * <pre>
 * START:5ADADA0BEE4C4B2C
 * </pre>
 */
public final class QuestActionExecutor
        implements ActionExecutor {

    private static final String OPERATION_START =
            "START";

    private final QuestService questService;

    /**
     * Crée l'executor des actions de quête.
     *
     * @param questService service d'accès
     *                     au système de quêtes
     */
    public QuestActionExecutor(
            QuestService questService
    ) {
        this.questService =
                questService;
    }

    @Override
    public String getProvider() {
        return "QUEST";
    }

    /**
     * Exécute l'opération de quête décrite
     * par l'expression de l'action.
     *
     * @param context contexte d'exécution
     * @param action action à exécuter
     */
    @Override
    public void execute(
            TriggerContext context,
            Action action
    ) {
        String expression =
                action.getExpression();

        if (expression == null
                || expression.isBlank()) {

            RpgLogger.error(
                    "Expression QUEST vide."
            );

            return;
        }

        String[] parts =
                expression.split(
                        ":",
                        2
                );

        if (parts.length != 2) {

            RpgLogger.error(
                    "Expression QUEST invalide : "
                            + expression
            );

            return;
        }

        String operation =
                parts[0].trim();

        String questId =
                parts[1].trim();

        if (questId.isEmpty()) {

            RpgLogger.error(
                    "ID de quête manquant dans l'expression : "
                            + expression
            );

            return;
        }

        if (OPERATION_START.equalsIgnoreCase(
                operation
        )) {

            startQuest(
                    context,
                    questId
            );

            return;
        }

        RpgLogger.error(
                "Opération QUEST inconnue : "
                        + operation
        );
    }

    /**
     * Demande le démarrage d'une quête
     * pour le joueur du contexte.
     */
    private void startQuest(
            TriggerContext context,
            String questId
    ) {
        boolean success =
                questService.start(
                        context.getPlayer()
                                .getUniqueId(),
                        questId
                );

        if (!success) {

            RpgLogger.error(
                    "Impossible de démarrer la quête : "
                            + questId
                            + " | joueur="
                            + context.getPlayer()
                            .getName()
            );
        }
    }
}