package fr.doryamy.rpgengine.dialogue;

import java.util.*;

/**
 * Service responsable des mutations structurelles
 * d'un graphe de dialogue.
 *
 * <p>Il ne gère ni la persistance, ni les Conditions,
 * ni les Actions, ni la présentation.
 *
 * <p>Chaque opération retourne un nouveau graphe.
 */
public final class DialogueGraphService {

    private static final String DEFAULT_CHOICE_1 =
            "Choix 1";

    private static final String DEFAULT_CHOICE_2 =
            "Choix 2";

    private final DialogueValidator validator;
    private final DialogueElementKeyGenerator keyGenerator;

    public DialogueGraphService(
            DialogueValidator validator,
            DialogueElementKeyGenerator keyGenerator
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
     * Crée le graphe initial d'un dialogue.
     *
     * <pre>
     * Start -> End
     * </pre>
     */
    public DialogueGraph createEmptyGraph() {

        DialogueStart start =
                new DialogueStart(
                        nextKey()
                );

        DialogueEnd end =
                new DialogueEnd(
                        nextKey()
                );

        DialogueGraph graph =
                new DialogueGraph(
                        List.of(
                                start,
                                end
                        ),
                        List.of(
                                new DialogueLink(
                                        start.key(),
                                        end.key()
                                )
                        )
                );

        requireValid(graph);

        return graph;
    }

    /**
     * Insère une réplique sur une liaison existante.
     *
     * <pre>
     * A -> B
     *
     * devient
     *
     * A -> Reply -> B
     * </pre>
     */
    public DialogueGraph insertReply(
            DialogueGraph graph,
            DialogueInsertionPoint insertionPoint,
            DialogueParticipantKey participantKey,
            String text
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                insertionPoint,
                "insertionPoint"
        );

        Objects.requireNonNull(
                participantKey,
                "participantKey"
        );

        requireValid(graph);

        DialogueLink replaced =
                requireInsertionLink(
                        graph,
                        insertionPoint
                );

        DialogueReply reply =
                new DialogueReply(
                        nextKey(),
                        participantKey,
                        text,
                        DialogueRules.empty()
                );

        List<DialogueElement> elements =
                mutableElements(graph);

        Set<DialogueLink> links =
                mutableLinks(graph);

        elements.add(reply);

        links.remove(replaced);

        links.add(
                new DialogueLink(
                        replaced.source(),
                        reply.key()
                )
        );

        links.add(
                new DialogueLink(
                        reply.key(),
                        replaced.target()
                )
        );

        return buildValidGraph(
                elements,
                links
        );
    }

    /**
     * Insère un embranchement sur une liaison existante.
     *
     * <pre>
     * A -> B
     *
     * devient
     *
     * A -> Branch
     *        |-> Choice 1 -> B
     *        |-> Choice 2 -> End
     * </pre>
     *
     * <p>Le premier choix conserve donc explicitement
     * la continuation qui existait avant l'insertion.
     * Le nouveau chemin reçoit automatiquement sa fin.
     */
    public DialogueGraph insertBranch(
            DialogueGraph graph,
            DialogueInsertionPoint insertionPoint
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                insertionPoint,
                "insertionPoint"
        );

        requireValid(graph);

        DialogueLink replaced =
                requireInsertionLink(
                        graph,
                        insertionPoint
                );

        DialogueBranch branch =
                new DialogueBranch(
                        nextKey()
                );

        DialogueChoice firstChoice =
                new DialogueChoice(
                        nextKey(),
                        DEFAULT_CHOICE_1,
                        0,
                        DialogueRules.empty()
                );

        DialogueChoice secondChoice =
                new DialogueChoice(
                        nextKey(),
                        DEFAULT_CHOICE_2,
                        1,
                        DialogueRules.empty()
                );

        DialogueEnd end =
                new DialogueEnd(
                        nextKey()
                );

        List<DialogueElement> elements =
                mutableElements(graph);

        Set<DialogueLink> links =
                mutableLinks(graph);

        elements.add(branch);
        elements.add(firstChoice);
        elements.add(secondChoice);
        elements.add(end);

        links.remove(replaced);

        links.add(
                new DialogueLink(
                        replaced.source(),
                        branch.key()
                )
        );

        links.add(
                new DialogueLink(
                        branch.key(),
                        firstChoice.key()
                )
        );

