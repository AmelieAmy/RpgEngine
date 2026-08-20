package fr.doryamy.rpgengine.dialogue.editor.view;

import fr.doryamy.rpgengine.quest.QuestState;

import java.util.List;

/**
 * Projection résumée d'un scénario destinée
 * à l'écran principal de gestion des dialogues.
 *
 * <p>Un scénario est une notion d'interface :
 * il agrège les dialogues associés au même
 * déclencheur et, lorsqu'elle existe, à la même quête.
 *
 * <p>Cette projection n'est jamais persistée.
 *
 * @param title              nom lisible du scénario
 * @param trigger            déclencheur associé
 * @param quest              quête associée, ou {@code null}
 * @param availableStates    états actuellement configurés
 * @param defaultDialogueKey dialogue utilisé pour ouvrir l'éditeur
 */
public record DialogueEditorScenarioSummaryView(
        String title,
        DialogueEditorTriggerView trigger,
        DialogueEditorQuestView quest,
        List<QuestState> availableStates,
        String defaultDialogueKey
) {

    public DialogueEditorScenarioSummaryView {
        availableStates =
                availableStates == null
                        ? List.of()
                        : List.copyOf(availableStates);
    }
}