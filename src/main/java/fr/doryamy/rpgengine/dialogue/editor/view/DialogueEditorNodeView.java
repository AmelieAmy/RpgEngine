package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.List;

/** Projection d'un node de dialogue dans l'éditeur. */
public record DialogueEditorNodeView(
        String key,
        String text,
        String kind,
        List<DialogueEditorTransitionView> transitions
) {
    public DialogueEditorNodeView {
        kind = kind == null || kind.isBlank() ? "NPC" : kind;
        transitions = transitions == null ? List.of() : List.copyOf(transitions);
    }
}
