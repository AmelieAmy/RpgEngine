package fr.doryamy.rpgengine.dialogue;

import java.util.*;

/**
 * Valide les invariants métier complets
 * d'un graphe de dialogue.
 *
 * <p>DialogueGraph garantit l'intégrité structurelle
 * élémentaire. Ce validator garantit que cette structure
 * constitue réellement un dialogue exécutable
 * sans ambiguïté.
 */
public final class DialogueValidator {

    /**
     * Valide un dialogue complet.
     *
     * @param dialogue dialogue à valider
     * @return résultat de validation
     */
    public DialogueValidationResult validate(
            Dialogue dialogue
    ) {

        if (dialogue == null) {
            return DialogueValidationResult.invalid(
                    List.of(
                            "Le dialogue ne peut pas être null."
                    )
            );
        }

        return validate(
                dialogue.graph()
        );
    }

    public DialogueValidationResult validate(
            DialogueGraph graph
    ) {

        if (graph == null) {
            return DialogueValidationResult.invalid(
                    List.of(
                            "Le graphe ne peut pas être null."
                    )
            );
        }

        List<String> errors =
                new ArrayList<>();

        validateStart(
                graph,
                errors
        );

        validateReplies(
                graph,
                errors
        );

        validateBranches(
                graph,
                errors
        );

        validateChoices(
                graph,
                errors
        );

        validateContinuationTargets(
                graph,
                errors
        );

        validateEnds(
                graph,
                errors
        );

        validateReachability(
                graph,
                errors
        );

        validateEndReachability(
                graph,
                errors
        );

        if (errors.isEmpty()) {
            return DialogueValidationResult.valid();
        }

        return DialogueValidationResult.invalid(
                errors
        );
    }

    /**
     * Valide l'élément Start.
     */
    private void validateStart(
            DialogueGraph graph,
            List<String> errors
    ) {

        DialogueStart start =
                graph.start();

        int incoming =
                graph.incomingLinks(
                        start.key()
                ).size();

        int outgoing =
                graph.outgoingLinks(
                        start.key()
                ).size();

        if (incoming != 0) {
            errors.add(
                    "Le Start "
                            + start.key()
                            + " ne doit posséder aucun lien entrant."
            );
        }

        if (outgoing != 1) {
            errors.add(
                    "Le Start "
                            + start.key()
                            + " doit posséder exactement "
                            + "un lien sortant. Trouvés : "
                            + outgoing
                            + "."
            );
        }
    }

    /**
     * Valide toutes les répliques.
     */
    private void validateReplies(
            DialogueGraph graph,
            List<String> errors
    ) {

        for (DialogueElement element :
                graph.elements().values()) {

            if (!(element instanceof DialogueReply reply)) {
                continue;
            }

            int outgoing =
                    graph.outgoingLinks(
                            reply.key()
                    ).size();

            if (outgoing != 1) {
                errors.add(
                        "La réplique "
                                + reply.key()
                                + " doit posséder exactement "
                                + "un lien sortant. Trouvés : "
                                + outgoing
                                + "."
                );
            }
        }
    }

    /**
     * Valide tous les embranchements.
     */
    private void validateBranches(
            DialogueGraph graph,
            List<String> errors
    ) {

        for (DialogueElement element :
                graph.elements().values()) {

            if (!(element instanceof DialogueBranch branch)) {
                continue;
            }

            List<DialogueLink> outgoing =
                    graph.outgoingLinks(
                            branch.key()
                    );

            if (outgoing.size() < 2) {
                errors.add(
                        "L'embranchement "
                                + branch.key()
                                + " doit posséder au moins "
                                + "deux choix. Trouvés : "
                                + outgoing.size()
                                + "."
                );
            }

            Set<Integer> positions =
                    new HashSet<>();

            for (DialogueLink link : outgoing) {

                DialogueElement target =
                        graph.require(
                                link.target()
                        );

                if (!(target instanceof DialogueChoice choice)) {

                    errors.add(
                            "L'embranchement "
                                    + branch.key()
                                    + " cible un élément qui "
                                    + "n'est pas un choix : "
                                    + target.key()
                                    + "."
                    );

                    continue;
                }

                if (!positions.add(
                        choice.position()
                )) {

                    errors.add(
                            "L'embranchement "
                                    + branch.key()
                                    + " contient plusieurs choix "
                                    + "à la position "
                                    + choice.position()
                                    + "."
                    );
                }
            }
        }
    }

    /**
     * Valide tous les choix.
     */
    private void validateChoices(
            DialogueGraph graph,
            List<String> errors
    ) {

        for (DialogueElement element :
                graph.elements().values()) {

            if (!(element instanceof DialogueChoice choice)) {
                continue;
            }

            List<DialogueLink> incoming =
                    graph.incomingLinks(
                            choice.key()
                    );

            List<DialogueLink> outgoing =
                    graph.outgoingLinks(
                            choice.key()
                    );

            if (incoming.size() != 1) {

                errors.add(
                        "Le choix "
                                + choice.key()
                                + " doit posséder exactement "
                                + "un lien entrant. Trouvés : "
                                + incoming.size()
                                + "."
                );

            } else {

                DialogueElement source =
                        graph.require(
                                incoming.getFirst()
                                        .source()
                        );

                if (!(source instanceof DialogueBranch)) {
                    errors.add(
                            "Le choix "
                                    + choice.key()
                                    + " doit appartenir à un "
                                    + "embranchement, mais son "
                                    + "parent est "
                                    + source.key()
                                    + "."
                    );
                }
            }

            if (outgoing.size() != 1) {
                errors.add(
                        "Le choix "
                                + choice.key()
                                + " doit posséder exactement "
                                + "un lien sortant. Trouvés : "
                                + outgoing.size()
                                + "."
                );
            }
        }
    }

