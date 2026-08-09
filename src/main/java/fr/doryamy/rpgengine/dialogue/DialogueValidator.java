package fr.doryamy.rpgengine.dialogue;

import java.util.*;

/**
 * Vérifie la cohérence structurelle
 * d'un graphe de dialogue.
 *
 * Cette classe ne modifie jamais le dialogue
 * et ne tente jamais de corriger automatiquement
 * une structure invalide.
 *
 * Toute ambiguïté est retournée comme
 * une erreur de validation.
 */
public final class DialogueValidator {

    /**
     * Valide la structure complète d'un dialogue.
     *
     * @param dialogue dialogue à vérifier
     * @return résultat de validation
     */
    public DialogueValidationResult validate(
            Dialogue dialogue
    ) {
        List<String> errors =
                new ArrayList<>();

        validateStartNode(
                dialogue,
                errors
        );

        validateNodeKeys(
                dialogue,
                errors
        );

        validateTransitionKeys(
                dialogue,
                errors
        );

        validateTransitionPositions(
                dialogue,
                errors
        );

        validateTransitions(
                dialogue,
                errors
        );

        validateEndTransitions(
                dialogue,
                errors
        );

        validateReachability(
                dialogue,
                errors
        );

        validateAutoLoops(
                dialogue,
                errors
        );

        return new DialogueValidationResult(
                errors.isEmpty(),
                errors
        );
    }

    /**
     * Vérifie que le dialogue possède
     * un node de départ existant.
     */
    private void validateStartNode(
            Dialogue dialogue,
            List<String> errors
    ) {
        String startNodeKey =
                dialogue.getStartNodeKey();

        if (startNodeKey == null
                || startNodeKey.isBlank()) {

            errors.add(
                    "Le dialogue ne possède aucun node de départ."
            );

            return;
        }

        if (!containsNode(
                dialogue,
                startNodeKey
        )) {
            errors.add(
                    "Le node de départ '"
                            + startNodeKey
                            + "' n'existe pas."
            );
        }
    }

    /**
     * Vérifie que les clés des nodes
     * sont définies et uniques.
     */
    private void validateNodeKeys(
            Dialogue dialogue,
            List<String> errors
    ) {
        Set<String> keys =
                new HashSet<>();

        for (DialogueNode node :
                dialogue.getNodes()) {

            String key =
                    node.getKey();

            if (key.isBlank()) {
                errors.add(
                        "Un node possède une clé vide."
                );

                continue;
            }

            if (!keys.add(key)) {
                errors.add(
                        "Clé de node dupliquée : "
                                + key
                );
            }
        }
    }

    /**
     * Vérifie que les clés des transitions
     * sont définies et uniques.
     */
    private void validateTransitionKeys(
            Dialogue dialogue,
            List<String> errors
    ) {
        Set<String> keys =
                new HashSet<>();

        for (DialogueTransition transition :
                dialogue.getTransitions()) {

            String key =
                    transition.getKey();

            if (key.isBlank()) {
                errors.add(
                        "Une transition possède une clé vide."
                );

                continue;
            }

            if (!keys.add(key)) {
                errors.add(
                        "Clé de transition dupliquée : "
                                + key
                );
            }
        }
    }

    /**
     * Vérifie que deux transitions issues
     * du même node ne partagent pas
     * la même position.
     */
    private void validateTransitionPositions(
            Dialogue dialogue,
            List<String> errors
    ) {
        Map<String, Set<Integer>> positionsBySource =
                new HashMap<>();

        for (DialogueTransition transition :
                dialogue.getTransitions()) {

            String source =
                    transition.getSourceNodeKey();

            Set<Integer> positions =
                    positionsBySource.computeIfAbsent(
                            source,
                            ignored -> new HashSet<>()
                    );

            if (!positions.add(
                    transition.getPosition()
            )) {
                errors.add(
                        "Position de transition dupliquée depuis le node '"
                                + source
                                + "' : "
                                + transition.getPosition()
                );
            }
        }
    }

    /**
     * Vérifie la cohérence locale
     * de chaque transition.
     */
    private void validateTransitions(
            Dialogue dialogue,
            List<String> errors
    ) {
        Map<String, Integer> transitionCount =
                new HashMap<>();

        for (DialogueTransition transition :
                dialogue.getTransitions()) {

            String source =
                    transition.getSourceNodeKey();

            String target =
                    transition.getTargetNodeKey();

            /*
             * Node source.
             */
            if (!containsNode(
                    dialogue,
                    source
            )) {
                errors.add(
                        "La transition référence un node source inexistant : "
                                + source
                );
            }

            /*
             * Node cible.
             */
            if (transition.getType()
                    != DialogueTransitionType.END) {

                if (target == null
                        || target.isBlank()) {

                    errors.add(
                            "La transition "
                                    + transition.getType()
                                    + " depuis '"
                                    + source
                                    + "' ne possède aucune cible."
                    );

                } else if (!containsNode(
                        dialogue,
                        target
                )) {

                    errors.add(
                            "La transition depuis '"
                                    + source
                                    + "' référence un node cible inexistant : "
                                    + target
                    );
                }
            }

            /*
             * Validation selon le type.
             */
            switch (transition.getType()) {

                case AUTO -> {

                    if (transition.getLabel() != null) {
                        errors.add(
                                "Une transition AUTO ne doit pas posséder de label."
                        );
                    }
                }

                case CHOICE -> {

                    String label =
                            transition.getLabel();

                    if (label == null
                            || label.isBlank()) {

                        errors.add(
                                "Une transition CHOICE doit posséder un label."
                        );
                    }
                }

                case END -> {

                    if (target != null) {
                        errors.add(
                                "Une transition END ne doit pas posséder de cible."
                        );
                    }

                    if (transition.getLabel() != null) {
                        errors.add(
                                "Une transition END ne doit pas posséder de label."
                        );
                    }
                }
            }

            /*
             * Position.
             */
            if (transition.getPosition() <= 0) {
                errors.add(
                        "Une transition depuis '"
                                + source
                                + "' possède une position invalide : "
                                + transition.getPosition()
                );
            }

            transitionCount.merge(
                    source,
                    1,
                    Integer::sum
            );
        }

        /*
         * Chaque node doit posséder
         * au moins une transition explicite.
         */
        for (DialogueNode node :
                dialogue.getNodes()) {

            if (!transitionCount.containsKey(
                    node.getKey()
            )) {
                errors.add(
                        "Le node '"
                                + node.getKey()
                                + "' ne possède aucune transition."
                );
            }
        }
    }