        links.add(
                new DialogueLink(
                        branch.key(),
                        secondChoice.key()
                )
        );

        links.add(
                new DialogueLink(
                        firstChoice.key(),
                        replaced.target()
                )
        );

        links.add(
                new DialogueLink(
                        secondChoice.key(),
                        end.key()
                )
        );

        return buildValidGraph(
                elements,
                links
        );
    }

    /**
     * Ajoute un nouveau choix à un embranchement.
     *
     * <p>Le nouveau choix reçoit automatiquement
     * sa propre fin de dialogue.
     */
    public DialogueGraph addChoice(
            DialogueGraph graph,
            DialogueElementKey branchKey
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                branchKey,
                "branchKey"
        );

        requireValid(graph);

        DialogueElement element =
                graph.require(branchKey);

        if (!(element instanceof DialogueBranch)) {
            throw new IllegalArgumentException(
                    "L'élément "
                            + branchKey
                            + " n'est pas un embranchement."
            );
        }

        List<DialogueChoice> currentChoices =
                graph.choicesOf(branchKey);

        int nextPosition =
                currentChoices.stream()
                        .mapToInt(
                                DialogueChoice::position
                        )
                        .max()
                        .orElse(-1)
                        + 1;

        DialogueChoice choice =
                new DialogueChoice(
                        nextKey(),
                        "Choix " + (nextPosition + 1),
                        nextPosition,
                        DialogueRules.empty()
                );

        DialogueEnd end =
                new DialogueEnd(
                        nextKey()
                );

        List<DialogueElement> elements =
                mutableElements(graph);

        Set<DialogueLink> links =
                mutableLinks(graph);

        elements.add(choice);
        elements.add(end);

        links.add(
                new DialogueLink(
                        branchKey,
                        choice.key()
                )
        );

        links.add(
                new DialogueLink(
                        choice.key(),
                        end.key()
                )
        );

        return buildValidGraph(
                elements,
                links
        );
    }

    /**
     * Supprime un élément administrable du graphe.
     *
     * <p>Cette entrée générique centralise la politique de suppression exposée
     * à l'administration. Les suppressions spécialisées restent disponibles
     * pour les règles structurelles propres à chaque type.
     *
     * <p>Pour l'instant, seule la suppression d'une Reply est activée.
     */
    public DialogueGraph deleteElement(
            DialogueGraph graph,
            DialogueElementKey elementKey
    ) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(elementKey, "elementKey");

        requireValid(graph);

        DialogueElement element = graph.require(elementKey);

        if (element instanceof DialogueReply) {
            return deleteReply(graph, elementKey);
        }
        if (element instanceof DialogueChoice) {
            return deleteChoice(graph, elementKey);
        }
        if (element instanceof DialogueBranch) {
            return deleteBranch(graph, elementKey);
        }

        throw new IllegalArgumentException(
                "La suppression de l'élément "
                        + elementKey
                        + " n'est pas autorisée pour ce type."
        );
    }

    /**
     * Supprime une réplique en reconnectant
     * directement toutes ses entrées
     * vers sa continuation.
     */
    public DialogueGraph deleteReply(
            DialogueGraph graph,
            DialogueElementKey replyKey
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                replyKey,
                "replyKey"
        );

        requireValid(graph);

        DialogueElement element =
                graph.require(replyKey);

        if (!(element instanceof DialogueReply)) {
            throw new IllegalArgumentException(
                    "L'élément "
                            + replyKey
                            + " n'est pas une réplique."
            );
        }

        List<DialogueLink> outgoing =
                graph.outgoingLinks(
                        replyKey
                );

        /*
         * Cet invariant est déjà assuré
         * par DialogueValidator.
         */
        DialogueElementKey continuation =
                outgoing.getFirst()
                        .target();

        List<DialogueLink> incoming =
                graph.incomingLinks(
                        replyKey
                );

        List<DialogueElement> elements =
                mutableElements(graph);

        Set<DialogueLink> links =
                mutableLinks(graph);

        removeElement(
                elements,
                replyKey
        );

        removeIncidentLinks(
                links,
                replyKey
        );

        for (DialogueLink incomingLink : incoming) {

            links.add(
                    new DialogueLink(
                            incomingLink.source(),
                            continuation
                    )
            );
        }

        return buildValidGraph(
                elements,
                links
        );
    }

    /**
     * Supprime un choix d'un embranchement.
     *
     * <p>La suppression est refusée si elle ferait
     * passer l'embranchement sous deux choix.
     *
     * <p>Les éléments qui deviennent inaccessibles
     * uniquement à cause de cette suppression
     * sont ensuite nettoyés.
     */
    public DialogueGraph deleteChoice(
            DialogueGraph graph,
            DialogueElementKey choiceKey
    ) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(choiceKey, "choiceKey");
        requireValid(graph);

        DialogueElement element = graph.require(choiceKey);
        if (!(element instanceof DialogueChoice)) {
            throw new IllegalArgumentException(
                    "L'élément " + choiceKey + " n'est pas un choix."
            );
        }

        DialogueElementKey branchKey = graph.incomingLinks(choiceKey).getFirst().source();
        List<DialogueChoice> branchChoices = graph.choicesOf(branchKey);

        if (branchChoices.size() == 2) {
            DialogueChoice survivor = branchChoices.stream()
                    .filter(choice -> !choice.key().equals(choiceKey))
                    .findFirst()
                    .orElseThrow();

            DialogueElementKey continuation = graph.outgoingLinks(survivor.key())
                    .getFirst()
                    .target();
            List<DialogueLink> branchIncoming = graph.incomingLinks(branchKey);

            List<DialogueElement> elements = mutableElements(graph);
            Set<DialogueLink> links = mutableLinks(graph);

            // On détache d'abord le choix supprimé : toute sa branche devient
            // inaccessible et peut être nettoyée selon le modèle arborescent.
            removeElement(elements, choiceKey);
            removeIncidentLinks(links, choiceKey);
            removeUnreachableElements(elements, links, graph.start().key());

            // Avec un seul choix restant, l'embranchement n'a plus de raison
            // d'exister : on retire le losange et le Choice survivant, puis on
            // reconnecte le flux principal directement à sa continuation.
            removeElement(elements, branchKey);
            removeIncidentLinks(links, branchKey);
            removeElement(elements, survivor.key());
            removeIncidentLinks(links, survivor.key());

            for (DialogueLink incomingLink : branchIncoming) {
                links.add(new DialogueLink(incomingLink.source(), continuation));
            }

            removeUnreachableElements(elements, links, graph.start().key());
            return buildValidGraph(elements, links);
        }

        List<DialogueElement> elements = mutableElements(graph);
        Set<DialogueLink> links = mutableLinks(graph);

        removeElement(elements, choiceKey);
        removeIncidentLinks(links, choiceKey);
        removeUnreachableElements(elements, links, graph.start().key());

        DialogueGraph result = new DialogueGraph(elements, links);
        result = normalizeChoicePositions(result, branchKey);
        requireValid(result);
        return result;
    }

    /**
     * Supprime un embranchement complet.
     *
     * <p>Chaque chemin qui arrivait sur cet
     * embranchement est reconnecté vers une
     * nouvelle fin système.
     *
     * <p>Les sous-graphes qui ne sont alors
     * plus accessibles depuis Start sont supprimés.
     */
    public DialogueGraph deleteBranch(
            DialogueGraph graph,
            DialogueElementKey branchKey
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                branchKey,
                "branchKey"
        );

        requireValid(graph);

        DialogueElement element =
                graph.require(branchKey);

        if (!(element instanceof DialogueBranch)) {
            throw new IllegalArgumentException(
                    "L'élément "
                            + branchKey
                            + " n'est pas un embranchement."
            );
        }

        List<DialogueLink> incoming =
                graph.incomingLinks(
                        branchKey
                );

        DialogueEnd replacementEnd =
                new DialogueEnd(
                        nextKey()
                );

        List<DialogueElement> elements =
                mutableElements(graph);

        Set<DialogueLink> links =
                mutableLinks(graph);

        elements.add(
                replacementEnd
        );

        removeElement(
                elements,
                branchKey
        );

        removeIncidentLinks(
                links,
                branchKey
        );

        for (DialogueLink incomingLink : incoming) {

            links.add(
                    new DialogueLink(
                            incomingLink.source(),
                            replacementEnd.key()
                    )
            );
        }

        removeUnreachableElements(
                elements,
                links,
                graph.start().key()
        );

        return buildValidGraph(
                elements,
                links
        );
    }

    /**
     * Vérifie qu'un point d'insertion correspond
     * réellement à une liaison du graphe.
     */
    private DialogueLink requireInsertionLink(
            DialogueGraph graph,
            DialogueInsertionPoint insertionPoint
    ) {

        DialogueLink expected =
                insertionPoint.asLink();

        if (!graph.links().contains(expected)) {
            throw new IllegalArgumentException(
                    "Le point d'insertion "
                            + insertionPoint.source()
                            + " -> "
                            + insertionPoint.target()
                            + " ne correspond à aucune liaison."
            );
        }

        return expected;
    }

    /**
     * Normalise les positions des choix
     * appartenant à un embranchement.
     */
    private DialogueGraph normalizeChoicePositions(
            DialogueGraph graph,
            DialogueElementKey branchKey
    ) {

        List<DialogueChoice> choices =
                graph.choicesOf(
                        branchKey
                );

        List<DialogueElement> elements =
                mutableElements(graph);

        for (int position = 0;
             position < choices.size();
             position++) {

            DialogueChoice current =
                    choices.get(position);

            if (current.position() == position) {
                continue;
            }

            DialogueChoice normalized =
                    new DialogueChoice(
                            current.key(),
                            current.text(),
                            position,
                            current.rules()
                    );

            replaceElement(
                    elements,
                    normalized
            );
        }

        return new DialogueGraph(
                elements,
                graph.links()
        );
    }

    /**
     * Supprime tous les éléments qui ne sont
     * plus accessibles depuis Start.
     */
    private void removeUnreachableElements(
            List<DialogueElement> elements,
            Set<DialogueLink> links,
            DialogueElementKey startKey
    ) {

        Set<DialogueElementKey> reachable =
                new HashSet<>();

        List<DialogueElementKey> pending =
                new ArrayList<>();

        pending.add(
                startKey
        );

        int index = 0;

        while (index < pending.size()) {

            DialogueElementKey current =
                    pending.get(index++);

            if (!reachable.add(current)) {
                continue;
            }

            for (DialogueLink link : links) {

                if (link.source().equals(current)) {
                    pending.add(
                            link.target()
                    );
                }
            }
        }

        elements.removeIf(
                element ->
                        !reachable.contains(
                                element.key()
                        )
        );

        links.removeIf(
                link ->
                        !reachable.contains(
                                link.source()
                        )
                                || !reachable.contains(
                                link.target()
                        )
        );
    }

    /**
     * Construit puis valide un graphe.
     */
    private DialogueGraph buildValidGraph(
            Collection<? extends DialogueElement> elements,
            Collection<DialogueLink> links
    ) {

        DialogueGraph graph =
                new DialogueGraph(
                        elements,
                        links
                );

        requireValid(graph);

        return graph;
    }

    /**
     * Refuse une mutation sur un graphe invalide
     * ou un résultat invalide.
     */
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

    /**
     * Produit une nouvelle clé et garantit
     * qu'elle n'est pas null.
     */
    private DialogueElementKey nextKey() {

        return Objects.requireNonNull(
                keyGenerator.generate(),
                "Le générateur de clés a retourné null."
        );
    }

    private static List<DialogueElement> mutableElements(
            DialogueGraph graph
    ) {

        return new ArrayList<>(
                graph.elements().values()
        );
    }

    private static Set<DialogueLink> mutableLinks(
            DialogueGraph graph
    ) {

        return new LinkedHashSet<>(
                graph.links()
        );
    }

    private static void removeElement(
            List<DialogueElement> elements,
            DialogueElementKey key
    ) {

        boolean removed =
                elements.removeIf(
                        element ->
                                element.key()
                                        .equals(key)
                );

        if (!removed) {
            throw new IllegalArgumentException(
                    "Élément introuvable : "
                            + key
            );
        }
    }

    private static void replaceElement(
            List<DialogueElement> elements,
            DialogueElement replacement
    ) {

        for (int index = 0;
             index < elements.size();
             index++) {

            if (elements.get(index)
                    .key()
                    .equals(
                            replacement.key()
                    )) {

                elements.set(
                        index,
                        replacement
                );

                return;
            }
        }

        throw new IllegalArgumentException(
                "Élément introuvable : "
                        + replacement.key()
        );
    }

    private static void removeIncidentLinks(
            Set<DialogueLink> links,
            DialogueElementKey key
    ) {

        links.removeIf(
                link ->
                        link.source().equals(key)
                                || link.target().equals(key)
        );
    }
}