package fr.doryamy.rpgengine.bridge.mapping;

import fr.doryamy.rpgengine.dialogue.editor.view.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Convertit les projections de l'éditeur
 * en structures Java standards destinées
 * au bridge NeoForge.
 *
 * <p>Le plugin ne dépend jamais des DTO du mod.
 * Les données traversent la frontière uniquement
 * sous forme de String, Number, List et Map.
 */
public final class DialogueEditorBridgeMapper {

    public Map<String, Object> toEditorMap(
            DialogueEditorView view
    ) {
        Map<String, Object> result =
                new HashMap<>();

        result.put("title", view.title());
        result.put("trigger", toTriggerMap(view.trigger()));
        result.put(
                "quest",
                view.quest() == null
                        ? null
                        : toQuestMap(view.quest())
        );
        result.put(
                "selectedState",
                view.selectedState() == null
                        ? null
                        : view.selectedState().name()
        );
        result.put(
                "availableStates",
                view.availableStates()
                        .stream()
                        .map(Enum::name)
                        .toList()
        );
        result.put("graph", toGraphMap(view.graph()));

        return result;
    }

    public Map<String, Object> toScenarioSummaryMap(
            DialogueEditorScenarioSummaryView scenario
    ) {
        Map<String, Object> result =
                new HashMap<>();

        result.put("title", scenario.title());
        result.put("trigger", toTriggerMap(scenario.trigger()));
        result.put(
                "quest",
                scenario.quest() == null
                        ? null
                        : toQuestMap(scenario.quest())
        );
        result.put(
                "availableStates",
                scenario.availableStates()
                        .stream()
                        .map(Enum::name)
                        .toList()
        );
        result.put("defaultDialogueKey", scenario.defaultDialogueKey());

        return result;
    }

    private Map<String, Object> toTriggerMap(
            DialogueEditorTriggerView trigger
    ) {
        Map<String, Object> result =
                new HashMap<>();

        result.put("type", trigger.type());
        result.put("targetId", trigger.targetId());
        result.put("displayName", trigger.displayName());

        return result;
    }

    private Map<String, Object> toQuestMap(
            DialogueEditorQuestView quest
    ) {
        Map<String, Object> result =
                new HashMap<>();

        result.put("id", quest.id());
        result.put("displayName", quest.displayName());

        return result;
    }

    private Map<String, Object> toGraphMap(
            DialogueEditorGraphView graph
    ) {
        Map<String, Object> result =
                new HashMap<>();

        result.put("dialogueKey", graph.dialogueKey());
        result.put("dialogueName", graph.dialogueName());
        result.put("startNodeKey", graph.startNodeKey());
        result.put(
                "nodes",
                graph.nodes()
                        .stream()
                        .map(this::toNodeMap)
                        .toList()
        );

        return result;
    }

    private Map<String, Object> toNodeMap(
            DialogueEditorNodeView node
    ) {
        Map<String, Object> result =
                new HashMap<>();

        result.put("key", node.key());
        result.put("text", node.text());
        result.put("kind", node.kind());
        result.put(
                "transitions",
                node.transitions()
                        .stream()
                        .map(this::toTransitionMap)
                        .toList()
        );

        return result;
    }

    private Map<String, Object> toTransitionMap(
            DialogueEditorTransitionView transition
    ) {
        Map<String, Object> result =
                new HashMap<>();

        result.put("key", transition.key());
        result.put("type", transition.type().name());
        result.put("targetNodeKey", transition.targetNodeKey());
        result.put("label", transition.label());
        result.put("position", transition.position());
        result.put("terminal", transition.terminal());
        result.put(
                "conditions",
                transition.conditions()
                        .stream()
                        .map(this::toConditionMap)
                        .toList()
        );
        result.put(
                "actions",
                transition.actions()
                        .stream()
                        .map(this::toActionMap)
                        .toList()
        );
        result.put(
                "playerReplies",
                transition.playerReplies()
                        .stream()
                        .map(this::toPlayerReplyMap)
                        .toList()
        );

        return result;
    }

    private Map<String, Object> toPlayerReplyMap(
            DialogueEditorPlayerReplyView reply
    ) {
        Map<String, Object> result =
                new HashMap<>();

        result.put("text", reply.text());
        result.put("position", reply.position());
        result.put(
                "conditions",
                reply.conditions()
                        .stream()
                        .map(this::toConditionMap)
                        .toList()
        );
        result.put(
                "actions",
                reply.actions()
                        .stream()
                        .map(this::toActionMap)
                        .toList()
        );

        return result;
    }

    private Map<String, Object> toConditionMap(
            DialogueEditorConditionView condition
    ) {
        Map<String, Object> result =
                new HashMap<>();

        result.put("provider", condition.provider());
        result.put("expression", condition.expression());
        result.put("displayText", condition.displayText());

        return result;
    }

    private Map<String, Object> toActionMap(
            DialogueEditorActionView action
    ) {
        Map<String, Object> result =
                new HashMap<>();

        result.put("provider", action.provider());
        result.put("expression", action.expression());
        result.put("position", action.position());
        result.put("displayText", action.displayText());

        return result;
    }
}
