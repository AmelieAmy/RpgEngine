package fr.doryamy.rpgengine.dialogue.presentation;

/**
 * Type d'interaction actuellement attendu du joueur.
 *
 * <p>Ce type décrit le comportement de présentation et non
 * directement le type de transition du Dialogue Engine.
 */
public enum DialogueInteractionType {

    /**
     * Le joueur peut poursuivre le dialogue.
     */
    CONTINUE,

    /**
     * Le joueur doit sélectionner un choix disponible.
     */
    CHOICE,

    /**
     * Le joueur peut confirmer la fin du dialogue.
     */
    CLOSE
}