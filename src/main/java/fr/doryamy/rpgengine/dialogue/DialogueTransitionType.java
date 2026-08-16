package fr.doryamy.rpgengine.dialogue;

/**
 * Définit la nature d'une transition entre deux nodes de dialogue.
 */
public enum DialogueTransitionType {

    /**
     * Transition sans décision narrative.
     * Son déclenchement peut dépendre de la présentation :
     * une confirmation CONTINUE du joueur peut être attendue,
     * ou la transition peut être automatique.
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