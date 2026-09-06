package fr.doryamy.rpgengine.quest.ftb;

import fr.doryamy.rpgengine.bridge.NeoForgeBridge;
import fr.doryamy.rpgengine.quest.QuestService;
import fr.doryamy.rpgengine.quest.QuestState;
import fr.doryamy.rpgengine.quest.QuestSummary;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implémentation de QuestService utilisant
 * l'intégration FTB Quests exposée par le bridge NeoForge.
 *
 * <p>Cette classe ne dépend pas directement
 * des classes de FTB Quests.
 */
public final class FtbQuestService
        implements QuestService {

    private final NeoForgeBridge neoForgeBridge;

    public FtbQuestService(
            NeoForgeBridge neoForgeBridge
    ) {
        this.neoForgeBridge = Objects.requireNonNull(
                neoForgeBridge,
                "NeoForgeBridge ne peut pas être null."
        );
    }

    @Override
    public QuestState getState(
            UUID playerUuid,
            String questId
    ) {
        requirePlayer(playerUuid);
        requireQuestId(questId);

        String state = neoForgeBridge.getQuestState(
                playerUuid,
                questId
        );

        if (state == null || state.isBlank()) {
            RpgLogger.error(
                    "État de quête indisponible pour "
                            + questId
                            + " | joueur="
                            + playerUuid
            );
            return QuestState.UNAVAILABLE;
        }

        try {
            return QuestState.valueOf(state);
        } catch (IllegalArgumentException e) {
            RpgLogger.error(
                    "État de quête inconnu reçu depuis NeoForge : "
                            + state
                            + " | quête="
                            + questId
                            + " | joueur="
                            + playerUuid
            );
            return QuestState.UNAVAILABLE;
        }
    }

    @Override
    public boolean start(
            UUID playerUuid,
            String questId
    ) {
        requirePlayer(playerUuid);
        requireQuestId(questId);
        return neoForgeBridge.startQuest(playerUuid, questId);
    }

    @Override
    public boolean complete(
            UUID playerUuid,
            String questId
    ) {
        requirePlayer(playerUuid);
        requireQuestId(questId);
        return neoForgeBridge.completeQuest(playerUuid, questId);
    }

    @Override
    public boolean reactivate(
            UUID playerUuid,
            String questId
    ) {
        requirePlayer(playerUuid);
        requireQuestId(questId);
        return neoForgeBridge.reactivateQuest(playerUuid, questId);
    }

    @Override
    public List<QuestSummary> findAll() {
        return neoForgeBridge.getAvailableQuests();
    }

    @Override
    public String getDisplayName(
            String questId
    ) {
        requireQuestId(questId);
        return neoForgeBridge.getQuestDisplayName(questId);
    }

    private static void requirePlayer(UUID playerUuid) {
        Objects.requireNonNull(
                playerUuid,
                "UUID joueur ne peut pas être null."
        );
    }

    private static void requireQuestId(String questId) {
        if (questId == null || questId.isBlank()) {
            throw new IllegalArgumentException(
                    "L'identifiant d'une quête ne peut pas être vide."
            );
        }
    }
}
