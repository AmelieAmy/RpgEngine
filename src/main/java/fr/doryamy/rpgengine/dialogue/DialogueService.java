package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.condition.expression.ExpressionParser;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
     * La transition créée est de type AUTO terminale.
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
                        DialogueTransitionType.AUTO,
                        null,
                        nextPosition,
                        true,
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
     * <p>La nature de la transition (AUTO / CHOICE)
     * est indépendante de sa destination :
     *
     * <ul>
     *     <li>terminale : aucun node cible ;</li>
     *     <li>non terminale : un node cible obligatoire.</li>
     * </ul>
     *
     * <p>La configuration est appliquée de manière atomique
     * afin qu'aucun état intermédiaire invalide
     * ne soit sauvegardé.
     *
     * @param dialogueKey clé du dialogue
     * @param transitionKey clé de la transition
     * @param type type de transition
     * @param targetNodeKey node cible, null si terminale
     * @param label label du choix, uniquement pour CHOICE
     * @param terminal true si la transition termine le dialogue
     *
     * @return résultat de l'opération
     */
    public CommandResult setTransition(
            String dialogueKey,
            String transitionKey,
            DialogueTransitionType type,
            String targetNodeKey,
            String label,
            boolean terminal
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

        /*
         * END est conservé temporairement dans l'enum pour
         * compatibilité binaire/source avec l'ancien code,
         * mais ne doit plus être produit par le métier.
         */
        if (type == DialogueTransitionType.END) {
            type = DialogueTransitionType.AUTO;
            terminal = true;
            targetNodeKey = null;
            label = null;
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

        /*
         * Destination.
         */
        if (terminal) {
            targetNodeKey = null;
        } else {

            if (targetNodeKey == null
                    || targetNodeKey.isBlank()) {

                return CommandResult.failure(
                        "Une transition non terminale doit posséder un node cible."
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
        }

        /*
         * Règles propres au type d'interaction.
         */
        switch (type) {

            case AUTO -> {
                /*
                 * Depuis V11, les répliques Joueur AUTO
                 * sont des objets dédiés. Le label est
                 * réservé aux transitions CHOICE.
                 */
                label = null;
            }

            case CHOICE -> {

                if (label == null
                        || label.isBlank()) {

                    return CommandResult.failure(
                            "Une transition CHOICE doit posséder un label."
                    );
                }
            }

            case END -> {
                /*
                 * Cas rendu impossible par la normalisation
                 * effectuée plus haut.
                 */
                return CommandResult.failure(
                        "Le type END n'est plus un type métier de transition."
                );
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
                        terminal,
                        previous.getConditions(),
                        previous.getActions(),
                        type == DialogueTransitionType.AUTO
                                ? previous.getPlayerReplies()
                                : List.of()
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
                        + " | terminal="
                        + terminal
        );

        return CommandResult.success(
                "Transition modifiée : "
                        + transitionKey
        );
    }

    /**
     * Compatibilité avec les appels existants.
     *
     * <p>Une cible null / vide conserve l'ancien sens END
     * en créant désormais une transition terminale.
     */
    public CommandResult setTransition(
            String dialogueKey,
            String transitionKey,
            DialogueTransitionType type,
            String targetNodeKey,
            String label
    ) {
        boolean terminal =
                type == DialogueTransitionType.END;

        return setTransition(
                dialogueKey,
                transitionKey,
                type,
                targetNodeKey,
                label,
                terminal
        );
    }

    /**
     * Termine explicitement une transition existante.
     *
     * <p>Cette opération est volontairement dédiée à l'intention
     * de l'éditeur : le client ne choisit ni le type métier,
     * ni le label, ni les autres données de la transition.
     * Le serveur conserve l'état existant et remplace uniquement
     * sa destination par une fin de dialogue.
     *
     * @param dialogueKey clé du dialogue
     * @param transitionKey clé de la transition
     *
     * @return résultat de l'opération
     */
    public CommandResult setTransitionTerminal(
            String dialogueKey,
            String transitionKey
    ) {
        Optional<Dialogue> result = repository.findByKey(dialogueKey);

        if (result.isEmpty()) {
            return CommandResult.failure("Dialogue introuvable : " + dialogueKey);
        }

        Dialogue dialogue = result.get();
        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(transitionKey);

        if (transitionResult.isEmpty()) {
            return CommandResult.failure("Transition introuvable : " + transitionKey);
        }

        DialogueTransition transition = transitionResult.get();

        if (transition.isTerminal()) {
            return CommandResult.success(
                    "La transition '" + transitionKey + "' est déjà terminale."
            );
        }

        DialogueTransition terminalTransition =
                new DialogueTransition(
                        transition.getKey(),
                        transition.getSourceNodeKey(),
                        null,
                        transition.getType() == DialogueTransitionType.END
                                ? DialogueTransitionType.AUTO
                                : transition.getType(),
                        transition.getType() == DialogueTransitionType.END
                                ? null
                                : transition.getLabel(),
                        transition.getPosition(),
                        true,
                        transition.getConditions(),
                        transition.getActions(),
                        transition.getType() == DialogueTransitionType.AUTO
                                ? transition.getPlayerReplies()
                                : List.of()
                );

        Dialogue updatedDialogue =
                pruneUnreachableStructuralNodes(
                        replaceTransition(
                                dialogue,
                                terminalTransition
                        )
                );

        if (!repository.save(updatedDialogue)) {
            return CommandResult.failure(
                    "Impossible de terminer la transition '" + transitionKey + "'."
            );
        }

        return CommandResult.success(
                "Fin de dialogue définie sur la transition '" + transitionKey + "'."
        );
    }

    /**
     * Insère une nouvelle réplique PNJ dans la structure du dialogue.
     *
     * <p>L'opération est serveur-authoritative : le client fournit uniquement
     * le point structurel sélectionné et le texte de la nouvelle réplique.
     * Le service reconstruit ensuite le graphe sans perdre les conditions,
     * actions, répliques Joueur ni la destination terminale existante.
     */
    public CommandResult insertNpcReply(
            String dialogueKey,
            String transitionKey,
            DialogueInsertionKind insertionKind,
            int position,
            String text
    ) {
        if (text == null || text.isBlank()) {
            return CommandResult.failure(
                    "La réplique PNJ ne peut pas être vide."
            );
        }

        if (insertionKind == null) {
            return CommandResult.failure(
                    "Le point d'insertion PNJ ne peut pas être null."
            );
        }

        if (position < 0) {
            return CommandResult.failure(
                    "La position d'insertion ne peut pas être négative."
            );
        }

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

        DialogueTransition transition =
                transitionResult.get();

        if (insertionKind == DialogueInsertionKind.AFTER_CHOICE
                && transition.getType() != DialogueTransitionType.CHOICE) {

            return CommandResult.failure(
                    "AFTER_CHOICE nécessite une transition CHOICE."
            );
        }

        if (insertionKind == DialogueInsertionKind.AFTER_PLAYER_REPLY) {
            if (transition.getType() != DialogueTransitionType.AUTO) {
                return CommandResult.failure(
                        "AFTER_PLAYER_REPLY nécessite une transition AUTO."
                );
            }

            boolean replyExists =
                    transition.getPlayerReplies()
                            .stream()
                            .anyMatch(reply ->
                                    reply.getPosition() == position
                            );

            if (!replyExists) {
                return CommandResult.failure(
                        "Aucune réplique Joueur en position "
                                + position
                                + " sur la transition '"
                                + transitionKey
                                + "'."
                );
            }
        }

        if ((insertionKind == DialogueInsertionKind.AFTER_SOURCE
                || insertionKind == DialogueInsertionKind.BRANCH_START
                || insertionKind == DialogueInsertionKind.AFTER_CHOICE)
                && position != 0) {

            return CommandResult.failure(
                    "Ce point d'insertion n'accepte pas de position Joueur."
            );
        }

        boolean beforeTransitionContent =
                insertionKind == DialogueInsertionKind.BRANCH_START
                        || (insertionKind == DialogueInsertionKind.AFTER_SOURCE
                        && (transition.getType() == DialogueTransitionType.CHOICE
                        || !transition.getPlayerReplies().isEmpty()));

        Dialogue updatedDialogue =
                beforeTransitionContent
                        ? insertNpcBeforeTransitionContent(
                        dialogue,
                        transition,
                        text
                )
                        : insertNpcInsideOrAfterTransition(
                        dialogue,
                        transition,
                        insertionKind,
                        position,
                        text
                );

        if (updatedDialogue == null) {
            return CommandResult.failure(
                    "Le point d'insertion PNJ est incompatible avec la transition '"
                            + transitionKey
                            + "'."
            );
        }

        if (!repository.save(
                updatedDialogue
        )) {
            return CommandResult.failure(
                    "Impossible d'insérer la réplique PNJ."
            );
        }

        RpgLogger.debug(
                "Réplique PNJ insérée : "
                        + dialogueKey
                        + " | transition="
                        + transitionKey
                        + " | point="
                        + insertionKind
                        + " | position="
                        + position
        );

        return CommandResult.success(
                "Réplique PNJ ajoutée."
        );
    }

    /**
     * Insère le nouveau PNJ avant le contenu métier de la transition.
     *
     * <p>La transition originale conserve sa clé et toutes ses données ;
     * seule sa source devient le nouveau node. Une transition AUTO neutre
     * est créée avant elle. Cette stratégie préserve l'identité métier
     * de la transition originale.
     */
    private Dialogue insertNpcBeforeTransitionContent(
            Dialogue dialogue,
            DialogueTransition transition,
            String text
    ) {
        String nodeKey =
                generateUniqueNodeKey(
                        dialogue,
                        transition.getKey() + "_npc"
                );

        String prefixTransitionKey =
                generateUniqueTransitionKey(
                        dialogue,
                        transition.getKey() + "_before_npc"
                );

        DialogueNode npcNode =
                new DialogueNode(
                        nodeKey,
                        text
                );

        DialogueTransition prefixTransition =
                new DialogueTransition(
                        prefixTransitionKey,
                        transition.getSourceNodeKey(),
                        nodeKey,
                        DialogueTransitionType.AUTO,
                        null,
                        transition.getPosition(),
                        false,
                        transition.getConditions(),
                        List.of(),
                        List.of()
                );

        DialogueTransition movedTransition =
                new DialogueTransition(
                        transition.getKey(),
                        nodeKey,
                        transition.getTargetNodeKey(),
                        transition.getType(),
                        transition.getLabel(),
                        1,
                        transition.isTerminal(),
                        List.of(),
                        transition.getActions(),
                        transition.getPlayerReplies()
                );

        List<DialogueNode> nodes =
                new ArrayList<>(
                        dialogue.getNodes()
                );

        nodes.add(
                npcNode
        );

        List<DialogueTransition> transitions =
                new ArrayList<>();

        for (DialogueTransition current :
                dialogue.getTransitions()) {

            if (current.getKey().equals(
                    transition.getKey()
            )) {
                transitions.add(
                        prefixTransition
                );
                transitions.add(
                        movedTransition
                );
            } else {
                transitions.add(
                        current
                );
            }
        }

        return new Dialogue(
                dialogue.getKey(),
                dialogue.getName(),
                dialogue.getStartNodeKey(),
                nodes,
                transitions
        );
    }

    /**
     * Insère un PNJ après une partie déjà exécutée de la transition.
     *
     * <p>La transition originale conserve sa clé. Sa destination devient
     * le nouveau PNJ, puis une nouvelle transition AUTO reprend la suite
     * de l'ancien chemin.
     */
    private Dialogue insertNpcInsideOrAfterTransition(
            Dialogue dialogue,
            DialogueTransition transition,
            DialogueInsertionKind insertionKind,
            int position,
            String text
    ) {
        if (insertionKind == DialogueInsertionKind.AFTER_CHOICE
                && transition.getType() != DialogueTransitionType.CHOICE) {
            return null;
        }

        if (insertionKind == DialogueInsertionKind.AFTER_PLAYER_REPLY
                && transition.getType() != DialogueTransitionType.AUTO) {
            return null;
        }

        String nodeKey =
                generateUniqueNodeKey(
                        dialogue,
                        transition.getKey() + "_npc"
                );

        String continuationTransitionKey =
                generateUniqueTransitionKey(
                        dialogue,
                        transition.getKey() + "_after_npc"
                );

        List<DialoguePlayerReply> sortedReplies =
                transition.getPlayerReplies()
                        .stream()
                        .sorted((left, right) ->
                                Integer.compare(
                                        left.getPosition(),
                                        right.getPosition()
                                )
                        )
                        .toList();

        List<DialoguePlayerReply> firstReplies =
                List.of();

        List<DialoguePlayerReply> continuationReplies =
                List.of();

        List<Action> firstActions =
                transition.getActions();

        List<Action> continuationActions =
                List.of();

        if (insertionKind == DialogueInsertionKind.AFTER_PLAYER_REPLY) {
            List<DialoguePlayerReply> before =
                    sortedReplies.stream()
                            .filter(reply ->
                                    reply.getPosition() <= position
                            )
                            .toList();

            List<DialoguePlayerReply> after =
                    sortedReplies.stream()
                            .filter(reply ->
                                    reply.getPosition() > position
                            )
                            .toList();

            firstReplies =
                    normalizePlayerReplyPositions(
                            before
                    );

            continuationReplies =
                    normalizePlayerReplyPositions(
                            after
                    );

            if (!continuationReplies.isEmpty()) {
                firstActions =
                        List.of();

                continuationActions =
                        transition.getActions();
            }
        } else if (insertionKind == DialogueInsertionKind.AFTER_SOURCE
                && transition.getType() == DialogueTransitionType.AUTO
                && !transition.getPlayerReplies().isEmpty()) {

            firstReplies =
                    List.of();

            continuationReplies =
                    normalizePlayerReplyPositions(
                            sortedReplies
                    );

            firstActions =
                    List.of();

            continuationActions =
                    transition.getActions();
        } else {
            firstReplies =
                    normalizePlayerReplyPositions(
                            sortedReplies
                    );
        }

        DialogueNode npcNode =
                new DialogueNode(
                        nodeKey,
                        text
                );

        DialogueTransition firstTransition =
                new DialogueTransition(
                        transition.getKey(),
                        transition.getSourceNodeKey(),
                        nodeKey,
                        transition.getType(),
                        transition.getLabel(),
                        transition.getPosition(),
                        false,
                        transition.getConditions(),
                        firstActions,
                        firstReplies
                );

        DialogueTransition continuationTransition =
                new DialogueTransition(
                        continuationTransitionKey,
                        nodeKey,
                        transition.getTargetNodeKey(),
                        DialogueTransitionType.AUTO,
                        null,
                        1,
                        transition.isTerminal(),
                        List.of(),
                        continuationActions,
                        continuationReplies
                );

        List<DialogueNode> nodes =
                new ArrayList<>(
                        dialogue.getNodes()
                );

        nodes.add(
                npcNode
        );

        List<DialogueTransition> transitions =
                new ArrayList<>();

        for (DialogueTransition current :
                dialogue.getTransitions()) {

            if (current.getKey().equals(
                    transition.getKey()
            )) {
                transitions.add(
                        firstTransition
                );
                transitions.add(
                        continuationTransition
                );
            } else {
                transitions.add(
                        current
                );
            }
        }

        return new Dialogue(
                dialogue.getKey(),
                dialogue.getName(),
                dialogue.getStartNodeKey(),
                nodes,
                transitions
        );
    }

    private String generateUniqueNodeKey(
            Dialogue dialogue,
            String baseKey
    ) {
        String candidate =
                baseKey;

        int suffix =
                2;

        while (dialogue.findNode(
                candidate
        ).isPresent()) {
            candidate =
                    baseKey
                            + "_"
                            + suffix++;
        }

        return candidate;
    }

    private String generateUniqueTransitionKey(
            Dialogue dialogue,
            String baseKey
    ) {
        String candidate =
                baseKey;

        int suffix =
                2;

        while (dialogue.findTransition(
                candidate
        ).isPresent()) {
            candidate =
                    baseKey
                            + "_"
                            + suffix++;
        }

        return candidate;
    }


    /**
     * Crée ou étend un embranchement à un point narratif précis.
     *
     * <p>Un embranchement est désormais porté par un node STRUCTURAL
     * invisible. Une nouvelle branche ouverte pointe vers un node
     * STRUCTURAL sans sortie : elle est sauvegardable mais reste
     * invalide pour le runtime tant que le créateur ne la poursuit
     * pas ou ne la termine pas explicitement.
     */
    public CommandResult createBranch(
            String dialogueKey,
            String transitionKey,
            DialogueInsertionKind insertionKind,
            int position,
            String existingChoiceLabel,
            String newChoiceLabel
    ) {
        if (insertionKind == null) {
            return CommandResult.failure(
                    "Le point d'insertion de l'embranchement ne peut pas être null."
            );
        }

        if (newChoiceLabel == null || newChoiceLabel.isBlank()) {
            return CommandResult.failure(
                    "Le nouveau choix de l'embranchement ne peut pas être vide."
            );
        }

        if (position < 0) {
            return CommandResult.failure(
                    "La position d'insertion ne peut pas être négative."
            );
        }

        Optional<Dialogue> result = repository.findByKey(dialogueKey);
        if (result.isEmpty()) {
            return CommandResult.failure("Dialogue introuvable : " + dialogueKey);
        }

        Dialogue dialogue = result.get();
        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(transitionKey);

        if (transitionResult.isEmpty()) {
            return CommandResult.failure("Transition introuvable : " + transitionKey);
        }

        DialogueTransition anchor = transitionResult.get();
        String normalizedNewLabel = newChoiceLabel.trim();

        Dialogue updatedDialogue;

        if (insertionKind == DialogueInsertionKind.BRANCH_EXPAND) {
            updatedDialogue = expandStructuralBranch(
                    dialogue,
                    anchor,
                    normalizedNewLabel
            );
        } else {
            String normalizedExistingLabel =
                    existingChoiceLabel == null
                            ? ""
                            : existingChoiceLabel.trim();

            updatedDialogue = insertStructuralBranch(
                    dialogue,
                    anchor,
                    insertionKind,
                    position,
                    normalizedExistingLabel,
                    normalizedNewLabel
            );
        }

        if (updatedDialogue == null) {
            return CommandResult.failure(
                    "Impossible de créer un embranchement à ce point du dialogue."
            );
        }

        if (!repository.save(updatedDialogue)) {
            return CommandResult.failure(
                    "Impossible de sauvegarder l'embranchement."
            );
        }

        RpgLogger.debug(
                "Embranchement créé/étendu : "
                        + dialogueKey
                        + " | transition="
                        + transitionKey
                        + " | point="
                        + insertionKind
                        + " | position="
                        + position
        );

        return CommandResult.success("Embranchement mis à jour.");
    }

    private Dialogue expandStructuralBranch(
            Dialogue dialogue,
            DialogueTransition anchor,
            String newChoiceLabel
    ) {
        Optional<DialogueNode> sourceResult =
                dialogue.findNode(anchor.getSourceNodeKey());

        if (sourceResult.isEmpty()) {
            return null;
        }

        DialogueNode source = sourceResult.get();
        List<DialogueTransition> outgoing =
                dialogue.getTransitionsFrom(source.getKey());

        if (outgoing.size() < 2
                || outgoing.stream().anyMatch(transition ->
                transition.getType() != DialogueTransitionType.CHOICE)) {
            return null;
        }

        if (hasChoiceLabel(outgoing, newChoiceLabel)) {
            return null;
        }

        String openNodeKey = generateUniqueNodeKey(
                dialogue,
                source.getKey() + "_open"
        );

        String newTransitionKey = generateUniqueTransitionKey(
                dialogue,
                anchor.getKey() + "_branch"
        );

        int nextPosition = outgoing.stream()
                .mapToInt(DialogueTransition::getPosition)
                .max()
                .orElse(0) + 1;

        List<DialogueNode> nodes = new ArrayList<>(dialogue.getNodes());
        nodes.add(DialogueNode.structural(openNodeKey));

        List<DialogueTransition> transitions =
                new ArrayList<>(dialogue.getTransitions());

        transitions.add(
                new DialogueTransition(
                        newTransitionKey,
                        source.getKey(),
                        openNodeKey,
                        DialogueTransitionType.CHOICE,
                        newChoiceLabel,
                        nextPosition,
                        false,
                        List.of(),
                        List.of(),
                        List.of()
                )
        );

        return copyDialogue(dialogue, nodes, transitions);
    }

    private Dialogue insertStructuralBranch(
            Dialogue dialogue,
            DialogueTransition transition,
            DialogueInsertionKind insertionKind,
            int position,
            String existingChoiceLabel,
            String newChoiceLabel
    ) {
        return switch (insertionKind) {
            case AFTER_SOURCE -> insertBranchAfterSource(
                    dialogue,
                    transition,
                    existingChoiceLabel,
                    newChoiceLabel
            );
            case AFTER_CHOICE -> insertBranchAfterChoice(
                    dialogue,
                    transition,
                    existingChoiceLabel,
                    newChoiceLabel
            );
            case AFTER_PLAYER_REPLY -> insertBranchAfterPlayerReply(
                    dialogue,
                    transition,
                    position,
                    existingChoiceLabel,
                    newChoiceLabel
            );
            default -> null;
        };
    }

    private Dialogue insertBranchAfterSource(
            Dialogue dialogue,
            DialogueTransition transition,
            String existingChoiceLabel,
            String newChoiceLabel
    ) {
        if (transition.getType() != DialogueTransitionType.AUTO
                && transition.getType() != DialogueTransitionType.CHOICE) {
            return null;
        }

        String junctionKey = generateUniqueNodeKey(
                dialogue,
                transition.getKey() + "_junction"
        );
        String openNodeKey = generateUniqueNodeKey(
                dialogue,
                transition.getKey() + "_open"
        );

        List<DialogueNode> nodes = new ArrayList<>(dialogue.getNodes());
        nodes.add(DialogueNode.structural(junctionKey));
        nodes.add(DialogueNode.structural(openNodeKey));

        List<DialogueTransition> replacements = new ArrayList<>();

        if (transition.getType() == DialogueTransitionType.CHOICE) {
            String prefixKey = generateUniqueTransitionKey(
                    dialogue,
                    transition.getKey() + "_before_branch"
            );

            DialogueTransition prefix = new DialogueTransition(
                    prefixKey,
                    transition.getSourceNodeKey(),
                    junctionKey,
                    DialogueTransitionType.AUTO,
                    null,
                    transition.getPosition(),
                    false,
                    transition.getConditions(),
                    List.of(),
                    List.of()
            );

            DialogueTransition existingBranch = new DialogueTransition(
                    transition.getKey(),
                    junctionKey,
                    transition.getTargetNodeKey(),
                    DialogueTransitionType.CHOICE,
                    transition.getLabel(),
                    1,
                    transition.isTerminal(),
                    List.of(),
                    transition.getActions(),
                    List.of()
            );

            if (existingBranch.getLabel() == null
                    || existingBranch.getLabel().isBlank()
                    || existingBranch.getLabel().equalsIgnoreCase(newChoiceLabel)) {
                return null;
            }

            replacements.add(prefix);
            replacements.add(existingBranch);
        } else {
            if (existingChoiceLabel.isBlank()
                    || existingChoiceLabel.equalsIgnoreCase(newChoiceLabel)) {
                return null;
            }

            boolean hasReplies = !transition.getPlayerReplies().isEmpty();

            DialogueTransition prefix = new DialogueTransition(
                    transition.getKey(),
                    transition.getSourceNodeKey(),
                    junctionKey,
                    DialogueTransitionType.AUTO,
                    null,
                    transition.getPosition(),
                    false,
                    transition.getConditions(),
                    hasReplies ? List.of() : transition.getActions(),
                    List.of()
            );

            replacements.add(prefix);

            if (hasReplies) {
                String continuationNodeKey = generateUniqueNodeKey(
                        dialogue,
                        transition.getKey() + "_continuation"
                );
                nodes.add(DialogueNode.structural(continuationNodeKey));

                replacements.add(
                        new DialogueTransition(
                                generateUniqueTransitionKey(
                                        dialogue,
                                        transition.getKey() + "_existing_branch"
                                ),
                                junctionKey,
                                continuationNodeKey,
                                DialogueTransitionType.CHOICE,
                                existingChoiceLabel,
                                1,
                                false,
                                List.of(),
                                List.of(),
                                List.of()
                        )
                );

                replacements.add(
                        new DialogueTransition(
                                generateUniqueTransitionKey(
                                        dialogue,
                                        transition.getKey() + "_continue"
                                ),
                                continuationNodeKey,
                                transition.getTargetNodeKey(),
                                DialogueTransitionType.AUTO,
                                null,
                                1,
                                transition.isTerminal(),
                                List.of(),
                                transition.getActions(),
                                normalizePlayerReplyPositions(
                                        transition.getPlayerReplies()
                                )
                        )
                );
            } else {
                replacements.add(
                        new DialogueTransition(
                                generateUniqueTransitionKey(
                                        dialogue,
                                        transition.getKey() + "_existing_branch"
                                ),
                                junctionKey,
                                transition.getTargetNodeKey(),
                                DialogueTransitionType.CHOICE,
                                existingChoiceLabel,
                                1,
                                transition.isTerminal(),
                                List.of(),
                                List.of(),
                                List.of()
                        )
                );
            }
        }

        replacements.add(
                new DialogueTransition(
                        generateUniqueTransitionKey(
                                dialogue,
                                transition.getKey() + "_new_branch"
                        ),
                        junctionKey,
                        openNodeKey,
                        DialogueTransitionType.CHOICE,
                        newChoiceLabel,
                        2,
                        false,
                        List.of(),
                        List.of(),
                        List.of()
                )
        );

        return replaceTransitionWithMany(
                dialogue,
                transition.getKey(),
                nodes,
                replacements
        );
    }

    private Dialogue insertBranchAfterChoice(
            Dialogue dialogue,
            DialogueTransition transition,
            String existingChoiceLabel,
            String newChoiceLabel
    ) {
        if (transition.getType() != DialogueTransitionType.CHOICE
                || existingChoiceLabel.isBlank()
                || existingChoiceLabel.equalsIgnoreCase(newChoiceLabel)) {
            return null;
        }

        String junctionKey = generateUniqueNodeKey(
                dialogue,
                transition.getKey() + "_junction"
        );
        String openNodeKey = generateUniqueNodeKey(
                dialogue,
                transition.getKey() + "_open"
        );

        List<DialogueNode> nodes = new ArrayList<>(dialogue.getNodes());
        nodes.add(DialogueNode.structural(junctionKey));
        nodes.add(DialogueNode.structural(openNodeKey));

        DialogueTransition prefix = new DialogueTransition(
                transition.getKey(),
                transition.getSourceNodeKey(),
                junctionKey,
                DialogueTransitionType.CHOICE,
                transition.getLabel(),
                transition.getPosition(),
                false,
                transition.getConditions(),
                transition.getActions(),
                List.of()
        );

        DialogueTransition existingBranch = new DialogueTransition(
                generateUniqueTransitionKey(
                        dialogue,
                        transition.getKey() + "_existing_branch"
                ),
                junctionKey,
                transition.getTargetNodeKey(),
                DialogueTransitionType.CHOICE,
                existingChoiceLabel,
                1,
                transition.isTerminal(),
                List.of(),
                List.of(),
                List.of()
        );

        DialogueTransition newBranch = new DialogueTransition(
                generateUniqueTransitionKey(
                        dialogue,
                        transition.getKey() + "_new_branch"
                ),
                junctionKey,
                openNodeKey,
                DialogueTransitionType.CHOICE,
                newChoiceLabel,
                2,
                false,
                List.of(),
                List.of(),
                List.of()
        );

        return replaceTransitionWithMany(
                dialogue,
                transition.getKey(),
                nodes,
                List.of(prefix, existingBranch, newBranch)
        );
    }

    private Dialogue insertBranchAfterPlayerReply(
            Dialogue dialogue,
            DialogueTransition transition,
            int position,
            String existingChoiceLabel,
            String newChoiceLabel
    ) {
        if (transition.getType() != DialogueTransitionType.AUTO
                || existingChoiceLabel.isBlank()
                || existingChoiceLabel.equalsIgnoreCase(newChoiceLabel)) {
            return null;
        }

        List<DialoguePlayerReply> sorted = transition.getPlayerReplies()
                .stream()
                .sorted((left, right) -> Integer.compare(
                        left.getPosition(),
                        right.getPosition()
                ))
                .toList();

        boolean exists = sorted.stream()
                .anyMatch(reply -> reply.getPosition() == position);

        if (!exists) {
            return null;
        }

        List<DialoguePlayerReply> before = normalizePlayerReplyPositions(
                sorted.stream()
                        .filter(reply -> reply.getPosition() <= position)
                        .toList()
        );

        List<DialoguePlayerReply> after = normalizePlayerReplyPositions(
                sorted.stream()
                        .filter(reply -> reply.getPosition() > position)
                        .toList()
        );

        String junctionKey = generateUniqueNodeKey(
                dialogue,
                transition.getKey() + "_junction"
        );
        String openNodeKey = generateUniqueNodeKey(
                dialogue,
                transition.getKey() + "_open"
        );

        List<DialogueNode> nodes = new ArrayList<>(dialogue.getNodes());
        nodes.add(DialogueNode.structural(junctionKey));
        nodes.add(DialogueNode.structural(openNodeKey));

        List<Action> prefixActions =
                after.isEmpty()
                        ? transition.getActions()
                        : List.of();

        DialogueTransition prefix = new DialogueTransition(
                transition.getKey(),
                transition.getSourceNodeKey(),
                junctionKey,
                DialogueTransitionType.AUTO,
                null,
                transition.getPosition(),
                false,
                transition.getConditions(),
                prefixActions,
                before
        );

        List<DialogueTransition> replacements = new ArrayList<>();
        replacements.add(prefix);

        if (after.isEmpty()) {
            replacements.add(
                    new DialogueTransition(
                            generateUniqueTransitionKey(
                                    dialogue,
                                    transition.getKey() + "_existing_branch"
                            ),
                            junctionKey,
                            transition.getTargetNodeKey(),
                            DialogueTransitionType.CHOICE,
                            existingChoiceLabel,
                            1,
                            transition.isTerminal(),
                            List.of(),
                            List.of(),
                            List.of()
                    )
            );
        } else {
            String continuationNodeKey = generateUniqueNodeKey(
                    dialogue,
                    transition.getKey() + "_continuation"
            );
            nodes.add(DialogueNode.structural(continuationNodeKey));

            replacements.add(
                    new DialogueTransition(
                            generateUniqueTransitionKey(
                                    dialogue,
                                    transition.getKey() + "_existing_branch"
                            ),
                            junctionKey,
                            continuationNodeKey,
                            DialogueTransitionType.CHOICE,
                            existingChoiceLabel,
                            1,
                            false,
                            List.of(),
                            List.of(),
                            List.of()
                    )
            );

            replacements.add(
                    new DialogueTransition(
                            generateUniqueTransitionKey(
                                    dialogue,
                                    transition.getKey() + "_continue"
                            ),
                            continuationNodeKey,
                            transition.getTargetNodeKey(),
                            DialogueTransitionType.AUTO,
                            null,
                            1,
                            transition.isTerminal(),
                            List.of(),
                            transition.getActions(),
                            after
                    )
            );
        }

        replacements.add(
                new DialogueTransition(
                        generateUniqueTransitionKey(
                                dialogue,
                                transition.getKey() + "_new_branch"
                        ),
                        junctionKey,
                        openNodeKey,
                        DialogueTransitionType.CHOICE,
                        newChoiceLabel,
                        2,
                        false,
                        List.of(),
                        List.of(),
                        List.of()
                )
        );

        return replaceTransitionWithMany(
                dialogue,
                transition.getKey(),
                nodes,
                replacements
        );
    }

    private boolean hasChoiceLabel(
            List<DialogueTransition> transitions,
            String label
    ) {
        return transitions.stream()
                .filter(transition ->
                        transition.getType() == DialogueTransitionType.CHOICE)
                .map(DialogueTransition::getLabel)
                .filter(java.util.Objects::nonNull)
                .anyMatch(existing -> existing.equalsIgnoreCase(label));
    }

    private Dialogue replaceTransitionWithMany(
            Dialogue dialogue,
            String transitionKey,
            List<DialogueNode> nodes,
            List<DialogueTransition> replacements
    ) {
        List<DialogueTransition> transitions = new ArrayList<>();

        for (DialogueTransition current : dialogue.getTransitions()) {
            if (current.getKey().equals(transitionKey)) {
                transitions.addAll(replacements);
            } else {
                transitions.add(current);
            }
        }

        return copyDialogue(dialogue, nodes, transitions);
    }

    private Dialogue copyDialogue(
            Dialogue dialogue,
            List<DialogueNode> nodes,
            List<DialogueTransition> transitions
    ) {
        return new Dialogue(
                dialogue.getKey(),
                dialogue.getName(),
                dialogue.getStartNodeKey(),
                nodes,
                transitions
        );
    }

    /**
     * Retire uniquement les nodes structurels devenus inaccessibles.
     * Les nodes narratifs ne sont jamais supprimés implicitement.
     */
    private Dialogue pruneUnreachableStructuralNodes(
            Dialogue dialogue
    ) {
        String start = dialogue.getStartNodeKey();
        if (start == null || start.isBlank()) {
            return dialogue;
        }

        java.util.Set<String> reachable = new java.util.HashSet<>();
        java.util.ArrayDeque<String> queue = new java.util.ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            if (!reachable.add(current)) {
                continue;
            }

            for (DialogueTransition transition : dialogue.getTransitionsFrom(current)) {
                if (transition.isTerminal()) {
                    continue;
                }

                String target = transition.getTargetNodeKey();
                if (target != null && !target.isBlank()) {
                    queue.addLast(target);
                }
            }
        }

        java.util.Set<String> removedStructuralKeys =
                dialogue.getNodes().stream()
                        .filter(DialogueNode::isStructural)
                        .map(DialogueNode::getKey)
                        .filter(key -> !reachable.contains(key))
                        .collect(java.util.stream.Collectors.toSet());

        if (removedStructuralKeys.isEmpty()) {
            return dialogue;
        }

        List<DialogueNode> nodes = dialogue.getNodes().stream()
                .filter(node -> !removedStructuralKeys.contains(node.getKey()))
                .toList();

        List<DialogueTransition> transitions = dialogue.getTransitions().stream()
                .filter(transition ->
                        !removedStructuralKeys.contains(
                                transition.getSourceNodeKey()
                        ))
                .toList();

        return copyDialogue(dialogue, nodes, transitions);
    }

    /**
     * Point de compatibilité avec le protocole éditeur existant.
     *
     * CHOICE : modifie le label du choix.
     * AUTO   : crée la première réplique si elle n'existe pas encore,
     *          ou modifie l'unique réplique existante.
     *
     * Lorsqu'une transition AUTO possède plusieurs répliques, la position
     * doit être fournie via l'overload dédié afin d'éviter toute ambiguïté.
     */
    public CommandResult updatePlayerReply(
            String dialogueKey,
            String transitionKey,
            String text
    ) {
        if (text == null || text.isBlank()) {
            return CommandResult.failure(
                    "La réplique du joueur ne peut pas être vide."
            );
        }

        Optional<Dialogue> result =
                repository.findByKey(dialogueKey);

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : " + dialogueKey
            );
        }

        Dialogue dialogue = result.get();

        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(transitionKey);

        if (transitionResult.isEmpty()) {
            return CommandResult.failure(
                    "Transition introuvable : " + transitionKey
            );
        }

        DialogueTransition transition =
                transitionResult.get();

        if (transition.getType()
                == DialogueTransitionType.CHOICE) {
            return setTransition(
                    dialogueKey,
                    transitionKey,
                    DialogueTransitionType.CHOICE,
                    transition.getTargetNodeKey(),
                    text,
                    transition.isTerminal()
            );
        }

        if (transition.getType()
                != DialogueTransitionType.AUTO) {
            return CommandResult.failure(
                    "La transition '"
                            + transitionKey
                            + "' ne peut pas porter de réplique Joueur."
            );
        }

        if (transition.getPlayerReplies().isEmpty()) {
            return addPlayerReply(
                    dialogueKey,
                    transitionKey,
                    text
            );
        }

        if (transition.getPlayerReplies().size() == 1) {
            return updatePlayerReply(
                    dialogueKey,
                    transitionKey,
                    transition.getPlayerReplies()
                            .get(0)
                            .getPosition(),
                    text
            );
        }

        return CommandResult.failure(
                "La transition '"
                        + transitionKey
                        + "' possède plusieurs répliques Joueur : "
                        + "la position doit être précisée."
        );
    }

    /**
     * Compatibilité avec le nom historique utilisé par le bridge actuel.
     */
    public CommandResult updateChoiceLabel(
            String dialogueKey,
            String transitionKey,
            String label
    ) {
        return updatePlayerReply(
                dialogueKey,
                transitionKey,
                label
        );
    }

    /**
     * Ajoute une réplique Joueur à la fin d'une transition AUTO.
     */
    public CommandResult addPlayerReply(
            String dialogueKey,
            String transitionKey,
            String text
    ) {
        Optional<Dialogue> result =
                repository.findByKey(dialogueKey);

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : " + dialogueKey
            );
        }

        Optional<DialogueTransition> transitionResult =
                result.get().findTransition(transitionKey);

        if (transitionResult.isEmpty()) {
            return CommandResult.failure(
                    "Transition introuvable : " + transitionKey
            );
        }

        int lastPosition =
                transitionResult.get()
                        .getPlayerReplies()
                        .stream()
                        .mapToInt(DialoguePlayerReply::getPosition)
                        .max()
                        .orElse(0);

        return insertPlayerReply(
                dialogueKey,
                transitionKey,
                lastPosition,
                text
        );
    }

    /**
     * Insère une réplique après la position indiquée.
     * afterPosition == 0 insère au début de la séquence.
     */
    public CommandResult insertPlayerReply(
            String dialogueKey,
            String transitionKey,
            int afterPosition,
            String text
    ) {
        if (text == null || text.isBlank()) {
            return CommandResult.failure(
                    "La réplique du joueur ne peut pas être vide."
            );
        }

        if (afterPosition < 0) {
            return CommandResult.failure(
                    "La position d'insertion ne peut pas être négative."
            );
        }

        Optional<Dialogue> result =
                repository.findByKey(dialogueKey);

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : " + dialogueKey
            );
        }

        Dialogue dialogue = result.get();
        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(transitionKey);

        if (transitionResult.isEmpty()) {
            return CommandResult.failure(
                    "Transition introuvable : " + transitionKey
            );
        }

        DialogueTransition previous =
                transitionResult.get();

        /*
         * Une vraie réplique Joueur placée après un CHOICE ne peut pas être
         * stockée directement sur la transition CHOICE : les playerReplies
         * appartiennent aux transitions AUTO.
         *
         * On scinde donc le chemin :
         * CHOICE -> STRUCTURAL -> AUTO(player reply) -> ancienne suite.
         */
        if (previous.getType()
                == DialogueTransitionType.CHOICE) {

            if (afterPosition != 0) {
                return CommandResult.failure(
                        "Une réplique Joueur ajoutée après un choix doit utiliser la position 0."
                );
            }

            return insertPlayerReplyAfterChoice(
                    dialogue,
                    previous,
                    text
            );
        }

        if (previous.getType()
                != DialogueTransitionType.AUTO) {
            return CommandResult.failure(
                    "Les répliques Joueur séquentielles sont réservées aux transitions AUTO."
            );
        }

        List<DialoguePlayerReply> sorted =
                previous.getPlayerReplies()
                        .stream()
                        .sorted((left, right) ->
                                Integer.compare(
                                        left.getPosition(),
                                        right.getPosition()
                                )
                        )
                        .toList();

        int insertionIndex = 0;

        if (afterPosition > 0) {
            insertionIndex = -1;

            for (int i = 0; i < sorted.size(); i++) {
                if (sorted.get(i).getPosition()
                        == afterPosition) {
                    insertionIndex = i + 1;
                    break;
                }
            }

            if (insertionIndex < 0) {
                return CommandResult.failure(
                        "Aucune réplique Joueur en position "
                                + afterPosition
                                + " sur la transition '"
                                + transitionKey
                                + "'."
                );
            }
        }

        List<DialoguePlayerReply> replies =
                new ArrayList<>(sorted);

        replies.add(
                insertionIndex,
                new DialoguePlayerReply(
                        text,
                        insertionIndex + 1,
                        List.of(),
                        List.of()
                )
        );

        replies = normalizePlayerReplyPositions(replies);

        DialogueTransition updatedTransition =
                copyTransitionWithPlayerReplies(
                        previous,
                        replies
                );

        if (!repository.save(
                replaceTransition(
                        dialogue,
                        updatedTransition
                )
        )) {
            return CommandResult.failure(
                    "Impossible d'ajouter la réplique Joueur."
            );
        }

        return CommandResult.success(
                "Réplique Joueur ajoutée à la transition '"
                        + transitionKey
                        + "'."
        );
    }

    /**
     * Modifie le texte d'une réplique Joueur sans toucher à ses règles.
     */
    public CommandResult updatePlayerReply(
            String dialogueKey,
            String transitionKey,
            int replyPosition,
            String text
    ) {
        if (text == null || text.isBlank()) {
            return CommandResult.failure(
                    "La réplique du joueur ne peut pas être vide."
            );
        }

        Optional<Dialogue> result =
                repository.findByKey(dialogueKey);

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : " + dialogueKey
            );
        }

        Dialogue dialogue = result.get();
        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(transitionKey);

        if (transitionResult.isEmpty()) {
            return CommandResult.failure(
                    "Transition introuvable : " + transitionKey
            );
        }

        DialogueTransition previous =
                transitionResult.get();

        List<DialoguePlayerReply> replies =
                new ArrayList<>();

        boolean found = false;

        for (DialoguePlayerReply reply :
                previous.getPlayerReplies()) {
            if (reply.getPosition()
                    == replyPosition) {
                replies.add(
                        new DialoguePlayerReply(
                                text,
                                reply.getPosition(),
                                reply.getConditions(),
                                reply.getActions()
                        )
                );
                found = true;
            } else {
                replies.add(reply);
            }
        }

        if (!found) {
            return CommandResult.failure(
                    "Aucune réplique Joueur en position "
                            + replyPosition
                            + " sur la transition '"
                            + transitionKey
                            + "'."
            );
        }

        DialogueTransition updatedTransition =
                copyTransitionWithPlayerReplies(
                        previous,
                        replies
                );

        if (!repository.save(
                replaceTransition(
                        dialogue,
                        updatedTransition
                )
        )) {
            return CommandResult.failure(
                    "Impossible de modifier la réplique Joueur."
            );
        }

        return CommandResult.success(
                "Réplique Joueur modifiée."
        );
    }

    /**
     * Supprime une réplique Joueur et compacte les positions restantes.
     */
    public CommandResult removePlayerReply(
            String dialogueKey,
            String transitionKey,
            int replyPosition
    ) {
        Optional<Dialogue> result =
                repository.findByKey(dialogueKey);

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : " + dialogueKey
            );
        }

        Dialogue dialogue = result.get();
        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(transitionKey);

        if (transitionResult.isEmpty()) {
            return CommandResult.failure(
                    "Transition introuvable : " + transitionKey
            );
        }

        DialogueTransition previous =
                transitionResult.get();

        boolean exists =
                previous.getPlayerReplies()
                        .stream()
                        .anyMatch(reply ->
                                reply.getPosition()
                                        == replyPosition
                        );

        if (!exists) {
            return CommandResult.failure(
                    "Aucune réplique Joueur en position "
                            + replyPosition
                            + "."
            );
        }

        List<DialoguePlayerReply> replies =
                previous.getPlayerReplies()
                        .stream()
                        .filter(reply ->
                                reply.getPosition()
                                        != replyPosition
                        )
                        .toList();

        replies = normalizePlayerReplyPositions(replies);

        DialogueTransition updatedTransition =
                copyTransitionWithPlayerReplies(
                        previous,
                        replies
                );

        if (!repository.save(
                replaceTransition(
                        dialogue,
                        updatedTransition
                )
        )) {
            return CommandResult.failure(
                    "Impossible de supprimer la réplique Joueur."
            );
        }

        return CommandResult.success(
                "Réplique Joueur supprimée."
        );
    }

    /**
     * Ajoute une condition facultative à une réplique Joueur.
     */
    public CommandResult addPlayerReplyCondition(
            String dialogueKey,
            String transitionKey,
            int replyPosition,
            String provider,
            String expression
    ) {
        if (provider == null || provider.isBlank()) {
            return CommandResult.failure(
                    "Le provider de la condition ne peut pas être vide."
            );
        }

        if (expression == null || expression.isBlank()) {
            return CommandResult.failure(
                    "L'expression de la condition ne peut pas être vide."
            );
        }

        try {
            expressionParser.parse(expression);
        } catch (IllegalArgumentException e) {
            return CommandResult.failure(
                    "Expression de condition invalide : "
                            + e.getMessage()
            );
        }

        return mutatePlayerReply(
                dialogueKey,
                transitionKey,
                replyPosition,
                reply -> {
                    List<Condition> conditions =
                            new ArrayList<>(reply.getConditions());

                    conditions.add(
                            new Condition(provider, expression)
                    );

                    return new DialoguePlayerReply(
                            reply.getText(),
                            reply.getPosition(),
                            conditions,
                            reply.getActions()
                    );
                },
                "Condition ajoutée à la réplique Joueur."
        );
    }

    public CommandResult updatePlayerReplyCondition(
            String dialogueKey,
            String transitionKey,
            int replyPosition,
            int conditionPosition,
            String provider,
            String expression
    ) {
        if (conditionPosition <= 0) {
            return CommandResult.failure(
                    "La position de la condition doit être supérieure à 0."
            );
        }

        if (provider == null || provider.isBlank()) {
            return CommandResult.failure(
                    "Le provider de la condition ne peut pas être vide."
            );
        }

        if (expression == null || expression.isBlank()) {
            return CommandResult.failure(
                    "L'expression de la condition ne peut pas être vide."
            );
        }

        try {
            expressionParser.parse(expression);
        } catch (IllegalArgumentException e) {
            return CommandResult.failure(
                    "Expression de condition invalide : "
                            + e.getMessage()
            );
        }

        return mutatePlayerReply(
                dialogueKey,
                transitionKey,
                replyPosition,
                reply -> {
                    int index = conditionPosition - 1;

                    if (index < 0
                            || index >= reply.getConditions().size()) {
                        return null;
                    }

                    List<Condition> conditions =
                            new ArrayList<>(reply.getConditions());

                    conditions.set(
                            index,
                            new Condition(provider, expression)
                    );

                    return new DialoguePlayerReply(
                            reply.getText(),
                            reply.getPosition(),
                            conditions,
                            reply.getActions()
                    );
                },
                "Condition de la réplique Joueur modifiée."
        );
    }

    public CommandResult removePlayerReplyCondition(
            String dialogueKey,
            String transitionKey,
            int replyPosition,
            int conditionPosition
    ) {
        if (conditionPosition <= 0) {
            return CommandResult.failure(
                    "La position de la condition doit être supérieure à 0."
            );
        }

        return mutatePlayerReply(
                dialogueKey,
                transitionKey,
                replyPosition,
                reply -> {
                    int index = conditionPosition - 1;

                    if (index < 0
                            || index >= reply.getConditions().size()) {
                        return null;
                    }

                    List<Condition> conditions =
                            new ArrayList<>(reply.getConditions());

                    conditions.remove(index);

                    return new DialoguePlayerReply(
                            reply.getText(),
                            reply.getPosition(),
                            conditions,
                            reply.getActions()
                    );
                },
                "Condition de la réplique Joueur supprimée."
        );
    }

    /**
     * Ajoute une action facultative à une réplique Joueur.
     */
    public CommandResult addPlayerReplyAction(
            String dialogueKey,
            String transitionKey,
            int replyPosition,
            String provider,
            String expression
    ) {
        if (provider == null || provider.isBlank()) {
            return CommandResult.failure(
                    "Le provider de l'action ne peut pas être vide."
            );
        }

        if (expression == null || expression.isBlank()) {
            return CommandResult.failure(
                    "L'expression de l'action ne peut pas être vide."
            );
        }

        return mutatePlayerReply(
                dialogueKey,
                transitionKey,
                replyPosition,
                reply -> {
                    int nextPosition =
                            reply.getActions()
                                    .stream()
                                    .mapToInt(Action::getPosition)
                                    .max()
                                    .orElse(0)
                                    + 1;

                    List<Action> actions =
                            new ArrayList<>(reply.getActions());

                    actions.add(
                            new Action(
                                    provider,
                                    expression,
                                    nextPosition
                            )
                    );

                    return new DialoguePlayerReply(
                            reply.getText(),
                            reply.getPosition(),
                            reply.getConditions(),
                            actions
                    );
                },
                "Action ajoutée à la réplique Joueur."
        );
    }

    public CommandResult updatePlayerReplyAction(
            String dialogueKey,
            String transitionKey,
            int replyPosition,
            int actionPosition,
            String provider,
            String expression
    ) {
        if (actionPosition <= 0) {
            return CommandResult.failure(
                    "La position de l'action doit être supérieure à 0."
            );
        }

        if (provider == null || provider.isBlank()) {
            return CommandResult.failure(
                    "Le provider de l'action ne peut pas être vide."
            );
        }

        if (expression == null || expression.isBlank()) {
            return CommandResult.failure(
                    "L'expression de l'action ne peut pas être vide."
            );
        }

        return mutatePlayerReply(
                dialogueKey,
                transitionKey,
                replyPosition,
                reply -> {
                    List<Action> actions =
                            new ArrayList<>();

                    boolean found = false;

                    for (Action action : reply.getActions()) {
                        if (action.getPosition()
                                == actionPosition) {
                            actions.add(
                                    new Action(
                                            provider,
                                            expression,
                                            actionPosition
                                    )
                            );
                            found = true;
                        } else {
                            actions.add(action);
                        }
                    }

                    if (!found) {
                        return null;
                    }

                    return new DialoguePlayerReply(
                            reply.getText(),
                            reply.getPosition(),
                            reply.getConditions(),
                            actions
                    );
                },
                "Action de la réplique Joueur modifiée."
        );
    }

    public CommandResult removePlayerReplyAction(
            String dialogueKey,
            String transitionKey,
            int replyPosition,
            int actionPosition
    ) {
        if (actionPosition <= 0) {
            return CommandResult.failure(
                    "La position de l'action doit être supérieure à 0."
            );
        }

        return mutatePlayerReply(
                dialogueKey,
                transitionKey,
                replyPosition,
                reply -> {
                    boolean exists =
                            reply.getActions()
                                    .stream()
                                    .anyMatch(action ->
                                            action.getPosition()
                                                    == actionPosition
                                    );

                    if (!exists) {
                        return null;
                    }

                    List<Action> actions =
                            reply.getActions()
                                    .stream()
                                    .filter(action ->
                                            action.getPosition()
                                                    != actionPosition
                                    )
                                    .toList();

                    return new DialoguePlayerReply(
                            reply.getText(),
                            reply.getPosition(),
                            reply.getConditions(),
                            actions
                    );
                },
                "Action de la réplique Joueur supprimée."
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
                        previous.isTerminal(),
                        previous.getConditions(),
                        actions,
                        previous.getPlayerReplies()
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
     * Modifie une action existante d'une transition
     * sans changer sa position dans l'ordre d'exécution.
     *
     * @param dialogueKey clé du dialogue
     * @param transitionKey clé de la transition
     * @param position position métier de l'action
     * @param provider nouveau provider
     * @param expression nouvelle expression
     *
     * @return résultat de l'opération
     */
    public CommandResult updateTransitionAction(
            String dialogueKey,
            String transitionKey,
            int position,
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

        if (position <= 0) {

            return CommandResult.failure(
                    "La position de l'action doit être supérieure à 0."
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

        boolean found =
                false;

        List<Action> actions =
                new ArrayList<>();

        for (Action action :
                previous.getActions()) {

            if (action.getPosition()
                    == position) {

                actions.add(
                        new Action(
                                provider,
                                expression,
                                position
                        )
                );

                found =
                        true;

            } else {

                actions.add(
                        action
                );
            }
        }

        if (!found) {

            return CommandResult.failure(
                    "Aucune action en position "
                            + position
                            + " sur la transition '"
                            + transitionKey
                            + "'."
            );
        }

        DialogueTransition updatedTransition =
                new DialogueTransition(
                        previous.getKey(),
                        previous.getSourceNodeKey(),
                        previous.getTargetNodeKey(),
                        previous.getType(),
                        previous.getLabel(),
                        previous.getPosition(),
                        previous.isTerminal(),
                        previous.getConditions(),
                        actions,
                        previous.getPlayerReplies()
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
                    "Impossible de modifier l'action."
            );
        }

        RpgLogger.debug(
                "Action modifiée : "
                        + dialogueKey
                        + " | transition="
                        + transitionKey
                        + " | position="
                        + position
                        + " | provider="
                        + provider
        );

        return CommandResult.success(
                "Action modifiée sur la transition '"
                        + transitionKey
                        + "'."
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
                        previous.isTerminal(),
                        previous.getConditions(),
                        actions,
                        previous.getPlayerReplies()
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
                        previous.isTerminal(),
                        conditions,
                        previous.getActions(),
                        previous.getPlayerReplies()
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
     * Modifie une condition existante d'une transition.
     *
     * <p>La position est 1-based afin de rester cohérente
     * avec les commandes d'administration existantes.
     */
    public CommandResult updateTransitionCondition(
            String dialogueKey,
            String transitionKey,
            int position,
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

        if (position <= 0) {
            return CommandResult.failure(
                    "La position de la condition doit être supérieure à 0."
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

        int index =
                position - 1;

        if (index < 0
                || index >= previous.getConditions().size()) {

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
                        previous.getConditions()
                );

        conditions.set(
                index,
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
                        previous.isTerminal(),
                        conditions,
                        previous.getActions(),
                        previous.getPlayerReplies()
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
                    "Impossible de modifier la condition."
            );
        }

        RpgLogger.debug(
                "Condition modifiée : "
                        + dialogueKey
                        + " | transition="
                        + transitionKey
                        + " | position="
                        + position
                        + " | provider="
                        + provider
        );

        return CommandResult.success(
                "Condition modifiée sur la transition '"
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
                        previous.isTerminal(),
                        conditions,
                        previous.getActions(),
                        previous.getPlayerReplies()
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
     * Supprime un élément depuis l'éditeur visuel.
     *
     * <p>Le client ne décrit jamais la mutation du graphe : il transmet
     * uniquement son intention et l'identité minimale de l'élément.
     */
    public CommandResult deleteEditorElement(
            String dialogueKey,
            String transitionKey,
            DialogueDeletionKind deletionKind,
            int position
    ) {
        if (deletionKind == null) {
            return CommandResult.failure(
                    "Le type de suppression ne peut pas être null."
            );
        }

        return switch (deletionKind) {
            case PLAYER_REPLY -> removePlayerReply(
                    dialogueKey,
                    transitionKey,
                    position
            );

            case NPC_REPLY -> removeNpcReply(
                    dialogueKey,
                    transitionKey
            );

            case CHOICE_REPLY -> removeChoiceReply(
                    dialogueKey,
                    transitionKey
            );

            case BRANCH_CHOICE -> removeBranchChoice(
                    dialogueKey,
                    transitionKey
            );

            case BRANCH -> removeWholeBranch(
                    dialogueKey,
                    transitionKey
            );
        };
    }

    /**
     * Retire une réplique PNJ sans détruire la structure qui l'entoure.
     *
     * <p>Le node narratif devient un node STRUCTURAL invisible. Cette
     * transformation est l'inverse structurel le plus sûr de l'insertion :
     * les transitions entrantes/sortantes, leurs conditions/actions et les
     * éventuelles branches restent intactes.
     */
    public CommandResult removeNpcReply(
            String dialogueKey,
            String transitionKey
    ) {
        Optional<Dialogue> result = repository.findByKey(dialogueKey);

        if (result.isEmpty()) {
            return CommandResult.failure("Dialogue introuvable : " + dialogueKey);
        }

        Dialogue dialogue = result.get();
        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(transitionKey);

        if (transitionResult.isEmpty()) {
            return CommandResult.failure("Transition introuvable : " + transitionKey);
        }

        DialogueTransition transition = transitionResult.get();
        Optional<DialogueNode> nodeResult =
                dialogue.findNode(transition.getSourceNodeKey());

        if (nodeResult.isEmpty()) {
            return CommandResult.failure(
                    "Node PNJ introuvable pour la transition '" + transitionKey + "'."
            );
        }

        DialogueNode node = nodeResult.get();

        if (node.isStructural()) {
            return CommandResult.failure(
                    "Le node ciblé n'est pas une réplique PNJ."
            );
        }

        List<DialogueNode> nodes = new ArrayList<>();

        for (DialogueNode current : dialogue.getNodes()) {
            if (current.getKey().equals(node.getKey())) {
                nodes.add(DialogueNode.structural(current.getKey()));
            } else {
                nodes.add(current);
            }
        }

        Dialogue updated = copyDialogue(
                dialogue,
                nodes,
                dialogue.getTransitions()
        );

        if (!repository.save(updated)) {
            return CommandResult.failure(
                    "Impossible de supprimer la réplique PNJ."
            );
        }

        return CommandResult.success("Réplique PNJ supprimée.");
    }

    /**
     * Retire une étape CHOICE isolée en conservant sa continuation.
     * La transition devient AUTO et son label disparaît.
     */
    public CommandResult removeChoiceReply(
            String dialogueKey,
            String transitionKey
    ) {
        Optional<Dialogue> result = repository.findByKey(dialogueKey);

        if (result.isEmpty()) {
            return CommandResult.failure("Dialogue introuvable : " + dialogueKey);
        }

        Dialogue dialogue = result.get();
        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(transitionKey);

        if (transitionResult.isEmpty()) {
            return CommandResult.failure("Transition introuvable : " + transitionKey);
        }

        DialogueTransition previous = transitionResult.get();

        if (previous.getType() != DialogueTransitionType.CHOICE) {
            return CommandResult.failure(
                    "La transition ciblée n'est pas une réplique de choix."
            );
        }

        if (dialogue.getTransitionsFrom(previous.getSourceNodeKey()).size() > 1) {
            return CommandResult.failure(
                    "Ce choix appartient à un embranchement : utilisez la suppression de branche."
            );
        }

        DialogueTransition replacement = new DialogueTransition(
                previous.getKey(),
                previous.getSourceNodeKey(),
                previous.getTargetNodeKey(),
                DialogueTransitionType.AUTO,
                null,
                previous.getPosition(),
                previous.isTerminal(),
                previous.getConditions(),
                previous.getActions(),
                List.of()
        );

        if (!repository.save(replaceTransition(dialogue, replacement))) {
            return CommandResult.failure(
                    "Impossible de supprimer la réplique de choix."
            );
        }

        return CommandResult.success("Réplique de choix supprimée.");
    }

    /**
     * Supprime une sortie d'un embranchement et tout le sous-graphe qui
     * devient réellement inaccessible depuis le start node.
     *
     * <p>Si une seule sortie subsiste, le junction est automatiquement
     * contracté : la dernière CHOICE devient AUTO, ce qui fait disparaître
     * à la fois le losange et le dernier tag Choix du graphe.
     */
    public CommandResult removeBranchChoice(
            String dialogueKey,
            String transitionKey
    ) {
        Optional<Dialogue> result = repository.findByKey(dialogueKey);

        if (result.isEmpty()) {
            return CommandResult.failure("Dialogue introuvable : " + dialogueKey);
        }

        Dialogue dialogue = result.get();
        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(transitionKey);

        if (transitionResult.isEmpty()) {
            return CommandResult.failure("Transition introuvable : " + transitionKey);
        }

        DialogueTransition selected = transitionResult.get();
        String junctionKey = selected.getSourceNodeKey();
        Optional<DialogueNode> junctionResult = dialogue.findNode(junctionKey);

        if (junctionResult.isEmpty() || !junctionResult.get().isStructural()) {
            return CommandResult.failure(
                    "La branche ciblée n'appartient pas à un junction structurel."
            );
        }

        List<DialogueTransition> outgoing = dialogue.getTransitionsFrom(junctionKey);

        if (outgoing.size() < 2
                || selected.getType() != DialogueTransitionType.CHOICE
                || outgoing.stream().anyMatch(candidate ->
                candidate.getType() != DialogueTransitionType.CHOICE)) {
            return CommandResult.failure(
                    "La transition ciblée n'appartient pas à un embranchement valide."
            );
        }

        List<DialogueTransition> transitions = dialogue.getTransitions().stream()
                .filter(candidate -> !candidate.getKey().equals(transitionKey))
                .toList();

        Dialogue updated = copyDialogue(
                dialogue,
                dialogue.getNodes(),
                transitions
        );

        updated = pruneUnreachableGraph(updated);
        updated = collapseBranchIfNeeded(updated, junctionKey);

        if (!repository.save(updated)) {
            return CommandResult.failure(
                    "Impossible de supprimer la branche '" + transitionKey + "'."
            );
        }

        return CommandResult.success("Branche supprimée.");
    }

    /**
     * Supprime tout l'embranchement et toutes les suites qui ne restent
     * accessibles par aucun autre chemin.
     *
     * <p>Cette action destructive ferme explicitement le chemin juste avant
     * l'ancien junction. Elle produit donc une vraie transition terminale,
     * plutôt qu'un dead-end implicite invalide pour le runtime.
     */
    public CommandResult removeWholeBranch(
            String dialogueKey,
            String anchorTransitionKey
    ) {
        Optional<Dialogue> result = repository.findByKey(dialogueKey);

        if (result.isEmpty()) {
            return CommandResult.failure("Dialogue introuvable : " + dialogueKey);
        }

        Dialogue dialogue = result.get();
        Optional<DialogueTransition> anchorResult =
                dialogue.findTransition(anchorTransitionKey);

        if (anchorResult.isEmpty()) {
            return CommandResult.failure(
                    "Transition d'embranchement introuvable : " + anchorTransitionKey
            );
        }

        String junctionKey = anchorResult.get().getSourceNodeKey();
        Optional<DialogueNode> junctionResult = dialogue.findNode(junctionKey);

        if (junctionResult.isEmpty() || !junctionResult.get().isStructural()) {
            return CommandResult.failure(
                    "L'embranchement ciblé n'est pas un junction structurel."
            );
        }

        List<DialogueTransition> outgoing = dialogue.getTransitionsFrom(junctionKey);

        if (outgoing.size() < 2
                || outgoing.stream().anyMatch(candidate ->
                candidate.getType() != DialogueTransitionType.CHOICE)) {
            return CommandResult.failure("Embranchement invalide.");
        }

        List<DialogueTransition> incoming = dialogue.getTransitions().stream()
                .filter(candidate -> !candidate.isTerminal())
                .filter(candidate -> junctionKey.equals(candidate.getTargetNodeKey()))
                .toList();

        if (incoming.size() != 1) {
            return CommandResult.failure(
                    "Impossible de supprimer entièrement cet embranchement : "
                            + "le junction doit posséder une unique entrée."
            );
        }

        DialogueTransition previous = incoming.getFirst();
        DialogueTransition terminalPrevious = new DialogueTransition(
                previous.getKey(),
                previous.getSourceNodeKey(),
                null,
                previous.getType() == DialogueTransitionType.END
                        ? DialogueTransitionType.AUTO
                        : previous.getType(),
                previous.getType() == DialogueTransitionType.END
                        ? null
                        : previous.getLabel(),
                previous.getPosition(),
                true,
                previous.getConditions(),
                previous.getActions(),
                previous.getType() == DialogueTransitionType.AUTO
                        ? previous.getPlayerReplies()
                        : List.of()
        );

        java.util.Set<String> outgoingKeys = outgoing.stream()
                .map(DialogueTransition::getKey)
                .collect(java.util.stream.Collectors.toSet());

        List<DialogueTransition> transitions = new ArrayList<>();

        for (DialogueTransition current : dialogue.getTransitions()) {
            if (outgoingKeys.contains(current.getKey())) {
                continue;
            }

            if (current.getKey().equals(previous.getKey())) {
                transitions.add(terminalPrevious);
            } else {
                transitions.add(current);
            }
        }

        Dialogue updated = copyDialogue(
                dialogue,
                dialogue.getNodes(),
                transitions
        );

        updated = pruneUnreachableGraph(updated);

        if (!repository.save(updated)) {
            return CommandResult.failure(
                    "Impossible de supprimer entièrement l'embranchement."
            );
        }

        return CommandResult.success("Embranchement supprimé.");
    }

    private Dialogue collapseBranchIfNeeded(
            Dialogue dialogue,
            String junctionKey
    ) {
        Optional<DialogueNode> junctionResult = dialogue.findNode(junctionKey);

        if (junctionResult.isEmpty() || !junctionResult.get().isStructural()) {
            return dialogue;
        }

        List<DialogueTransition> outgoing = dialogue.getTransitionsFrom(junctionKey);

        if (outgoing.size() != 1) {
            return dialogue;
        }

        DialogueTransition remaining = outgoing.getFirst();

        if (remaining.getType() != DialogueTransitionType.CHOICE) {
            return dialogue;
        }

        DialogueTransition collapsed = new DialogueTransition(
                remaining.getKey(),
                remaining.getSourceNodeKey(),
                remaining.getTargetNodeKey(),
                DialogueTransitionType.AUTO,
                null,
                remaining.getPosition(),
                remaining.isTerminal(),
                remaining.getConditions(),
                remaining.getActions(),
                List.of()
        );

        return replaceTransition(dialogue, collapsed);
    }

    /**
     * Prune général réservé aux opérations destructives explicites.
     * Un node partagé reste conservé dès qu'il est encore accessible depuis
     * le start node par au moins un chemin.
     */
    private Dialogue pruneUnreachableGraph(
            Dialogue dialogue
    ) {
        String start = dialogue.getStartNodeKey();

        if (start == null || start.isBlank()) {
            return dialogue;
        }

        java.util.Set<String> reachable = new java.util.HashSet<>();
        java.util.ArrayDeque<String> queue = new java.util.ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.removeFirst();

            if (!reachable.add(current)) {
                continue;
            }

            for (DialogueTransition transition : dialogue.getTransitionsFrom(current)) {
                if (transition.isTerminal()) {
                    continue;
                }

                String target = transition.getTargetNodeKey();
                if (target != null && !target.isBlank()) {
                    queue.addLast(target);
                }
            }
        }

        List<DialogueNode> nodes = dialogue.getNodes().stream()
                .filter(node -> reachable.contains(node.getKey()))
                .toList();

        List<DialogueTransition> transitions = dialogue.getTransitions().stream()
                .filter(transition -> reachable.contains(transition.getSourceNodeKey()))
                .filter(transition -> transition.isTerminal()
                        || transition.getTargetNodeKey() == null
                        || reachable.contains(transition.getTargetNodeKey()))
                .toList();

        return copyDialogue(dialogue, nodes, transitions);
    }

    private CommandResult mutatePlayerReply(
            String dialogueKey,
            String transitionKey,
            int replyPosition,
            Function<DialoguePlayerReply, DialoguePlayerReply> mutation,
            String successMessage
    ) {
        Optional<Dialogue> result =
                repository.findByKey(dialogueKey);

        if (result.isEmpty()) {
            return CommandResult.failure(
                    "Dialogue introuvable : " + dialogueKey
            );
        }

        Dialogue dialogue = result.get();
        Optional<DialogueTransition> transitionResult =
                dialogue.findTransition(transitionKey);

        if (transitionResult.isEmpty()) {
            return CommandResult.failure(
                    "Transition introuvable : " + transitionKey
            );
        }

        DialogueTransition previous =
                transitionResult.get();

        List<DialoguePlayerReply> replies =
                new ArrayList<>();

        boolean found = false;
        boolean mutationValid = true;

        for (DialoguePlayerReply reply :
                previous.getPlayerReplies()) {
            if (reply.getPosition()
                    == replyPosition) {
                DialoguePlayerReply replacement =
                        mutation.apply(reply);

                if (replacement == null) {
                    mutationValid = false;
                    replies.add(reply);
                } else {
                    replies.add(replacement);
                }

                found = true;
            } else {
                replies.add(reply);
            }
        }

        if (!found) {
            return CommandResult.failure(
                    "Aucune réplique Joueur en position "
                            + replyPosition
                            + " sur la transition '"
                            + transitionKey
                            + "'."
            );
        }

        if (!mutationValid) {
            return CommandResult.failure(
                    "Élément introuvable dans la réplique Joueur en position "
                            + replyPosition
                            + "."
            );
        }

        DialogueTransition updatedTransition =
                copyTransitionWithPlayerReplies(
                        previous,
                        replies
                );

        if (!repository.save(
                replaceTransition(
                        dialogue,
                        updatedTransition
                )
        )) {
            return CommandResult.failure(
                    "Impossible de sauvegarder la réplique Joueur."
            );
        }

        return CommandResult.success(successMessage);
    }


    private CommandResult insertPlayerReplyAfterChoice(
            Dialogue dialogue,
            DialogueTransition choiceTransition,
            String text
    ) {
        String continuationNodeKey =
                generateUniqueNodeKey(
                        dialogue,
                        choiceTransition.getKey() + "_player_reply"
                );

        String continuationTransitionKey =
                generateUniqueTransitionKey(
                        dialogue,
                        choiceTransition.getKey() + "_after_player_reply"
                );

        DialogueNode continuationNode =
                DialogueNode.structural(
                        continuationNodeKey
                );

        DialogueTransition choicePrefix =
                new DialogueTransition(
                        choiceTransition.getKey(),
                        choiceTransition.getSourceNodeKey(),
                        continuationNodeKey,
                        DialogueTransitionType.CHOICE,
                        choiceTransition.getLabel(),
                        choiceTransition.getPosition(),
                        false,
                        choiceTransition.getConditions(),
                        choiceTransition.getActions(),
                        List.of()
                );

        DialogueTransition continuation =
                new DialogueTransition(
                        continuationTransitionKey,
                        continuationNodeKey,
                        choiceTransition.getTargetNodeKey(),
                        DialogueTransitionType.AUTO,
                        null,
                        1,
                        choiceTransition.isTerminal(),
                        List.of(),
                        List.of(),
                        List.of(
                                new DialoguePlayerReply(
                                        text,
                                        1,
                                        List.of(),
                                        List.of()
                                )
                        )
                );

        List<DialogueNode> nodes =
                new ArrayList<>(
                        dialogue.getNodes()
                );

        nodes.add(
                continuationNode
        );

        List<DialogueTransition> transitions =
                new ArrayList<>();

        for (DialogueTransition current :
                dialogue.getTransitions()) {

            if (current.getKey().equals(
                    choiceTransition.getKey()
            )) {
                transitions.add(
                        choicePrefix
                );
                transitions.add(
                        continuation
                );
            } else {
                transitions.add(
                        current
                );
            }
        }

        Dialogue updatedDialogue =
                copyDialogue(
                        dialogue,
                        nodes,
                        transitions
                );

        if (!repository.save(
                updatedDialogue
        )) {
            return CommandResult.failure(
                    "Impossible d'ajouter la réplique Joueur après le choix."
            );
        }

        return CommandResult.success(
                "Réplique Joueur ajoutée après le choix."
        );
    }

    private DialogueTransition copyTransitionWithPlayerReplies(
            DialogueTransition transition,
            List<DialoguePlayerReply> playerReplies
    ) {
        return new DialogueTransition(
                transition.getKey(),
                transition.getSourceNodeKey(),
                transition.getTargetNodeKey(),
                transition.getType(),
                transition.getLabel(),
                transition.getPosition(),
                transition.isTerminal(),
                transition.getConditions(),
                transition.getActions(),
                playerReplies
        );
    }

    private List<DialoguePlayerReply> normalizePlayerReplyPositions(
            List<DialoguePlayerReply> replies
    ) {
        List<DialoguePlayerReply> normalized =
                new ArrayList<>();

        for (int i = 0; i < replies.size(); i++) {
            DialoguePlayerReply reply =
                    replies.get(i);

            normalized.add(
                    new DialoguePlayerReply(
                            reply.getText(),
                            i + 1,
                            reply.getConditions(),
                            reply.getActions()
                    )
            );
        }

        return List.copyOf(normalized);
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