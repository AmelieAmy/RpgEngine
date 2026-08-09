package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.condition.expression.ExpressionParser;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fournit les opérations métier permettant
 * d'administrer les dialogues de RPGEngine.
 *
 * Cette classe constitue le point d'entrée
 * commun des outils d'administration :
 *
 * - commandes ;
 * - interfaces graphiques ;
 * - futures intégrations.
 *
 * Elle manipule les objets métier puis délègue
 * leur persistance à DialogueRepository.
 */
public final class DialogueService {

    private final DialogueRepository repository;
    private final ExpressionParser expressionParser;

    /**
     * Construit le service de dialogues.
     *
     * @param repository repository des dialogues
     */
    public DialogueService(
            DialogueRepository repository,
            ExpressionParser expressionParser
    ) {
        this.repository = repository;
        this.expressionParser = expressionParser;
    }

    /**
     * Recherche un dialogue.
     *
     * @param key clé métier du dialogue
     * @return dialogue correspondant s'il existe
     */
    public Optional<Dialogue> find(
            String key
    ) {
        return repository.findByKey(
                key
        );
    }

    /**
     * Retourne tous les dialogues disponibles.
     *
     * @return liste des dialogues
     */
    public List<Dialogue> findAll() {
        return repository.findAll();
    }

    /**
     * Crée un nouveau dialogue vide.
     *
     * Aucun node de départ n'est défini
     * lors de la création.
     *
     * @param key clé métier
     * @param name nom lisible
     *
     * @return résultat de l'opération
     */
    public CommandResult create(
            String key,
            String name
    ) {
        if (key == null || key.isBlank()) {
            return CommandResult.failure(
                    "La clé du dialogue ne peut pas être vide."
            );
        }

        if (name == null || name.isBlank()) {
            return CommandResult.failure(
                    "Le nom du dialogue ne peut pas être vide."
            );
        }

        if (repository.exists(key)) {
            return CommandResult.failure(
                    "Un dialogue portant la clé '"
                            + key
                            + "' existe déjà."
            );
        }

        boolean created =
                repository.create(
                        key,
                        name
                );

        if (!created) {
            return CommandResult.failure(
                    "Impossible de créer le dialogue '"
                            + key
                            + "'."
            );
        }

        RpgLogger.debug(
                "Dialogue créé : "
                        + key
        );

        return CommandResult.success(
                "Dialogue créé : "
                        + key
        );
    }

