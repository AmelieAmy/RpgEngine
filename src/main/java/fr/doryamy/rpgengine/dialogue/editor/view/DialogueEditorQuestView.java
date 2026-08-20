package fr.doryamy.rpgengine.dialogue.editor.view;

/**
 * Représentation éditoriale d'une quête
 * associée au scénario.
 *
 * @param id          identifiant externe de la quête
 * @param displayName nom lisible affiché dans l'éditeur
 */
public record DialogueEditorQuestView(
        String id,
        String displayName
) {
}