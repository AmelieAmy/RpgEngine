package fr.doryamy.rpgengine.dialogue.editor.view;

import fr.doryamy.rpgengine.dialogue.DialogueTransitionType;

import java.util.List;

/**
 * Projection éditoriale d'une transition.
 *
 * @param key           clé métier
 * @param type          type de transition
 * @param targetNodeKey node cible éventuel
 * @param label         texte d'un choix joueur, uniquement pour CHOICE
 * @param position      ordre de la transition
 * @param terminal      true si la transition termine le dialogue
 * @param conditions    conditions associées à la transition
 * @param actions       actions associées à la transition
 * @param playerReplies répliques Joueur séquentielles d'une transition AUTO
 */
public record DialogueEditorTransitionView(
        String key,
        DialogueTransitionType type,
        String targetNodeKey,
        String label,
        int position,
        boolean terminal,
        List<DialogueEditorConditionView> conditions,
        List<DialogueEditorActionView> actions,
        List<DialogueEditorPlayerReplyView> playerReplies
) {

    public DialogueEditorTransitionView {
        conditions =
                conditions == null
                        ? List.of()
                        : List.copyOf(conditions);

        actions =
                actions == null
                        ? List.of()
                        : List.copyOf(actions);

        playerReplies =
                playerReplies == null
                        ? List.of()
                        : List.copyOf(playerReplies);
    }
}
