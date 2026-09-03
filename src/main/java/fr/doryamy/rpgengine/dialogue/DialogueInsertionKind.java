package fr.doryamy.rpgengine.dialogue;

/** Position sémantique d'une insertion dans une transition de dialogue. */
public enum DialogueInsertionKind {
    AFTER_SOURCE,
    AFTER_CHOICE,
    AFTER_PLAYER_REPLY,
    BRANCH_START,
    BRANCH_EXPAND
}
