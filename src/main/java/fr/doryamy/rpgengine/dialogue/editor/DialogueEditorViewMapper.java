package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueNode;
import fr.doryamy.rpgengine.dialogue.DialogueTransition;
import fr.doryamy.rpgengine.dialogue.editor.formatter.DialogueEditorValueFormatter;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorActionView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorConditionView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorGraphView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorNodeView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorTransitionView;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;

import java.util.List;

/**
 * Convertit les objets métier du Dialogue Engine
 * en projections destinées à l'éditeur.
 *
 * <p>Cette classe ne lit ni n'écrit aucune donnée.
 * Elle effectue uniquement des transformations
 * déterministes entre modèles.
 */
public final class DialogueEditorViewMapper {

    private final DialogueEditorValueFormatter valueFormatter;

    public DialogueEditorViewMapper(
            DialogueEditorValueFormatter valueFormatter
    ) {
        this.valueFormatter =
                valueFormatter;
    }

    /**
     * Construit la projection graphique
     * d'un dialogue.
     *
     * @param dialogue dialogue métier
     *
     * @return graphe destiné à l'éditeur
     */
    public DialogueEditorGraphView toGraphView(
            Dialogue dialogue
    ) {
        List<DialogueEditorNodeView> nodes =
                dialogue.getNodes()
                        .stream()
                        .map(node ->
                                toNodeView(
                                        dialogue,
                                        node
                                )
                        )
                        .toList();

        return new DialogueEditorGraphView(
                dialogue.getKey(),
                dialogue.getName(),
                dialogue.getStartNodeKey(),
                nodes
        );
    }

    /**
     * Construit la projection d'un node
     * et de ses transitions sortantes.
     */
    private DialogueEditorNodeView toNodeView(
            Dialogue dialogue,
            DialogueNode node
    ) {
        List<DialogueEditorTransitionView> transitions =
                dialogue.getTransitionsFrom(
                                node.getKey()
                        )
                        .stream()
                        .map(
                                this::toTransitionView
                        )
                        .toList();

        return new DialogueEditorNodeView(
                node.getKey(),
                node.getText(),
                transitions
        );
    }

    /**
     * Construit la projection
     * d'une transition.
     */
    private DialogueEditorTransitionView toTransitionView(
            DialogueTransition transition
    ) {
        List<DialogueEditorConditionView> conditions =
                transition.getConditions()
                        .stream()
                        .map(
                                this::toConditionView
                        )
                        .toList();

        List<DialogueEditorActionView> actions =
                transition.getActions()
                        .stream()
                        .map(
                                this::toActionView
                        )
                        .toList();

        return new DialogueEditorTransitionView(
                transition.getKey(),
                transition.getType(),
                transition.getTargetNodeKey(),
                transition.getLabel(),
                transition.getPosition(),
                conditions,
                actions
        );
    }

    private DialogueEditorConditionView toConditionView(
            Condition condition
    ) {
        return new DialogueEditorConditionView(
                condition.getProvider(),
                condition.getExpression(),
                valueFormatter.formatCondition(
                        condition.getProvider(),
                        condition.getExpression()
                )
        );
    }

    private DialogueEditorActionView toActionView(
            Action action
    ) {
        return new DialogueEditorActionView(
                action.getProvider(),
                action.getExpression(),
                action.getPosition(),
                valueFormatter.formatAction(
                        action.getProvider(),
                        action.getExpression()
                )
        );
    }
}