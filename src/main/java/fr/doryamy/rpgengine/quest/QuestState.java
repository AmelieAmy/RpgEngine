package fr.doryamy.rpgengine.quest;

/**
 * État abstrait d'une quête pour RPGEngine.
 *
 * <p>Cette enum ne dépend d'aucun système de quêtes
 * particulier. L'implémentation externe est responsable
 * de convertir son propre état vers ces valeurs.
 */
public enum QuestState {

    /**
     * La quête n'a pas encore commencé.
     */
    NOT_STARTED,

    /**
     * La quête est actuellement en cours.
     */
    ACTIVE,

    /**
     * La quête est terminée.
     */
    COMPLETED,

    /**
     * L'état de la quête n'a pas pu être déterminé.
     */
    UNAVAILABLE
}