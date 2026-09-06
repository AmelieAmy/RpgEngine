package fr.doryamy.rpgengine.dialogue;

/**
 * Élément appartenant au graphe d'un dialogue.
 *
 * <p>Les implémentations autorisées représentent
 * explicitement les différents concepts du dialogue.
 */
public sealed interface DialogueElement
        permits DialogueStart,
        DialogueReply,
        DialogueBranch,
        DialogueChoice,
        DialogueEnd {

    /**
     * Retourne l'identité de l'élément.
     *
     * @return clé métier de l'élément
     */
    DialogueElementKey key();
}