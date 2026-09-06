package fr.doryamy.rpgengine.dialogue.editor.view;

/**
 * Projection d'un élément du graphe destinée
 * à l'interface d'administration.
 *
 * <p>Chaque implémentation représente explicitement
 * un concept du domaine Dialogue.
 */
public sealed interface DialogueEditorElementView
        permits DialogueEditorStartView,
        DialogueEditorReplyView,
        DialogueEditorBranchView,
        DialogueEditorChoiceView,
        DialogueEditorEndView {

    /**
     * Identité métier opaque de l'élément.
     */
    String key();
}