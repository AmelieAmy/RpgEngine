package fr.doryamy.rpgengine.quest.ftb;

import fr.doryamy.rpgengine.bridge.NeoForgeBridge;
import fr.doryamy.rpgengine.quest.QuestService;
import fr.doryamy.rpgengine.quest.QuestSummary;
import fr.doryamy.rpgengine.quest.QuestState;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.List;
import java.util.UUID;

/**
 * Implémentation de {@link QuestService} utilisant
 * FTB Quests via le bridge NeoForge.
 *
 * <p>Cette classe ne dépend directement d'aucune
 * classe FTB Quests ou NeoForge.
 */
public final class FtbQuestService
        implements QuestService {

    private final NeoForgeBridge bridge;

    public FtbQuestService(
            NeoForgeBridge bridge
    ) {
        this.bridge = bridge;
    }

    @Override
    public QuestState getState(
            UUID playerUuid,
            String questId
    ) {
        String state =
                bridge.getQuestState(
                        playerUuid,
                        questId
                );

        try {
            return QuestState.valueOf(
                    state
            );

        } catch (IllegalArgumentException e) {

            RpgLogger.error(
                    "État FTB Quests inconnu : "
                            + state
                            + " | quête="
                            + questId
            );

            return QuestState.UNAVAILABLE;
        }
    }

    @Override
    public boolean start(
            UUID playerUuid,
            String questId
    ) {
        return bridge.startQuest(
                playerUuid,
                questId
        );
    }

    @Override
    public List<QuestSummary> findAll() {
        return bridge.getAvailableQuests();
    }

    @Override
    public String getDisplayName(
            String questId
    ) {
        return bridge.getQuestDisplayName(
                questId
        );
    }
}