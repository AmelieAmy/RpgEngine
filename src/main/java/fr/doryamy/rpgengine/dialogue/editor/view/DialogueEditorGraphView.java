package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.List;

/**
 * Projection d'un graphe de dialogue
 * destinée à l'éditeur visuel.
 *
 * @param dialogueKey  clé métier du dialogue
 * @param dialogueName nom lisible
 * @param startNodeKey node de départ
 * @param nodes        nodes du graphe
 */
public record DialogueEditorGraphView(
        String dialogueKey,
        String dialogueName,
        String startNodeKey,
        List<DialogueEditorNodeView> nodes
) {

    public DialogueEditorGraphView {
        nodes =
                nodes == null
                        ? List.of()
                        : List.copyOf(
                        nodes
                );
    }
}