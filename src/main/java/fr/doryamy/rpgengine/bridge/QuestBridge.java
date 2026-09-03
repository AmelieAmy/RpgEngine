package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.quest.QuestSummary;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bridge spécialisé dans les opérations
 * liées aux systèmes de quêtes exposés
 * par le mod NeoForge.
 *
 * <p>Cette classe ne contient aucune logique
 * de quête RPGEngine. Elle se limite à appeler
 * l'API du mod par réflexion.
 *
 * <p>Le plugin ne conserve ainsi aucune
 * dépendance compile-time vers NeoForge
 * ou FTB Quests.
 */
public final class QuestBridge {

    private Method getQuestStateMethod;
    private Method startQuestMethod;
    private Method getQuestDisplayNameMethod;
    private Method getAvailableQuestsMethod;

    /**
     * Résout les méthodes de l'API NeoForge
     * nécessaires aux opérations de quête.
     *
     * @param bridgeClass classe de l'API bridge du mod
     *
     * @throws NoSuchMethodException si l'API attendue
     *                               n'est pas disponible
     */
    public void initialize(
            Class<?> bridgeClass
    ) throws NoSuchMethodException {

        getQuestStateMethod =
                bridgeClass.getMethod(
                        "getQuestState",
                        UUID.class,
                        String.class
                );

        startQuestMethod =
                bridgeClass.getMethod(
                        "startQuest",
                        UUID.class,
                        String.class
                );

        getQuestDisplayNameMethod =
                bridgeClass.getMethod(
                        "getQuestDisplayName",
                        String.class
                );

        getAvailableQuestsMethod =
                bridgeClass.getMethod(
                        "getAvailableQuests"
                );
    }

    /**
     * Lit l'état d'une quête via le mod NeoForge.
     *
     * @param playerUuid joueur concerné
     * @param questId identifiant externe de la quête
     *
     * @return état retourné par le mod,
     *         ou {@code UNAVAILABLE} si le bridge
     *         n'est pas disponible
     */
    public String getQuestState(
            UUID playerUuid,
            String questId
    ) {
        if (getQuestStateMethod == null) {
            return "UNAVAILABLE";
        }

        try {
            Object result =
                    getQuestStateMethod.invoke(
                            null,
                            playerUuid,
                            questId
                    );

            return result instanceof String state
                    ? state
                    : "UNAVAILABLE";

        } catch (ReflectiveOperationException e) {

            RpgLogger.error(
                    "Impossible de lire l'état de quête via NeoForge : "
                            + e.getMessage()
            );

            return "UNAVAILABLE";
        }
    }

    /**
     * Demande au mod NeoForge de démarrer
     * une quête pour un joueur.
     *
     * @param playerUuid joueur concerné
     * @param questId identifiant externe de la quête
     *
     * @return {@code true} si l'opération a réussi
     */
    public boolean startQuest(
            UUID playerUuid,
            String questId
    ) {
        if (startQuestMethod == null) {
            return false;
        }

        try {
            Object result =
                    startQuestMethod.invoke(
                            null,
                            playerUuid,
                            questId
                    );

            return result instanceof Boolean success
                    && success;

        } catch (ReflectiveOperationException e) {

            RpgLogger.error(
                    "Impossible de démarrer la quête via NeoForge : "
                            + questId
                            + " | "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Retourne les quêtes disponibles via NeoForge.
     */
    public List<QuestSummary> getAvailableQuests() {
        if (getAvailableQuestsMethod == null) {
            return List.of();
        }

        try {
            Object result =
                    getAvailableQuestsMethod.invoke(
                            null
                    );

            if (!(result instanceof List<?> values)) {
                return List.of();
            }

            List<QuestSummary> quests =
                    new ArrayList<>();

            for (Object value :
                    values) {

                if (!(value instanceof Map<?, ?> map)) {
                    continue;
                }

                Object idValue =
                        map.get(
                                "id"
                        );

                Object nameValue =
                        map.get(
                                "name"
                        );

                Object descriptionValue =
                        map.get(
                                "description"
                        );

                if (!(idValue instanceof String id)
                        || !(nameValue instanceof String name)) {

                    continue;
                }

                String description =
                        descriptionValue instanceof String text
                                ? text
                                : "";

                quests.add(
                        new QuestSummary(
                                id,
                                name,
                                description
                        )
                );
            }

            return List.copyOf(
                    quests
            );

        } catch (ReflectiveOperationException e) {

            Throwable cause =
                    e.getCause() != null
                            ? e.getCause()
                            : e;

            RpgLogger.error(
                    "Impossible de charger la liste des quêtes via NeoForge : "
                            + cause.getClass()
                            .getSimpleName()
                            + " | "
                            + cause.getMessage()
            );

            cause.printStackTrace();

            return List.of();
        }
    }

    /**
     * Retourne le nom affichable d'une quête
     * via le mod NeoForge.
     *
     * @param questId identifiant externe de la quête
     *
     * @return nom affichable de la quête,
     *         ou l'identifiant si la résolution échoue
     */
    public String getQuestDisplayName(
            String questId
    ) {
        if (getQuestDisplayNameMethod == null) {
            return questId;
        }

        try {
            Object result =
                    getQuestDisplayNameMethod.invoke(
                            null,
                            questId
                    );

            if (result instanceof String displayName
                    && !displayName.isBlank()) {

                return displayName;
            }

            return questId;

        } catch (ReflectiveOperationException e) {

            RpgLogger.error(
                    "Impossible de résoudre le nom de la quête via NeoForge : "
                            + questId
                            + " | "
                            + e.getMessage()
            );

            return questId;
        }
    }

    /**
     * Libère les références réflexives
     * conservées par ce bridge.
     */
    public void shutdown() {
        getQuestStateMethod =
                null;

        startQuestMethod =
                null;

        getQuestDisplayNameMethod =
                null;

        getAvailableQuestsMethod =
                null;
    }
}