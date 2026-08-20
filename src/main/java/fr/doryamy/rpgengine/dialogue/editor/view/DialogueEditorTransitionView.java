package fr.doryamy.rpgengine.dialogue.editor.view;

import fr.doryamy.rpgengine.dialogue.DialogueTransitionType;

import java.util.List;

/**
 * Projection éditoriale d'une transition.
 *
 * @param key           clé métier
 * @param type          type de transition
 * @param targetNodeKey node cible éventuel
 * @param label         texte d'un choix joueur
 * @param position      ordre de la transition
 * @param conditions    conditions associées
 * @param actions       actions associées
 */
public record DialogueEditorTransitionView(
        String key,
        DialogueTransitionType type,
        String targetNodeKey,
        String label,
        int position,
        List<DialogueEditorConditionView> conditions,
        List<DialogueEditorActionView> actions
) {

    public DialogueEditorTransitionView {
        conditions =
                conditions == null
                        ? List.of()
                        : List.copyOf(
                        conditions
                );

        actions =
                actions == null
                        ? List.of()
                        : List.copyOf(
                        actions
                );
    }
}