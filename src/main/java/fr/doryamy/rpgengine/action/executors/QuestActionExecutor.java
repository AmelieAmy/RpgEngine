package fr.doryamy.rpgengine.action.executors;

import fr.doryamy.rpgengine.action.ActionExecutor;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.quest.QuestActionOperation;
import fr.doryamy.rpgengine.quest.QuestService;
import fr.doryamy.rpgengine.quest.QuestState;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.util.RpgLogger;

/**
 * Executor responsable des actions QUEST.
 *
 * <p>Expressions supportées :
 * <pre>
 * START:&lt;questId&gt;
 * COMPLETE:&lt;questId&gt;
 * REACTIVATE:&lt;questId&gt;
 * </pre>
 */
public final class QuestActionExecutor
        implements ActionExecutor {

    private final QuestService questService;

    public QuestActionExecutor(
            QuestService questService
    ) {
        this.questService = questService;
    }

    @Override
    public String getProvider() {
        return "QUEST";
    }

    @Override
    public void execute(
            TriggerContext context,
            Action action
    ) {
        String expression = action.getExpression();

        if (expression == null || expression.isBlank()) {
            RpgLogger.error("Expression QUEST vide.");
            return;
        }

        String[] parts = expression.split(":", 2);
        if (parts.length != 2 || parts[1].isBlank()) {
            RpgLogger.error(
                    "Expression QUEST invalide : " + expression
            );
            return;
        }

        final QuestActionOperation operation;
        try {
            operation = QuestActionOperation.valueOf(
                    parts[0].trim().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            RpgLogger.error(
                    "Opération QUEST inconnue : " + parts[0]
            );
            return;
        }

        String questId = parts[1].trim();
        QuestState currentState = questService.getState(
                context.getPlayer().getUniqueId(),
                questId
        );

        if (currentState != operation.sourceState()) {
            RpgLogger.error(
                    "Transition QUEST refusée : "
                            + questId
                            + " | état="
                            + currentState
                            + " | opération="
                            + operation
                            + " | état attendu="
                            + operation.sourceState()
            );
            return;
        }

        boolean success = switch (operation) {
            case START -> questService.start(
                    context.getPlayer().getUniqueId(),
                    questId
            );
            case COMPLETE -> questService.complete(
                    context.getPlayer().getUniqueId(),
                    questId
            );
            case REACTIVATE -> questService.reactivate(
                    context.getPlayer().getUniqueId(),
                    questId
            );
        };

        if (!success) {
            RpgLogger.error(
                    "Échec de l'opération QUEST "
                            + operation
                            + " pour la quête "
                            + questId
            );
        }
    }
}
