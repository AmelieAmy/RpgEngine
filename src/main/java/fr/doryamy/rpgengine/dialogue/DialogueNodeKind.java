package fr.doryamy.rpgengine.dialogue;

/**
 * Nature structurelle d'un node du graphe de dialogue.
 */
public enum DialogueNodeKind {
    /** Réplique narrative affichée comme PNJ. */
    NPC,

    /** Node technique invisible utilisé pour structurer un embranchement ou une continuation. */
    STRUCTURAL
}
