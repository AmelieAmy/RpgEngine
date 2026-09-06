package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.quest.QuestSummary;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Bridge réflexif spécialisé dans l'intégration des quêtes. */
public final class QuestBridge {

    private Method getQuestStateMethod;
    private Method startQuestMethod;
    private Method completeQuestMethod;
    private Method reactivateQuestMethod;
    private Method getQuestDisplayNameMethod;
    private Method getAvailableQuestsMethod;

    public void initialize(Class<?> bridgeClass) throws NoSuchMethodException {
        getQuestStateMethod = bridgeClass.getMethod("getQuestState", UUID.class, String.class);
        startQuestMethod = bridgeClass.getMethod("startQuest", UUID.class, String.class);
        completeQuestMethod = bridgeClass.getMethod("completeQuest", UUID.class, String.class);
        reactivateQuestMethod = bridgeClass.getMethod("reactivateQuest", UUID.class, String.class);
        getQuestDisplayNameMethod = bridgeClass.getMethod("getQuestDisplayName", String.class);
        getAvailableQuestsMethod = bridgeClass.getMethod("getAvailableQuests");
    }

    public String getQuestState(UUID playerUuid, String questId) {
        if (getQuestStateMethod == null) return "UNAVAILABLE";
        try {
            Object result = getQuestStateMethod.invoke(null, playerUuid, questId);
            return result instanceof String state ? state : "UNAVAILABLE";
        } catch (ReflectiveOperationException e) {
            RpgLogger.error("Impossible de lire l'état de quête via NeoForge : " + e.getMessage());
            return "UNAVAILABLE";
        }
    }

    public boolean startQuest(UUID playerUuid, String questId) {
        return invokeMutation(startQuestMethod, playerUuid, questId, "démarrer");
    }

    public boolean completeQuest(UUID playerUuid, String questId) {
        return invokeMutation(completeQuestMethod, playerUuid, questId, "terminer");
    }

    public boolean reactivateQuest(UUID playerUuid, String questId) {
        return invokeMutation(reactivateQuestMethod, playerUuid, questId, "réactiver");
    }

    private boolean invokeMutation(
            Method method,
            UUID playerUuid,
            String questId,
            String operation
    ) {
        if (method == null) return false;
        try {
            Object result = method.invoke(null, playerUuid, questId);
            return result instanceof Boolean success && success;
        } catch (ReflectiveOperationException e) {
            RpgLogger.error("Impossible de " + operation + " la quête via NeoForge : "
                    + questId + " | " + e.getMessage());
            return false;
        }
    }

    public List<QuestSummary> getAvailableQuests() {
        if (getAvailableQuestsMethod == null) return List.of();
        try {
            Object result = getAvailableQuestsMethod.invoke(null);
            if (!(result instanceof List<?> values)) return List.of();
            List<QuestSummary> quests = new ArrayList<>();
            for (Object value : values) {
                if (!(value instanceof Map<?, ?> map)) continue;
                Object idValue = map.get("id");
                Object nameValue = map.get("name");
                Object descriptionValue = map.get("description");
                if (!(idValue instanceof String id) || !(nameValue instanceof String name)) continue;
                String description = descriptionValue instanceof String text ? text : "";
                quests.add(new QuestSummary(id, name, description));
            }
            return List.copyOf(quests);
        } catch (ReflectiveOperationException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            RpgLogger.error("Impossible de charger la liste des quêtes via NeoForge : "
                    + cause.getClass().getSimpleName() + " | " + cause.getMessage());
            return List.of();
        }
    }

    public String getQuestDisplayName(String questId) {
        if (getQuestDisplayNameMethod == null) return questId;
        try {
            Object result = getQuestDisplayNameMethod.invoke(null, questId);
            return result instanceof String displayName && !displayName.isBlank()
                    ? displayName : questId;
        } catch (ReflectiveOperationException e) {
            RpgLogger.error("Impossible de résoudre le nom de la quête via NeoForge : "
                    + questId + " | " + e.getMessage());
            return questId;
        }
    }

    public void shutdown() {
        getQuestStateMethod = null;
        startQuestMethod = null;
        completeQuestMethod = null;
        reactivateQuestMethod = null;
        getQuestDisplayNameMethod = null;
        getAvailableQuestsMethod = null;
    }
}
