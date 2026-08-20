package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.List;

/**
 * Projection d'un node de dialogue.
 *
 * <p>Dans l'éditeur V1, un node est présenté
 * graphiquement comme une réplique PNJ.
 *
 * @param key         clé métier du node
 * @param text        texte narratif
 * @param transitions transitions sortantes
 */
public record DialogueEditorNodeView(
        String key,
        String text,
        List<DialogueEditorTransitionView> transitions
) {

    public DialogueEditorNodeView {
        transitions =
                transitions == null
                        ? List.of()
                        : List.copyOf(
                        transitions
                );
    }
}