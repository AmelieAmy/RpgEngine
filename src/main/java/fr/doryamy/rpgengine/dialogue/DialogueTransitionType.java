package fr.doryamy.rpgengine.dialogue;

/**
 * Définit la nature d'une transition entre deux nodes de dialogue.
 */
public enum DialogueTransitionType {

    /**
     * Transition automatique vers un autre node.
     */
    AUTO,

    /**
     * Transition proposée explicitement au joueur sous forme de choix.
     */
    CHOICE,

    /**
     * Transition terminant explicitement le dialogue.
     */
    END
}