package fr.doryamy.rpgengine.dialogue;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Représente un dialogue complet.
 *
 * Un dialogue est un graphe narratif composé :
 *   de nodes ;
 *   de transitions ;
 *   d'un node de départ.
 *
 * Cette classe est immuable et ne contient aucune logique d'exécution.
 *
 * Elle fournit uniquement des méthodes permettant d'accéder
 * aux éléments constituant son propre graphe.
 */
public final class Dialogue {

    private final String key;
    private final String name;

    private final String startNodeKey;

    private final List<DialogueNode> nodes;
    private final List<DialogueTransition> transitions;

    /**
     * Construit un dialogue complet.
     *
     * @param key clé métier
     * @param name nom lisible
     * @param startNodeKey clé du node de départ,
     *   éventuellement null pendant l'édition
     * @param nodes nodes du dialogue
     * @param transitions transitions du dialogue
     */
    public Dialogue(
            String key,
            String name,
            String startNodeKey,
            List<DialogueNode> nodes,
            List<DialogueTransition> transitions
    ) {
        this.key =
                Objects.requireNonNull(
                        key,
                        "La clé du dialogue ne peut pas être null."
                );

        this.name =
                Objects.requireNonNull(
                        name,
                        "Le nom du dialogue ne peut pas être null."
                );

        this.startNodeKey =
                startNodeKey;

        this.nodes =
                List.copyOf(
                        Objects.requireNonNull(
                                nodes,
                                "Les nodes ne peuvent pas être null."
                        )
                );

        this.transitions =
                List.copyOf(
                        Objects.requireNonNull(
                                transitions,
                                "Les transitions ne peuvent pas être null."
                        )
                );
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getStartNodeKey() {
        return startNodeKey;
    }

    public List<DialogueNode> getNodes() {
        return nodes;
    }

    public List<DialogueTransition> getTransitions() {
        return transitions;
    }

    /**
     * Recherche un node à partir de sa clé métier.
     *
     * @param nodeKey clé du node
     * @return node correspondant s'il existe
     */
    public Optional<DialogueNode> findNode(
            String nodeKey
    ) {
        if (nodeKey == null) {
            return Optional.empty();
        }

        return nodes.stream()
                .filter(node ->
                        node.getKey()
                                .equals(nodeKey)
                )
                .findFirst();
    }

    /**
     * Retourne le node de départ du dialogue.
     *
     * Un dialogue en cours d'édition peut ne pas encore posséder de node de départ.
     *
     * @return node de départ s'il existe
     */
    public Optional<DialogueNode> getStartNode() {

        if (startNodeKey == null) {
            return Optional.empty();
        }

        return findNode(
                startNodeKey
        );
    }

    /**
     * Retourne les transitions ayant pour source le node indiqué.
     *
     * Les transitions sont retournées dans leur ordre de position.
     *
     * @param nodeKey clé du node source
     * @return transitions sortantes
     */
    public List<DialogueTransition> getTransitionsFrom(
            String nodeKey
    ) {
        if (nodeKey == null) {
            return List.of();
        }

        return transitions.stream()
                .filter(transition ->
                        transition.getSourceNodeKey()
                                .equals(nodeKey)
                )
                .sorted(
                        Comparator.comparingInt(
                                DialogueTransition::getPosition
                        )
                )
                .toList();
    }

    /**
     * Recherche une transition à partir
     * de sa clé métier.
     *
     * @param transitionKey clé de la transition
     * @return transition correspondante si elle existe
     */
    public Optional<DialogueTransition> findTransition(
            String transitionKey
    ) {
        if (transitionKey == null) {
            return Optional.empty();
        }

        return transitions.stream()
                .filter(transition ->
                        transition.getKey()
                                .equals(transitionKey)
                )
                .findFirst();
    }
}