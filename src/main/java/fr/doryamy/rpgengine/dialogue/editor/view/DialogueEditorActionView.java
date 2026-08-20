package fr.doryamy.rpgengine.dialogue.editor.view;

/**
 * Projection éditoriale d'une action.
 *
 * @param provider    provider utilisé
 * @param expression  expression configurée
 * @param position    ordre d'exécution
 * @param displayText libellé lisible destiné à l'éditeur
 */
public record DialogueEditorActionView(
        String provider,
        String expression,
        int position,
        String displayText
) {
}