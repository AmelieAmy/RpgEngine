package fr.doryamy.rpgengine.dialogue.editor.view;

/**
 * Projection éditoriale d'une condition.
 *
 * @param provider    provider utilisé
 * @param expression  expression configurée
 * @param displayText libellé lisible destiné à l'éditeur
 */
public record DialogueEditorConditionView(
        String provider,
        String expression,
        String displayText
) {
}