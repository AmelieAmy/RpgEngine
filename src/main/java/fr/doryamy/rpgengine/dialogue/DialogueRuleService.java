package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Gère les Conditions et Actions d'un dialogue.
 *
 * <p>Les règles peuvent appartenir au Dialogue lui-même
 * ou à une Reply / Choice.
 *
 * <p>Ce service ne modifie jamais la topologie
 * du graphe.
 */
public final class DialogueRuleService {

    private final DialogueValidator validator;
    private final DialogueRuleKeyGenerator keyGenerator;

    public DialogueRuleService(
            DialogueValidator validator,
            DialogueRuleKeyGenerator keyGenerator
    ) {

        this.validator =
                Objects.requireNonNull(
                        validator,
                        "validator"
                );

        this.keyGenerator =
                Objects.requireNonNull(
                        keyGenerator,
                        "keyGenerator"
                );
    }

    /**
     * Ajoute une Condition à un élément.
     */
    public DialogueGraph addCondition(
            DialogueGraph graph,
            DialogueElementKey ownerKey,
            String provider,
            String expression
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        requireValid(
                graph
        );

        DialogueRules previous =
                requireRulesOwner(
                        graph,
                        ownerKey
                );

        List<DialogueConditionEntry> conditions =
                new ArrayList<>(
                        previous.conditions()
                );

        conditions.add(
                new DialogueConditionEntry(
                        nextKey(),
                        new Condition(
                                provider,
                                expression
                        )
                )
        );

        return replaceRules(
                graph,
                ownerKey,
                new DialogueRules(
                        conditions,
                        previous.actions()
                )
        );
    }

    /**
     * Modifie une Condition existante.
     */
    public DialogueGraph updateCondition(
            DialogueGraph graph,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey,
            String provider,
            String expression
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                ruleKey,
                "ruleKey"
        );

        requireValid(
                graph
        );

        DialogueRules previous =
                requireRulesOwner(
                        graph,
                        ownerKey
                );

        List<DialogueConditionEntry> conditions =
                new ArrayList<>(
                        previous.conditions()
                );

        int index =
                findConditionIndex(
                        conditions,
                        ruleKey
                );

        conditions.set(
                index,
                new DialogueConditionEntry(
                        ruleKey,
                        new Condition(
                                provider,
                                expression
                        )
                )
        );

