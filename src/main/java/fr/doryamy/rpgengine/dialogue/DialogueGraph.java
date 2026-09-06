package fr.doryamy.rpgengine.dialogue;

import java.util.*;

/**
 * Graphe structurel complet d'un dialogue.
 *
 * <p>Le graphe contient les éléments métier
 * et les liaisons qui les relient.
 *
 * <p>Cette classe garantit uniquement les invariants
 * structurels fondamentaux nécessaires pour disposer
 * d'un graphe cohérent.
 *
 * <p>Les invariants métier complets sont vérifiés
 * par DialogueValidator.
 */
public final class DialogueGraph {

    private final Map<
            DialogueElementKey,
            DialogueElement
            > elements;

    private final Set<DialogueLink> links;

    private final DialogueStart start;

    /**
     * Construit un graphe de dialogue.
     *
     * @param elements éléments du graphe
     * @param links liaisons du graphe
     */
    public DialogueGraph(
            Collection<? extends DialogueElement> elements,
            Collection<DialogueLink> links
    ) {

        Objects.requireNonNull(
                elements,
                "La collection d'éléments ne peut pas être null."
        );

        Objects.requireNonNull(
                links,
                "La collection de liaisons ne peut pas être null."
        );

        this.elements =
                buildElements(elements);

        this.start =
                resolveStart(this.elements);

        this.links =
                buildLinks(
                        links,
                        this.elements
                );
    }

    /**
     * Retourne tous les éléments du graphe,
     * indexés par leur clé.
     */
    public Map<
            DialogueElementKey,
            DialogueElement
            > elements() {

        return elements;
    }

    /**
     * Retourne toutes les liaisons du graphe.
     */
    public Set<DialogueLink> links() {

        return links;
    }

    /**
     * Retourne l'unique élément Start.
     */
    public DialogueStart start() {

        return start;
    }

    /**
     * Recherche un élément par sa clé.
     */
    public Optional<DialogueElement> find(
            DialogueElementKey key
    ) {

        Objects.requireNonNull(
                key,
                "La clé recherchée ne peut pas être null."
        );

        return Optional.ofNullable(
                elements.get(key)
        );
    }

    /**
     * Retourne un élément ou échoue s'il
     * n'existe pas dans le graphe.
     */
    public DialogueElement require(
            DialogueElementKey key
    ) {

        return find(key)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Élément de dialogue introuvable : "
                                        + key
                        )
                );
    }

    /**
     * Retourne les liaisons sortantes
     * d'un élément.
     */
    public List<DialogueLink> outgoingLinks(
            DialogueElementKey source
    ) {

        require(source);

        return links.stream()
                .filter(link ->
                        link.source().equals(source)
                )
                .toList();
    }

    /**
     * Retourne les liaisons entrantes
     * d'un élément.
     */
    public List<DialogueLink> incomingLinks(
            DialogueElementKey target
    ) {

        require(target);

        return links.stream()
                .filter(link ->
                        link.target().equals(target)
                )
                .toList();
    }

    /**
     * Retourne les éléments directement atteignables
     * depuis l'élément indiqué.
     */
    public List<DialogueElement> outgoingElements(
            DialogueElementKey source
    ) {

        return outgoingLinks(source)
                .stream()
                .map(DialogueLink::target)
                .map(this::require)
                .toList();
    }

    /**
     * Retourne les éléments reliés directement
     * vers l'élément indiqué.
     */
    public List<DialogueElement> incomingElements(
            DialogueElementKey target
    ) {

        return incomingLinks(target)
                .stream()
                .map(DialogueLink::source)
                .map(this::require)
                .toList();
    }

    /**
     * Retourne les choix directement associés
     * à un embranchement.
     *
     * <p>Cette méthode ne valide pas que le graphe
     * complet respecte tous les invariants métier.
     * Elle retourne uniquement les DialogueChoice
     * directement reliés à l'embranchement.
     */
    public List<DialogueChoice> choicesOf(
            DialogueElementKey branchKey
    ) {

        DialogueElement element =
                require(branchKey);

        if (!(element instanceof DialogueBranch)) {
            throw new IllegalArgumentException(
                    "L'élément "
                            + branchKey
                            + " n'est pas un embranchement."
            );
        }

        return outgoingElements(branchKey)
                .stream()
                .filter(DialogueChoice.class::isInstance)
                .map(DialogueChoice.class::cast)
                .sorted(
                        java.util.Comparator.comparingInt(
                                DialogueChoice::position
                        )
                )
                .toList();
    }

    /**
     * Construit l'index immuable des éléments.
     */
    private static Map<
            DialogueElementKey,
            DialogueElement
            > buildElements(
            Collection<? extends DialogueElement> source
    ) {

        Map<
                DialogueElementKey,
                DialogueElement
                > result =
                new LinkedHashMap<>();

        for (DialogueElement element : source) {

            Objects.requireNonNull(
                    element,
                    "Un élément du graphe ne peut pas être null."
            );

            DialogueElement previous =
                    result.putIfAbsent(
                            element.key(),
                            element
                    );

            if (previous != null) {
                throw new IllegalArgumentException(
                        "Clé d'élément dupliquée : "
                                + element.key()
                );
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException(
                    "Un graphe de dialogue ne peut pas être vide."
            );
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(result)
        );
    }

    /**
     * Recherche et garantit l'unique Start
     * du graphe.
     */
    private static DialogueStart resolveStart(
            Map<
                    DialogueElementKey,
                    DialogueElement
                    > elements
    ) {

        List<DialogueStart> starts =
                elements.values()
                        .stream()
                        .filter(
                                DialogueStart.class::isInstance
                        )
                        .map(
                                DialogueStart.class::cast
                        )
                        .toList();

        if (starts.size() != 1) {
            throw new IllegalArgumentException(
                    "Un graphe de dialogue doit contenir "
                            + "exactement un Start. Trouvés : "
                            + starts.size()
            );
        }

        return starts.getFirst();
    }

    /**
     * Construit l'ensemble immuable des liaisons
     * et garantit que leurs extrémités existent.
     */
    private static Set<DialogueLink> buildLinks(
            Collection<DialogueLink> source,
            Map<
                    DialogueElementKey,
                    DialogueElement
                    > elements
    ) {

        Set<DialogueLink> result =
                new LinkedHashSet<>();

        for (DialogueLink link : source) {

            Objects.requireNonNull(
                    link,
                    "Une liaison du graphe ne peut pas être null."
            );

            if (!elements.containsKey(
                    link.source()
            )) {

                throw new IllegalArgumentException(
                        "Source de liaison inexistante : "
                                + link.source()
                );
            }

            if (!elements.containsKey(
                    link.target()
            )) {

                throw new IllegalArgumentException(
                        "Cible de liaison inexistante : "
                                + link.target()
                );
            }

            if (!result.add(link)) {
                throw new IllegalArgumentException(
                        "Liaison dupliquée : "
                                + link.source()
                                + " -> "
                                + link.target()
                );
            }
        }

        return Collections.unmodifiableSet(
                new LinkedHashSet<>(result)
        );
    }
}