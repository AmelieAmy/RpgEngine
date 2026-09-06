package fr.doryamy.rpgengine.dialogue;

/**
 * Interaction actuellement attendue
 * pour une session de dialogue.
 */
public enum DialogueSessionPhase {

    /**
     * Le moteur progresse dans le graphe.
     * Aucune interaction client ne doit être acceptée.
     */
    PROCESSING,

    /**
     * Une réplique est affichée.
     * Le joueur doit demander à continuer.
     */
    WAITING_CONTINUE,

    /**
     * Un embranchement est affiché.
     * Le joueur doit sélectionner l'un
     * des choix effectivement présentés.
     */
    WAITING_CHOICE
}