package fr.doryamy.rpgengine.quest;

import java.util.List;
import java.util.UUID;

/**
 * Abstraction du système de quêtes utilisé par RPGEngine.
 *
 * <p>Cette interface permet au moteur RPG d'interagir
 * avec un système de quêtes externe sans dépendre
 * directement de son implémentation.
 */
public interface QuestService {

    QuestState getState(
            UUID playerUuid,
            String questId
    );

    boolean start(
            UUID playerUuid,
            String questId
    );

    boolean complete(
            UUID playerUuid,
            String questId
    );

    boolean reactivate(
            UUID playerUuid,
            String questId
    );

    List<QuestSummary> findAll();

    String getDisplayName(
            String questId
    );
}
