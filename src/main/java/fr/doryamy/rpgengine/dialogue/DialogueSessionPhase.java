package fr.doryamy.rpgengine.dialogue;

/**
 * Phase de présentation d'une session de dialogue.
 */
public enum DialogueSessionPhase {

    /**
     * Le node PNJ courant est affiché.
     */
    NODE,

    /**
     * Une réplique Joueur d'une transition AUTO est affichée.
     */
    AUTO_PLAYER_REPLY
}
