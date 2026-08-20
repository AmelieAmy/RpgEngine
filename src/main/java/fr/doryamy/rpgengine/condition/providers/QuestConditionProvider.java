package fr.doryamy.rpgengine.condition.providers;

import fr.doryamy.rpgengine.condition.ConditionProvider;
import fr.doryamy.rpgengine.quest.QuestService;
import fr.doryamy.rpgengine.quest.QuestState;
import fr.doryamy.rpgengine.trigger.TriggerContext;

/**
 * Fournit l'état courant d'une quête
 * au système de conditions RPGEngine.
 *
 * <p>La clé de l'expression correspond
 * à l'identifiant externe de la quête.
 */
public final class QuestConditionProvider
        implements ConditionProvider {

    private final QuestService questService;

    public QuestConditionProvider(
            QuestService questService
    ) {
        this.questService =
                questService;
    }

    @Override
    public String getProvider() {
        return "QUEST";
    }

    @Override
    public String resolve(
            TriggerContext context,
            String key
    ) {
        QuestState state =
                questService.getState(
                        context.getPlayer()
                                .getUniqueId(),
                        key
                );

        return state.name();
    }
}