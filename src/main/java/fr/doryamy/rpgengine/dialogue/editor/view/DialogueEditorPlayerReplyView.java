package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.List;

/**
 * Projection éditoriale d'une réplique Joueur d'une transition AUTO.
 *
 * @param text       texte de la réplique
 * @param position   position dans la séquence
 * @param conditions conditions facultatives propres à la réplique
 * @param actions    actions facultatives propres à la réplique
 */
public record DialogueEditorPlayerReplyView(
        String text,
        int position,
        List<DialogueEditorConditionView> conditions,
        List<DialogueEditorActionView> actions
) {

    public DialogueEditorPlayerReplyView {
        conditions =
                conditions == null
                        ? List.of()
                        : List.copyOf(conditions);

        actions =
                actions == null
                        ? List.of()
                        : List.copyOf(actions);
    }
}