        return replaceRules(
                graph,
                ownerKey,
                new DialogueRules(
                        conditions,
                        previous.actions()
                )
        );
    }

    /**
     * Supprime une Condition.
     */
    public DialogueGraph deleteCondition(
            DialogueGraph graph,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                ruleKey,
                "ruleKey"
        );

        requireValid(
                graph
        );

        DialogueRules previous =
                requireRulesOwner(
                        graph,
                        ownerKey
                );

        List<DialogueConditionEntry> conditions =
                new ArrayList<>(
                        previous.conditions()
                );

        int index =
                findConditionIndex(
                        conditions,
                        ruleKey
                );

        conditions.remove(
                index
        );

        return replaceRules(
                graph,
                ownerKey,
                new DialogueRules(
                        conditions,
                        previous.actions()
                )
        );
    }

    /**
     * Supprime une règle sans demander à la couche appelante
     * si sa clé désigne une Condition ou une Action.
     */
    public DialogueGraph deleteRule(
            DialogueGraph graph,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey
    ) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(ownerKey, "ownerKey");
        Objects.requireNonNull(ruleKey, "ruleKey");

        requireValid(graph);

        DialogueRules previous =
                requireRulesOwner(
                        graph,
                        ownerKey
                );

        return replaceRules(
                graph,
                ownerKey,
                deleteRule(
                        previous,
                        ruleKey
                )
        );
    }

    /**
     * Ajoute une Action à la fin
     * de l'ordre d'exécution.
     */
    public DialogueGraph addAction(
            DialogueGraph graph,
            DialogueElementKey ownerKey,
            String provider,
            String expression
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        requireValid(
                graph
        );

        DialogueRules previous =
                requireRulesOwner(
                        graph,
                        ownerKey
                );

        int position =
                previous.actions()
                        .stream()
                        .mapToInt(entry ->
                                entry.action()
                                        .getPosition()
                        )
                        .max()
                        .orElse(-1)
                        + 1;

        List<DialogueActionEntry> actions =
                new ArrayList<>(
                        previous.actions()
                );

        actions.add(
                new DialogueActionEntry(
                        nextKey(),
                        new Action(
                                provider,
                                expression,
                                position
                        )
                )
        );

        return replaceRules(
                graph,
                ownerKey,
                new DialogueRules(
                        previous.conditions(),
                        actions
                )
        );
    }

    /**
     * Modifie une Action en conservant
     * sa position d'exécution.
     */
    public DialogueGraph updateAction(
            DialogueGraph graph,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey,
            String provider,
            String expression
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                ruleKey,
                "ruleKey"
        );

        requireValid(
                graph
        );

        DialogueRules previous =
                requireRulesOwner(
                        graph,
                        ownerKey
                );

        List<DialogueActionEntry> actions =
                new ArrayList<>(
                        previous.actions()
                );

        int index =
                findActionIndex(
                        actions,
                        ruleKey
                );

        DialogueActionEntry current =
                actions.get(
                        index
                );

        actions.set(
                index,
                new DialogueActionEntry(
                        ruleKey,
                        new Action(
                                provider,
                                expression,
                                current.action()
                                        .getPosition()
                        )
                )
        );

        return replaceRules(
                graph,
                ownerKey,
                new DialogueRules(
                        previous.conditions(),
                        actions
                )
        );
    }

    /**
     * Supprime une Action et normalise
     * les positions restantes.
     */
    public DialogueGraph deleteAction(
            DialogueGraph graph,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                ruleKey,
                "ruleKey"
        );

        requireValid(
                graph
        );

        DialogueRules previous =
                requireRulesOwner(
                        graph,
                        ownerKey
                );

        List<DialogueActionEntry> actions =
                new ArrayList<>(
                        previous.actions()
                );

        int index =
                findActionIndex(
                        actions,
                        ruleKey
                );

        actions.remove(
                index
        );

        actions =
                normalizeActionPositions(
                        actions
                );

        return replaceRules(
                graph,
                ownerKey,
                new DialogueRules(
                        previous.conditions(),
                        actions
                )
        );
    }


    /**
     * Ajoute une Condition à un ensemble de règles indépendant du graphe.
     * Utilisé pour les règles globales du Dialogue.
     */
    public DialogueRules addCondition(
            DialogueRules previous,
            String provider,
            String expression
    ) {
        Objects.requireNonNull(previous, "previous");

        List<DialogueConditionEntry> conditions =
                new ArrayList<>(previous.conditions());

        conditions.add(
                new DialogueConditionEntry(
                        nextKey(),
                        new Condition(provider, expression)
                )
        );

        return new DialogueRules(
                conditions,
                previous.actions()
        );
    }

    /** Modifie une Condition dans un ensemble de règles indépendant du graphe. */
    public DialogueRules updateCondition(
            DialogueRules previous,
            DialogueRuleKey ruleKey,
            String provider,
            String expression
    ) {
        Objects.requireNonNull(previous, "previous");
        Objects.requireNonNull(ruleKey, "ruleKey");

        List<DialogueConditionEntry> conditions =
                new ArrayList<>(previous.conditions());

        int index = findConditionIndex(conditions, ruleKey);
        conditions.set(
                index,
                new DialogueConditionEntry(
                        ruleKey,
                        new Condition(provider, expression)
                )
        );

        return new DialogueRules(
                conditions,
                previous.actions()
        );
    }

    /** Supprime une Condition dans un ensemble de règles indépendant du graphe. */
    public DialogueRules deleteCondition(
            DialogueRules previous,
            DialogueRuleKey ruleKey
    ) {
        Objects.requireNonNull(previous, "previous");
        Objects.requireNonNull(ruleKey, "ruleKey");

        List<DialogueConditionEntry> conditions =
                new ArrayList<>(previous.conditions());
        conditions.remove(findConditionIndex(conditions, ruleKey));

        return new DialogueRules(
                conditions,
                previous.actions()
        );
    }

    /**
     * Supprime une règle dans un ensemble indépendant du graphe,
     * sans exposer son type à la couche de transport.
     */
    public DialogueRules deleteRule(
            DialogueRules previous,
            DialogueRuleKey ruleKey
    ) {
        Objects.requireNonNull(previous, "previous");
        Objects.requireNonNull(ruleKey, "ruleKey");

        boolean conditionExists =
                previous.conditions()
                        .stream()
                        .anyMatch(entry ->
                                entry.key().equals(ruleKey)
                        );

        if (conditionExists) {
            return deleteCondition(
                    previous,
                    ruleKey
            );
        }

        boolean actionExists =
                previous.actions()
                        .stream()
                        .anyMatch(entry ->
                                entry.key().equals(ruleKey)
                        );

        if (actionExists) {
            return deleteAction(
                    previous,
                    ruleKey
            );
        }

        throw new IllegalArgumentException(
                "Règle introuvable : "
                        + ruleKey
        );
    }

    /** Ajoute une Action à un ensemble de règles indépendant du graphe. */
    public DialogueRules addAction(
            DialogueRules previous,
            String provider,
            String expression
    ) {
        Objects.requireNonNull(previous, "previous");

        int position = previous.actions()
                .stream()
                .mapToInt(entry -> entry.action().getPosition())
                .max()
                .orElse(-1)
                + 1;

        List<DialogueActionEntry> actions =
                new ArrayList<>(previous.actions());

        actions.add(
                new DialogueActionEntry(
                        nextKey(),
                        new Action(provider, expression, position)
                )
        );

        return new DialogueRules(
                previous.conditions(),
                actions
        );
    }

    /** Modifie une Action en conservant sa position d'exécution. */
    public DialogueRules updateAction(
            DialogueRules previous,
            DialogueRuleKey ruleKey,
            String provider,
            String expression
    ) {
        Objects.requireNonNull(previous, "previous");
        Objects.requireNonNull(ruleKey, "ruleKey");

        List<DialogueActionEntry> actions =
                new ArrayList<>(previous.actions());

        int index = findActionIndex(actions, ruleKey);
        DialogueActionEntry current = actions.get(index);

        actions.set(
                index,
                new DialogueActionEntry(
                        ruleKey,
                        new Action(
                                provider,
                                expression,
                                current.action().getPosition()
                        )
                )
        );

        return new DialogueRules(
                previous.conditions(),
                actions
        );
    }

    /** Supprime une Action et normalise les positions restantes. */
    public DialogueRules deleteAction(
            DialogueRules previous,
            DialogueRuleKey ruleKey
    ) {
        Objects.requireNonNull(previous, "previous");
        Objects.requireNonNull(ruleKey, "ruleKey");

        List<DialogueActionEntry> actions =
                new ArrayList<>(previous.actions());
        actions.remove(findActionIndex(actions, ruleKey));
        actions = normalizeActionPositions(actions);

        return new DialogueRules(
                previous.conditions(),
                actions
        );
    }

    /**
     * Retourne les règles de l'élément
     * et garantit que celui-ci peut en posséder.
     */
    private DialogueRules requireRulesOwner(
            DialogueGraph graph,
            DialogueElementKey ownerKey
    ) {

        Objects.requireNonNull(
                ownerKey,
                "ownerKey"
        );

        DialogueElement element =
                graph.require(
                        ownerKey
                );

        if (element instanceof DialogueReply reply) {
            return reply.rules();
        }

        if (element instanceof DialogueChoice choice) {
            return choice.rules();
        }

        throw new IllegalArgumentException(
                "L'élément "
                        + ownerKey
                        + " ne peut pas posséder "
                        + "de Conditions ou d'Actions."
        );
    }

    /**
     * Remplace uniquement les règles
     * de leur propriétaire.
     */
    private DialogueGraph replaceRules(
            DialogueGraph graph,
            DialogueElementKey ownerKey,
            DialogueRules rules
    ) {

        DialogueElement current =
                graph.require(
                        ownerKey
                );

        DialogueElement replacement;

        if (current instanceof DialogueReply reply) {

            replacement =
                    new DialogueReply(
                            reply.key(),
                            reply.participantKey(),
                            reply.text(),
                            rules
                    );

        } else if (current instanceof DialogueChoice choice) {

            replacement =
                    new DialogueChoice(
                            choice.key(),
                            choice.text(),
                            choice.position(),
                            rules
                    );

        } else {

            throw new IllegalArgumentException(
                    "L'élément "
                            + ownerKey
                            + " ne peut pas posséder "
                            + "de règles."
            );
        }

        List<DialogueElement> elements =
                new ArrayList<>(
                        graph.elements()
                                .values()
                );

        boolean replaced = false;

        for (int index = 0;
             index < elements.size();
             index++) {

            if (!elements.get(index)
                    .key()
                    .equals(ownerKey)) {
                continue;
            }

            elements.set(
                    index,
                    replacement
            );

            replaced = true;
            break;
        }

        if (!replaced) {
            throw new IllegalArgumentException(
                    "Élément introuvable : "
                            + ownerKey
            );
        }

        DialogueGraph result =
                new DialogueGraph(
                        elements,
                        graph.links()
                );

        requireValid(
                result
        );

        return result;
    }

    private static int findConditionIndex(
            List<DialogueConditionEntry> entries,
            DialogueRuleKey ruleKey
    ) {

        for (int index = 0;
             index < entries.size();
             index++) {

            if (entries.get(index)
                    .key()
                    .equals(ruleKey)) {

                return index;
            }
        }

        throw new IllegalArgumentException(
                "Condition introuvable : "
                        + ruleKey
        );
    }

    private static int findActionIndex(
            List<DialogueActionEntry> entries,
            DialogueRuleKey ruleKey
    ) {

        for (int index = 0;
             index < entries.size();
             index++) {

            if (entries.get(index)
                    .key()
                    .equals(ruleKey)) {

                return index;
            }
        }

        throw new IllegalArgumentException(
                "Action introuvable : "
                        + ruleKey
        );
    }

    private static List<DialogueActionEntry>
    normalizeActionPositions(
            List<DialogueActionEntry> entries
    ) {

        List<DialogueActionEntry> result =
                new ArrayList<>();

        for (int position = 0;
             position < entries.size();
             position++) {

            DialogueActionEntry entry =
                    entries.get(
                            position
                    );

            Action current =
                    entry.action();

            result.add(
                    new DialogueActionEntry(
                            entry.key(),
                            new Action(
                                    current.getProvider(),
                                    current.getExpression(),
                                    position
                            )
                    )
            );
        }

        return result;
    }

    private DialogueRuleKey nextKey() {

        return Objects.requireNonNull(
                keyGenerator.generate(),
                "Le générateur de clés de règles "
                        + "a retourné null."
        );
    }

    private void requireValid(
            DialogueGraph graph
    ) {

        DialogueValidationResult result =
                validator.validate(
                        graph
                );

        if (result.isValid()) {
            return;
        }

        throw new IllegalStateException(
                "Le graphe de dialogue est invalide : "
                        + String.join(
                        " | ",
                        result.errors()
                )
        );
    }
}