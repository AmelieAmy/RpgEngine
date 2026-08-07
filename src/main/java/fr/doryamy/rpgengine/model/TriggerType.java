package fr.doryamy.rpgengine.model;

/**
 * Représente les catégories de déclencheurs supportées par RPGEngine.
 *
 * Chaque valeur correspond à un type d'événement pouvant être converti
 * en TriggerContext par un listener ou une intégration externe.
 */
public enum TriggerType {

    /**
     * Déclenché lorsqu'un joueur interagit avec un PNJ.
     */
    NPC,

    /**
     * Déclenché lorsqu'un joueur interagit avec un bloc du monde.
     */
    BLOCK,

    /**
     * Déclenché lorsqu'un joueur interagit avec une entité vivante.
     */
    ENTITY,

    /**
     * Déclenché lorsqu'un joueur entre, sort ou interagit avec une région.
     */
    REGION,

    /**
     * Déclenché lorsqu'une commande est exécutée.
     */
    COMMAND
}