    /**
     * Vérifie qu'au moins une fin explicite
     * existe dans le dialogue.
     */
    private void validateEndTransitions(
            Dialogue dialogue,
            List<String> errors
    ) {
        boolean hasEnd =
                dialogue.getTransitions()
                        .stream()
                        .anyMatch(
                                transition ->
                                        transition.getType()
                                                == DialogueTransitionType.END
                        );

        if (!hasEnd) {
            errors.add(
                    "Le dialogue ne possède aucune transition END."
            );
        }
    }

    /**
     * Vérifie que tous les nodes sont accessibles
     * depuis le node de départ.
     */
    private void validateReachability(
            Dialogue dialogue,
            List<String> errors
    ) {
        String start =
                dialogue.getStartNodeKey();

        if (start == null
                || !containsNode(
                dialogue,
                start
        )) {
            return;
        }

        Set<String> visited =
                new HashSet<>();

        visit(
                dialogue,
                start,
                visited
        );

        for (DialogueNode node :
                dialogue.getNodes()) {

            if (!visited.contains(
                    node.getKey()
            )) {
                errors.add(
                        "Le node '"
                                + node.getKey()
                                + "' est inaccessible depuis le node de départ."
                );
            }
        }
    }

    /**
     * Parcourt récursivement le graphe.
     */
    private void visit(
            Dialogue dialogue,
            String nodeKey,
            Set<String> visited
    ) {
        if (!visited.add(nodeKey)) {
            return;
        }

        for (DialogueTransition transition :
                dialogue.getTransitions()) {

            if (!transition
                    .getSourceNodeKey()
                    .equals(nodeKey)) {

                continue;
            }

            if (transition.getType()
                    == DialogueTransitionType.END) {

                continue;
            }

            String target =
                    transition.getTargetNodeKey();

            if (target != null
                    && containsNode(
                    dialogue,
                    target
            )) {

                visit(
                        dialogue,
                        target,
                        visited
                );
            }
        }
    }

    /**
     * Détecte les cycles constitués exclusivement
     * de transitions AUTO.
     *
     * Une boucle contenant un choix joueur
     * reste valide puisqu'elle nécessite
     * une interaction explicite.
     */
    private void validateAutoLoops(
            Dialogue dialogue,
            List<String> errors
    ) {
        Set<String> visited =
                new HashSet<>();

        Set<String> recursionStack =
                new HashSet<>();

        for (DialogueNode node :
                dialogue.getNodes()) {

            if (hasAutoCycle(
                    dialogue,
                    node.getKey(),
                    visited,
                    recursionStack
            )) {
                errors.add(
                        "Une boucle infinie de transitions AUTO a été détectée."
                );

                return;
            }
        }
    }

    /**
     * Recherche récursivement un cycle AUTO.
     */
    private boolean hasAutoCycle(
            Dialogue dialogue,
            String nodeKey,
            Set<String> visited,
            Set<String> recursionStack
    ) {
        if (recursionStack.contains(
                nodeKey
        )) {
            return true;
        }

        if (visited.contains(
                nodeKey
        )) {
            return false;
        }

        visited.add(nodeKey);
        recursionStack.add(nodeKey);

        for (DialogueTransition transition :
                dialogue.getTransitions()) {

            if (!transition
                    .getSourceNodeKey()
                    .equals(nodeKey)) {

                continue;
            }

            if (transition.getType()
                    != DialogueTransitionType.AUTO) {

                continue;
            }

            String target =
                    transition.getTargetNodeKey();

            if (target != null
                    && containsNode(
                    dialogue,
                    target
            )
                    && hasAutoCycle(
                    dialogue,
                    target,
                    visited,
                    recursionStack
            )) {

                return true;
            }
        }

        recursionStack.remove(
                nodeKey
        );

        return false;
    }

    /**
     * Vérifie l'existence d'un node.
     */
    private boolean containsNode(
            Dialogue dialogue,
            String key
    ) {
        return dialogue.findNode(
                key
        ).isPresent();
    }
}