    /**
     * Vérifie que les continuations respectent
     * le vocabulaire structurel du dialogue.
     */
    private void validateContinuationTargets(
            DialogueGraph graph,
            List<String> errors
    ) {

        for (DialogueElement element :
                graph.elements().values()) {

            if (element instanceof DialogueBranch
                    || element instanceof DialogueEnd) {

                continue;
            }

            for (DialogueLink link :
                    graph.outgoingLinks(
                            element.key()
                    )) {

                DialogueElement target =
                        graph.require(
                                link.target()
                        );

                if (target instanceof DialogueChoice) {

                    errors.add(
                            "L'élément "
                                    + element.key()
                                    + " ne peut pas cibler "
                                    + "directement le choix "
                                    + target.key()
                                    + ". Un DialogueChoice doit "
                                    + "être atteint depuis son "
                                    + "DialogueBranch."
                    );
                }

                if (target instanceof DialogueStart) {

                    errors.add(
                            "L'élément "
                                    + element.key()
                                    + " ne peut pas cibler "
                                    + "le Start "
                                    + target.key()
                                    + "."
                    );
                }
            }
        }
    }

    /**
     * Valide les fins du dialogue.
     */
    private void validateEnds(
            DialogueGraph graph,
            List<String> errors
    ) {

        int endCount = 0;

        for (DialogueElement element :
                graph.elements().values()) {

            if (!(element instanceof DialogueEnd end)) {
                continue;
            }

            endCount++;

            int outgoing =
                    graph.outgoingLinks(
                            end.key()
                    ).size();

            if (outgoing != 0) {
                errors.add(
                        "La fin "
                                + end.key()
                                + " ne doit posséder "
                                + "aucun lien sortant."
                );
            }
        }

        if (endCount == 0) {
            errors.add(
                    "Le dialogue doit contenir "
                            + "au moins une fin."
            );
        }
    }

    /**
     * Vérifie que tous les éléments du graphe
     * sont accessibles depuis Start.
     */
    private void validateReachability(
            DialogueGraph graph,
            List<String> errors
    ) {

        Set<DialogueElementKey> reachable =
                new HashSet<>();

        Deque<DialogueElementKey> pending =
                new ArrayDeque<>();

        pending.add(
                graph.start().key()
        );

        while (!pending.isEmpty()) {

            DialogueElementKey current =
                    pending.removeFirst();

            if (!reachable.add(current)) {
                continue;
            }

            for (DialogueLink link :
                    graph.outgoingLinks(current)) {

                pending.addLast(
                        link.target()
                );
            }
        }

        for (DialogueElementKey key :
                graph.elements().keySet()) {

            if (!reachable.contains(key)) {
                errors.add(
                        "L'élément "
                                + key
                                + " est inaccessible depuis "
                                + "le Start."
                );
            }
        }
    }

    /**
     * Vérifie que chaque élément peut atteindre
     * au moins une fin de dialogue.
     *
     * <p>Les boucles sont autorisées dès lors
     * qu'une sortie vers une fin reste possible.
     */
    private void validateEndReachability(
            DialogueGraph graph,
            List<String> errors
    ) {

        Set<DialogueElementKey> canReachEnd =
                new HashSet<>();

        Deque<DialogueElementKey> pending =
                new ArrayDeque<>();

        Map<
                DialogueElementKey,
                List<DialogueElementKey>
                > reverseLinks =
                buildReverseLinks(graph);

        for (DialogueElement element :
                graph.elements().values()) {

            if (element instanceof DialogueEnd) {

                canReachEnd.add(
                        element.key()
                );

                pending.addLast(
                        element.key()
                );
            }
        }

        while (!pending.isEmpty()) {

            DialogueElementKey current =
                    pending.removeFirst();

            for (DialogueElementKey previous :
                    reverseLinks.getOrDefault(
                            current,
                            List.of()
                    )) {

                if (canReachEnd.add(previous)) {
                    pending.addLast(previous);
                }
            }
        }

        for (DialogueElementKey key :
                graph.elements().keySet()) {

            if (!canReachEnd.contains(key)) {
                errors.add(
                        "L'élément "
                                + key
                                + " ne possède aucun chemin "
                                + "vers une fin de dialogue."
                );
            }
        }
    }

    /**
     * Construit l'index inverse des liaisons.
     */
    private Map<
            DialogueElementKey,
            List<DialogueElementKey>
            > buildReverseLinks(
            DialogueGraph graph
    ) {

        Map<
                DialogueElementKey,
                List<DialogueElementKey>
                > reverse =
                new HashMap<>();

        for (DialogueLink link :
                graph.links()) {

            reverse.computeIfAbsent(
                    link.target(),
                    ignored ->
                            new ArrayList<>()
            ).add(
                    link.source()
            );
        }

        return reverse;
    }
}