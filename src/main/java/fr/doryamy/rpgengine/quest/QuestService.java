package fr.doryamy.rpgengine.quest;

import java.util.UUID;

/**
 * Contrat d'accès au système de quêtes utilisé
 * par RPGEngine.
 *
 * <p>Le moteur RPGEngine reste indépendant
 * de l'implémentation réelle du système de quêtes.
 */
public interface QuestService {

    /**
     * Retourne l'état courant d'une quête
     * pour un joueur.
     *
     * @param playerUuid joueur concerné
     * @param questId identifiant externe de la quête
     *
     * @return état courant de la quête
     */
    QuestState getState(
            UUID playerUuid,
            String questId
    );

    /**
     * Demande le démarrage d'une quête
     * pour un joueur.
     *
     * @param playerUuid joueur concerné
     * @param questId identifiant externe de la quête
     *
     * @return true si l'opération a réussi
     */
    boolean start(
            UUID playerUuid,
            String questId
    );

    /**
     * Retourne le nom affichable d'une quête externe.
     *
     * @param questId identifiant FTB Quests
     *
     * @return nom affichable, ou l'identifiant
     *         si la quête ne peut pas être résolue
     */
    String getDisplayName(
            String questId
    );
}