    /**
     * Supprime un dialogue complet.
     *
     * @param key clé métier
     * @return résultat de l'opération
     */
    public CommandResult delete(
            String key
    ) {
        if (key == null || key.isBlank()) {
            return CommandResult.failure(
                    "La clé du dialogue ne peut pas être vide."
            );
        }

        if (!repository.exists(key)) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + key
            );
        }

        boolean deleted =
                repository.delete(
                        key
                );

        if (!deleted) {
            return CommandResult.failure(
                    "Impossible de supprimer le dialogue '"
                            + key
                            + "'."
            );
        }

        RpgLogger.debug(
                "Dialogue supprimé : "
                        + key
        );

        return CommandResult.success(
                "Dialogue supprimé : "
                        + key
        );
    }

    /**
     * Ajoute un node à un dialogue.
     *
     * @param dialogueKey clé du dialogue
     * @param nodeKey clé du node
     * @param text texte narratif
     *
     * @return résultat de l'opération
     */
    public CommandResult addNode(
            String dialogueKey,
            String nodeKey,
            String text
    ) {
        Optional<Dialogue> result =
                repository.findByKey(
                        dialogueKey
                );

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + dialogueKey
            );
        }

        if (nodeKey == null
                || nodeKey.isBlank()) {

            return CommandResult.failure(
                    "La clé du node ne peut pas être vide."
            );
        }

        if (text == null
                || text.isBlank()) {

            return CommandResult.failure(
                    "Le texte du node ne peut pas être vide."
            );
        }

        Dialogue dialogue =
                result.get();

        boolean alreadyExists =
                dialogue.getNodes()
                        .stream()
                        .anyMatch(node ->
                                node.getKey()
                                        .equals(nodeKey)
                        );

        if (alreadyExists) {
            return CommandResult.failure(
                    "Un node portant la clé '"
                            + nodeKey
                            + "' existe déjà dans le dialogue '"
                            + dialogueKey
                            + "'."
            );
        }

        List<DialogueNode> nodes =
                new ArrayList<>(
                        dialogue.getNodes()
                );

        nodes.add(
                new DialogueNode(
                        nodeKey,
                        text
                )
        );

        Dialogue updatedDialogue =
                new Dialogue(
                        dialogue.getKey(),
                        dialogue.getName(),
                        dialogue.getStartNodeKey(),
                        nodes,
                        dialogue.getTransitions()
                );

        if (!repository.save(
                updatedDialogue
        )) {
            return CommandResult.failure(
                    "Impossible d'ajouter le node '"
                            + nodeKey
                            + "'."
            );
        }

        RpgLogger.debug(
                "Node ajouté : "
                        + dialogueKey
                        + " | "
                        + nodeKey
        );

        return CommandResult.success(
                "Node ajouté : "
                        + nodeKey
        );
    }

    /**
     * Définit le node de départ
     * d'un dialogue.
     *
     * @param dialogueKey clé du dialogue
     * @param nodeKey clé du node de départ
     *
     * @return résultat de l'opération
     */
    public CommandResult setStartNode(
            String dialogueKey,
            String nodeKey
    ) {
        Optional<Dialogue> result =
                repository.findByKey(
                        dialogueKey
                );

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + dialogueKey
            );
        }

        Dialogue dialogue =
                result.get();

        boolean nodeExists =
                dialogue.getNodes()
                        .stream()
                        .anyMatch(node ->
                                node.getKey()
                                        .equals(nodeKey)
                        );

        if (!nodeExists) {
            return CommandResult.failure(
                    "Node introuvable : "
                            + nodeKey
            );
        }

        Dialogue updatedDialogue =
                new Dialogue(
                        dialogue.getKey(),
                        dialogue.getName(),
                        nodeKey,
                        dialogue.getNodes(),
                        dialogue.getTransitions()
                );

        if (!repository.save(
                updatedDialogue
        )) {
            return CommandResult.failure(
                    "Impossible de définir le node de départ."
            );
        }

        RpgLogger.debug(
                "Node de départ défini : "
                        + dialogueKey
                        + " | "
                        + nodeKey
        );

        return CommandResult.success(
                "Node de départ défini : "
                        + nodeKey
        );
    }

    /**
     * Modifie le texte d'un node.
     *
     * @param dialogueKey clé du dialogue
     * @param nodeKey clé du node
     * @param text nouveau texte
     *
     * @return résultat de l'opération
     */
    public CommandResult updateNodeText(
            String dialogueKey,
            String nodeKey,
            String text
    ) {
        Optional<Dialogue> result =
                repository.findByKey(
                        dialogueKey
                );

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + dialogueKey
            );
        }

        if (text == null
                || text.isBlank()) {

            return CommandResult.failure(
                    "Le texte du node ne peut pas être vide."
            );
        }

        Dialogue dialogue =
                result.get();

        List<DialogueNode> nodes =
                new ArrayList<>();

        boolean found = false;

        for (DialogueNode node :
                dialogue.getNodes()) {

            if (node.getKey()
                    .equals(nodeKey)) {

                nodes.add(
                        new DialogueNode(
                                nodeKey,
                                text
                        )
                );

                found = true;

            } else {
                nodes.add(node);
            }
        }

        if (!found) {
            return CommandResult.failure(
                    "Node introuvable : "
                            + nodeKey
            );
        }

        Dialogue updatedDialogue =
                new Dialogue(
                        dialogue.getKey(),
                        dialogue.getName(),
                        dialogue.getStartNodeKey(),
                        nodes,
                        dialogue.getTransitions()
                );

        if (!repository.save(
                updatedDialogue
        )) {
            return CommandResult.failure(
                    "Impossible de modifier le node '"
                            + nodeKey
                            + "'."
            );
        }

        return CommandResult.success(
                "Node modifié : "
                        + nodeKey
        );
    }

    /**
     * Ajoute une transition minimale à un dialogue.
     *
     * La transition créée est de type END.
     * Sa position est calculée automatiquement
     * à partir des transitions déjà présentes
     * sur le node source.
     *
     * @param dialogueKey clé du dialogue
     * @param sourceNodeKey clé du node source
     * @param transitionKey clé métier de la transition
     *
     * @return résultat de l'opération
     */
    public CommandResult addTransition(
            String dialogueKey,
            String sourceNodeKey,
            String transitionKey
    ) {
        Optional<Dialogue> result =
                repository.findByKey(
                        dialogueKey
                );

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + dialogueKey
            );
        }

        if (sourceNodeKey == null
                || sourceNodeKey.isBlank()) {

            return CommandResult.failure(
                    "La clé du node source ne peut pas être vide."
            );
        }

        if (transitionKey == null
                || transitionKey.isBlank()) {

            return CommandResult.failure(
                    "La clé de la transition ne peut pas être vide."
            );
        }

        Dialogue dialogue =
                result.get();

        if (dialogue.findNode(
                sourceNodeKey
        ).isEmpty()) {

            return CommandResult.failure(
                    "Node source introuvable : "
                            + sourceNodeKey
            );
        }

        if (dialogue.findTransition(
                transitionKey
        ).isPresent()) {

            return CommandResult.failure(
                    "Une transition portant la clé '"
                            + transitionKey
                            + "' existe déjà dans le dialogue '"
                            + dialogueKey
                            + "'."
            );
        }

        int nextPosition =
                dialogue.getTransitionsFrom(
                                sourceNodeKey
                        )
                        .stream()
                        .mapToInt(
                                DialogueTransition::getPosition
                        )
                        .max()
                        .orElse(0)
                        + 1;

        List<DialogueTransition> transitions =
                new ArrayList<>(
                        dialogue.getTransitions()
                );

        transitions.add(
                new DialogueTransition(
                        transitionKey,
                        sourceNodeKey,
                        null,
                        DialogueTransitionType.END,
                        null,
                        nextPosition,
                        List.of(),
                        List.of()
                )
        );

        Dialogue updatedDialogue =
                new Dialogue(
                        dialogue.getKey(),
                        dialogue.getName(),
                        dialogue.getStartNodeKey(),
                        dialogue.getNodes(),
                        transitions
                );

        if (!repository.save(
                updatedDialogue
        )) {
            return CommandResult.failure(
                    "Impossible d'ajouter la transition '"
                            + transitionKey
                            + "'."
            );
        }

        RpgLogger.debug(
                "Transition ajoutée : "
                        + dialogueKey
                        + " | "
                        + transitionKey
                        + " | source="
                        + sourceNodeKey
        );

        return CommandResult.success(
                "Transition ajoutée : "
                        + transitionKey
        );
    }

    /**
     * Configure une transition existante.
     *
     * La configuration est appliquée de manière atomique
     * afin qu'aucun état intermédiaire invalide
     * ne soit sauvegardé.
     *
     * @param dialogueKey clé du dialogue
     * @param transitionKey clé de la transition
     * @param type type de transition
     * @param targetNodeKey node cible, null pour END
     * @param label label du choix, uniquement pour CHOICE
     *
     * @return résultat de l'opération
     */
    public CommandResult setTransition(
            String dialogueKey,
            String transitionKey,
            DialogueTransitionType type,
            String targetNodeKey,
            String label
    ) {
        Optional<Dialogue> result =
                repository.findByKey(
                        dialogueKey
                );

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + dialogueKey
            );
        }

        if (transitionKey == null
                || transitionKey.isBlank()) {

            return CommandResult.failure(
                    "La clé de la transition ne peut pas être vide."
            );
        }

        if (type == null) {
            return CommandResult.failure(
                    "Le type de transition ne peut pas être null."
            );
        }

        Dialogue dialogue =
                result.get();

        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(
                        transitionKey
                );

        if (transitionResult.isEmpty()) {
            return CommandResult.failure(
                    "Transition introuvable : "
                            + transitionKey
            );
        }

        switch (type) {

            case AUTO -> {

                if (targetNodeKey == null
                        || targetNodeKey.isBlank()) {

                    return CommandResult.failure(
                            "Une transition AUTO doit posséder un node cible."
                    );
                }

                if (dialogue.findNode(
                        targetNodeKey
                ).isEmpty()) {

                    return CommandResult.failure(
                            "Node cible introuvable : "
                                    + targetNodeKey
                    );
                }

                label = null;
            }

            case CHOICE -> {

                if (targetNodeKey == null
                        || targetNodeKey.isBlank()) {

                    return CommandResult.failure(
                            "Une transition CHOICE doit posséder un node cible."
                    );
                }

                if (dialogue.findNode(
                        targetNodeKey
                ).isEmpty()) {

                    return CommandResult.failure(
                            "Node cible introuvable : "
                                    + targetNodeKey
                    );
                }

                if (label == null
                        || label.isBlank()) {

                    return CommandResult.failure(
                            "Une transition CHOICE doit posséder un label."
                    );
                }
            }

            case END -> {
                targetNodeKey = null;
                label = null;
            }
        }

        DialogueTransition previous =
                transitionResult.get();

        DialogueTransition updatedTransition =
                new DialogueTransition(
                        previous.getKey(),
                        previous.getSourceNodeKey(),
                        targetNodeKey,
                        type,
                        label,
                        previous.getPosition(),
                        previous.getConditions(),
                        previous.getActions()
                );

        Dialogue updatedDialogue =
                replaceTransition(
                        dialogue,
                        updatedTransition
                );

        if (!repository.save(
                updatedDialogue
        )) {
            return CommandResult.failure(
                    "Impossible de modifier la transition '"
                            + transitionKey
                            + "'."
            );
        }

        RpgLogger.debug(
                "Transition modifiée : "
                        + dialogueKey
                        + " | "
                        + transitionKey
                        + " | type="
                        + type
        );

        return CommandResult.success(
                "Transition modifiée : "
                        + transitionKey
        );
    }

    /**
     * Ajoute une action à une transition.
     *
     * La position de l'action est calculée
     * automatiquement à partir des actions
     * déjà présentes sur la transition.
     *
     * @param dialogueKey clé du dialogue
     * @param transitionKey clé de la transition
     * @param provider provider de l'action
     * @param expression expression de l'action
     *
     * @return résultat de l'opération
     */
    public CommandResult addTransitionAction(
            String dialogueKey,
            String transitionKey,
            String provider,
            String expression
    ) {
        Optional<Dialogue> result =
                repository.findByKey(
                        dialogueKey
                );

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + dialogueKey
            );
        }

        if (transitionKey == null
                || transitionKey.isBlank()) {

            return CommandResult.failure(
                    "La clé de la transition ne peut pas être vide."
            );
        }

        if (provider == null
                || provider.isBlank()) {

            return CommandResult.failure(
                    "Le provider de l'action ne peut pas être vide."
            );
        }

        if (expression == null
                || expression.isBlank()) {

            return CommandResult.failure(
                    "L'expression de l'action ne peut pas être vide."
            );
        }

        Dialogue dialogue =
                result.get();

        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(
                        transitionKey
                );

        if (transitionResult.isEmpty()) {
            return CommandResult.failure(
                    "Transition introuvable : "
                            + transitionKey
            );
        }

        DialogueTransition previous =
                transitionResult.get();

        int nextPosition =
                previous.getActions()
                        .stream()
                        .mapToInt(
                                Action::getPosition
                        )
                        .max()
                        .orElse(0)
                        + 1;

        List<Action> actions =
                new ArrayList<>(
                        previous.getActions()
                );

        actions.add(
                new Action(
                        provider,
                        expression,
                        nextPosition
                )
        );

        DialogueTransition updatedTransition =
                new DialogueTransition(
                        previous.getKey(),
                        previous.getSourceNodeKey(),
                        previous.getTargetNodeKey(),
                        previous.getType(),
                        previous.getLabel(),
                        previous.getPosition(),
                        previous.getConditions(),
                        actions
                );

        Dialogue updatedDialogue =
                replaceTransition(
                        dialogue,
                        updatedTransition
                );

        if (!repository.save(
                updatedDialogue
        )) {
            return CommandResult.failure(
                    "Impossible d'ajouter l'action à la transition '"
                            + transitionKey
                            + "'."
            );
        }

        RpgLogger.debug(
                "Action ajoutée à la transition : "
                        + dialogueKey
                        + " | "
                        + transitionKey
                        + " | provider="
                        + provider
                        + " | position="
                        + nextPosition
        );

        return CommandResult.success(
                "Action ajoutée à la transition '"
                        + transitionKey
                        + "' en position "
                        + nextPosition
                        + "."
        );
    }

    /**
     * Retourne les actions associées
     * à une transition.
     *
     * @param dialogueKey clé du dialogue
     * @param transitionKey clé de la transition
     *
     * @return liste des actions si la transition existe
     */
    public Optional<List<Action>> getTransitionActions(
            String dialogueKey,
            String transitionKey
    ) {
        Optional<Dialogue> result =
                repository.findByKey(
                        dialogueKey
                );

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Dialogue dialogue =
                result.get();

        Optional<DialogueTransition> transition =
                dialogue.findTransition(
                        transitionKey
                );

        if (transition.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                transition.get()
                        .getActions()
        );
    }

    /**
     * Supprime une action d'une transition
     * à partir de sa position.
     *
     * @param dialogueKey clé du dialogue
     * @param transitionKey clé de la transition
     * @param position position de l'action
     *
     * @return résultat de l'opération
     */
    public CommandResult removeTransitionAction(
            String dialogueKey,
            String transitionKey,
            int position
    ) {
        Optional<Dialogue> result =
                repository.findByKey(
                        dialogueKey
                );

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + dialogueKey
            );
        }

        if (position <= 0) {
            return CommandResult.failure(
                    "La position doit être supérieure à 0."
            );
        }

        Dialogue dialogue =
                result.get();

        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(
                        transitionKey
                );

        if (transitionResult.isEmpty()) {
            return CommandResult.failure(
                    "Transition introuvable : "
                            + transitionKey
            );
        }

        DialogueTransition previous =
                transitionResult.get();

        boolean exists =
                previous.getActions()
                        .stream()
                        .anyMatch(action ->
                                action.getPosition() == position
                        );

        if (!exists) {
            return CommandResult.failure(
                    "Aucune action en position "
                            + position
                            + " sur la transition '"
                            + transitionKey
                            + "'."
            );
        }

        List<Action> actions =
                previous.getActions()
                        .stream()
                        .filter(action ->
                                action.getPosition() != position
                        )
                        .toList();

        DialogueTransition updatedTransition =
                new DialogueTransition(
                        previous.getKey(),
                        previous.getSourceNodeKey(),
                        previous.getTargetNodeKey(),
                        previous.getType(),
                        previous.getLabel(),
                        previous.getPosition(),
                        previous.getConditions(),
                        actions
                );

        Dialogue updatedDialogue =
                replaceTransition(
                        dialogue,
                        updatedTransition
                );

        if (!repository.save(
                updatedDialogue
        )) {
            return CommandResult.failure(
                    "Impossible de supprimer l'action."
            );
        }

        RpgLogger.debug(
                "Action supprimée : "
                        + dialogueKey
                        + " | transition="
                        + transitionKey
                        + " | position="
                        + position
        );

        return CommandResult.success(
                "Action supprimée de la transition '"
                        + transitionKey
                        + "'."
        );
    }

    /**
     * Ajoute une condition à une transition.
     *
     * @param dialogueKey clé du dialogue
     * @param transitionKey clé de la transition
     * @param provider provider de la condition
     * @param expression expression de la condition
     *
     * @return résultat de l'opération
     */
    public CommandResult addTransitionCondition(
            String dialogueKey,
            String transitionKey,
            String provider,
            String expression
    ) {
        Optional<Dialogue> result =
                repository.findByKey(
                        dialogueKey
                );

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + dialogueKey
            );
        }

        if (transitionKey == null
                || transitionKey.isBlank()) {

            return CommandResult.failure(
                    "La clé de la transition ne peut pas être vide."
            );
        }

        if (provider == null
                || provider.isBlank()) {

            return CommandResult.failure(
                    "Le provider de la condition ne peut pas être vide."
            );
        }

        if (expression == null
                || expression.isBlank()) {

            return CommandResult.failure(
                    "L'expression de la condition ne peut pas être vide."
            );
        }

        try {
            expressionParser.parse(
                    expression
            );

        } catch (IllegalArgumentException e) {

            return CommandResult.failure(
                    "Expression de condition invalide : "
                            + e.getMessage()
            );
        }

        Dialogue dialogue =
                result.get();

        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(
                        transitionKey
                );

        if (transitionResult.isEmpty()) {
            return CommandResult.failure(
                    "Transition introuvable : "
                            + transitionKey
            );
        }

        DialogueTransition previous =
                transitionResult.get();

        List<Condition> conditions =
                new ArrayList<>(
                        previous.getConditions()
                );

        conditions.add(
                new Condition(
                        provider,
                        expression
                )
        );

        DialogueTransition updatedTransition =
                new DialogueTransition(
                        previous.getKey(),
                        previous.getSourceNodeKey(),
                        previous.getTargetNodeKey(),
                        previous.getType(),
                        previous.getLabel(),
                        previous.getPosition(),
                        conditions,
                        previous.getActions()
                );

        Dialogue updatedDialogue =
                replaceTransition(
                        dialogue,
                        updatedTransition
                );

        if (!repository.save(
                updatedDialogue
        )) {
            return CommandResult.failure(
                    "Impossible d'ajouter la condition à la transition '"
                            + transitionKey
                            + "'."
            );
        }

        RpgLogger.debug(
                "Condition ajoutée à la transition : "
                        + dialogueKey
                        + " | "
                        + transitionKey
                        + " | provider="
                        + provider
        );

        return CommandResult.success(
                "Condition ajoutée à la transition '"
                        + transitionKey
                        + "'."
        );
    }

    /**
     * Retourne les conditions associées
     * à une transition.
     *
     * @param dialogueKey clé du dialogue
     * @param transitionKey clé de la transition
     *
     * @return liste des conditions si la transition existe
     */
    public Optional<List<Condition>> getTransitionConditions(
            String dialogueKey,
            String transitionKey
    ) {
        Optional<Dialogue> result =
                repository.findByKey(
                        dialogueKey
                );

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Dialogue dialogue =
                result.get();

        Optional<DialogueTransition> transition =
                dialogue.findTransition(
                        transitionKey
                );

        if (transition.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                transition.get()
                        .getConditions()
        );
    }

    /**
     * Supprime une condition d'une transition
     * à partir de sa position dans la liste.
     *
     * @param dialogueKey clé du dialogue
     * @param transitionKey clé de la transition
     * @param position position affichée de la condition
     *
     * @return résultat de l'opération
     */
    public CommandResult removeTransitionCondition(
            String dialogueKey,
            String transitionKey,
            int position
    ) {
        Optional<Dialogue> result =
                repository.findByKey(
                        dialogueKey
                );

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + dialogueKey
            );
        }

        if (position <= 0) {
            return CommandResult.failure(
                    "La position doit être supérieure à 0."
            );
        }

        Dialogue dialogue =
                result.get();

        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(
                        transitionKey
                );

        if (transitionResult.isEmpty()) {
            return CommandResult.failure(
                    "Transition introuvable : "
                            + transitionKey
            );
        }

        DialogueTransition previous =
                transitionResult.get();

        List<Condition> previousConditions =
                previous.getConditions();

        if (position > previousConditions.size()) {
            return CommandResult.failure(
                    "Aucune condition en position "
                            + position
                            + " sur la transition '"
                            + transitionKey
                            + "'."
            );
        }

        List<Condition> conditions =
                new ArrayList<>(
                        previousConditions
                );

        conditions.remove(
                position - 1
        );

        DialogueTransition updatedTransition =
                new DialogueTransition(
                        previous.getKey(),
                        previous.getSourceNodeKey(),
                        previous.getTargetNodeKey(),
                        previous.getType(),
                        previous.getLabel(),
                        previous.getPosition(),
                        conditions,
                        previous.getActions()
                );

        Dialogue updatedDialogue =
                replaceTransition(
                        dialogue,
                        updatedTransition
                );

        if (!repository.save(
                updatedDialogue
        )) {
            return CommandResult.failure(
                    "Impossible de supprimer la condition."
            );
        }

        RpgLogger.debug(
                "Condition supprimée : "
                        + dialogueKey
                        + " | transition="
                        + transitionKey
                        + " | position="
                        + position
        );

        return CommandResult.success(
                "Condition supprimée de la transition '"
                        + transitionKey
                        + "'."
        );
    }

    /**
     * Remplace une transition dans un dialogue
     * par une nouvelle version portant la même clé métier.
     *
     * Le dialogue retourné est un nouvel agrégat immutable.
     *
     * @param dialogue dialogue d'origine
     * @param replacement nouvelle version de la transition
     *
     * @return dialogue mis à jour
     */
    private Dialogue replaceTransition(
            Dialogue dialogue,
            DialogueTransition replacement
    ) {
        List<DialogueTransition> transitions =
                dialogue.getTransitions()
                        .stream()
                        .map(transition ->
                                transition.getKey()
                                        .equals(
                                                replacement.getKey()
                                        )
                                        ? replacement
                                        : transition
                        )
                        .toList();

        return new Dialogue(
                dialogue.getKey(),
                dialogue.getName(),
                dialogue.getStartNodeKey(),
                dialogue.getNodes(),
                transitions
        );
    }
}