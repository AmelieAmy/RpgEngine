package fr.doryamy.rpgengine.dialogue.editor.view;

import fr.doryamy.rpgengine.quest.QuestState;

import java.util.List;

/**
 * Projection complète d'un scénario destinée
 * à l'éditeur visuel de dialogues.
 *
 * <p>Cette vue agrège des informations provenant
 * du Dialogue Engine, du Trigger Engine et,
 * éventuellement, du système de quêtes.
 *
 * <p>Elle n'est jamais persistée directement.
 */
public record DialogueEditorView(
        String title,
        DialogueEditorTriggerView trigger,
        DialogueEditorQuestView quest,
        QuestState selectedState,
        List<QuestState> availableStates,
        DialogueEditorGraphView graph
) {

    public DialogueEditorView {
        availableStates =
                availableStates == null
                        ? List.of()
                        : List.copyOf(
                        availableStates
                );
    }
}