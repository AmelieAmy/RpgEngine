package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Orchestre les modifications d'un dialogue
 * demandées par les couches d'administration.
 *
 * <p>Ce service ne contient aucune logique
 * de transformation du graphe.
 *
 * <p>Chaque opération suit le même cycle :
 *
 * <pre>
 * charger le Dialogue
 * -> déléguer la mutation métier
 * -> persister le nouvel agrégat
 * -> retourner le Dialogue mis à jour
 * </pre>
 */
public final class DialogueEditingService {

    private final DialogueService dialogueService;
    private final DialogueGraphService graphService;
    private final DialogueContentService contentService;
    private final DialogueRuleService ruleService;

    public DialogueEditingService(
            DialogueService dialogueService,
            DialogueGraphService graphService,
            DialogueContentService contentService,
            DialogueRuleService ruleService
    ) {

        this.dialogueService =
                Objects.requireNonNull(
                        dialogueService,
                        "dialogueService"
                );

        this.graphService =
                Objects.requireNonNull(
                        graphService,
                        "graphService"
                );

        this.contentService =
                Objects.requireNonNull(
                        contentService,
                        "contentService"
                );

        this.ruleService =
                Objects.requireNonNull(
                        ruleService,
                        "ruleService"
                );
    }

    /**
     * Insère une réplique sur une liaison existante.
     */
    public Dialogue insertReply(
            DialogueKey dialogueKey,
            DialogueInsertionPoint insertionPoint,
            DialogueParticipantKey participantKey,
            String text
    ) {

        Objects.requireNonNull(
                insertionPoint,
                "insertionPoint"
        );

        Objects.requireNonNull(
                participantKey,
                "participantKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        graphService.insertReply(
                                graph,
                                insertionPoint,
                                participantKey,
                                text
                        )
        );
    }

    /**
     * Insère un embranchement sur une liaison existante.
     */
    public Dialogue insertBranch(
            DialogueKey dialogueKey,
            DialogueInsertionPoint insertionPoint
    ) {

        Objects.requireNonNull(
                insertionPoint,
                "insertionPoint"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        graphService.insertBranch(
                                graph,
                                insertionPoint
                        )
        );
    }

    /**
     * Ajoute un choix à un embranchement.
     */
    public Dialogue addChoice(
            DialogueKey dialogueKey,
            DialogueElementKey branchKey
    ) {

        Objects.requireNonNull(
                branchKey,
                "branchKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        graphService.addChoice(
                                graph,
                                branchKey
                        )
        );
    }

    /**
     * Supprime un élément administrable sans exposer son type
     * à la couche de transport.
     */
    public Dialogue deleteElement(
            DialogueKey dialogueKey,
            DialogueElementKey elementKey
    ) {
        Objects.requireNonNull(elementKey, "elementKey");

        return mutateGraph(
                dialogueKey,
                graph -> graphService.deleteElement(
                        graph,
                        elementKey
                )
        );
    }

    /**
     * Supprime une réplique.
     */
    public Dialogue deleteReply(
            DialogueKey dialogueKey,
            DialogueElementKey replyKey
    ) {

        Objects.requireNonNull(
                replyKey,
                "replyKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        graphService.deleteReply(
                                graph,
                                replyKey
                        )
        );
    }

    /**
     * Supprime un choix.
     */
    public Dialogue deleteChoice(
            DialogueKey dialogueKey,
            DialogueElementKey choiceKey
    ) {

        Objects.requireNonNull(
                choiceKey,
                "choiceKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        graphService.deleteChoice(
                                graph,
                                choiceKey
                        )
        );
    }

    /**
     * Supprime un embranchement complet.
     */
    public Dialogue deleteBranch(
            DialogueKey dialogueKey,
            DialogueElementKey branchKey
    ) {

        Objects.requireNonNull(
                branchKey,
                "branchKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        graphService.deleteBranch(
                                graph,
                                branchKey
                        )
        );
    }

    /**
     * Modifie le texte d'un élément éditable sans exposer son type
     * à la couche de transport.
     */
    public Dialogue updateElementText(
            DialogueKey dialogueKey,
            DialogueElementKey elementKey,
            String text
    ) {

        Objects.requireNonNull(elementKey, "elementKey");
        Objects.requireNonNull(text, "text");

        return mutateGraph(
                dialogueKey,
                graph -> contentService.updateElementText(
                        graph,
                        elementKey,
                        text
                )
        );
    }

    /**
     * Modifie le texte d'une réplique.
     */
    public Dialogue updateReplyText(
            DialogueKey dialogueKey,
            DialogueElementKey replyKey,
            String text
    ) {

        Objects.requireNonNull(
                replyKey,
                "replyKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        contentService.updateReplyText(
                                graph,
                                replyKey,
                                text
                        )
        );
    }

    /**
     * Modifie le texte d'un choix.
     */
    public Dialogue updateChoiceText(
            DialogueKey dialogueKey,
            DialogueElementKey choiceKey,
            String text
    ) {

        Objects.requireNonNull(
                choiceKey,
                "choiceKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        contentService.updateChoiceText(
                                graph,
                                choiceKey,
                                text
                        )
        );
    }

    /**
     * Ajoute une Condition à une Reply, un Choice ou au Dialogue lui-même.
     * Le Start sert uniquement d'identifiant de présentation des règles globales.
     */
    public Dialogue addCondition(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            String provider,
            String expression
    ) {
        Objects.requireNonNull(ownerKey, "ownerKey");

        return mutateRules(
                dialogueKey,
                ownerKey,
                rules -> ruleService.addCondition(
                        rules,
                        provider,
                        expression
                ),
                graph -> ruleService.addCondition(
                        graph,
                        ownerKey,
                        provider,
                        expression
                )
        );
    }

    /** Modifie une Condition existante. */
    public Dialogue updateCondition(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey,
            String provider,
            String expression
    ) {
        Objects.requireNonNull(ownerKey, "ownerKey");
        Objects.requireNonNull(ruleKey, "ruleKey");

        return mutateRules(
                dialogueKey,
                ownerKey,
                rules -> ruleService.updateCondition(
                        rules,
                        ruleKey,
                        provider,
                        expression
                ),
                graph -> ruleService.updateCondition(
                        graph,
                        ownerKey,
                        ruleKey,
                        provider,
                        expression
                )
        );
    }

    /** Supprime une Condition. */
    public Dialogue deleteCondition(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey
    ) {
        Objects.requireNonNull(ownerKey, "ownerKey");
        Objects.requireNonNull(ruleKey, "ruleKey");

        return mutateRules(
                dialogueKey,
                ownerKey,
                rules -> ruleService.deleteCondition(
                        rules,
                        ruleKey
                ),
                graph -> ruleService.deleteCondition(
                        graph,
                        ownerKey,
                        ruleKey
                )
        );
    }

    /**
     * Supprime une règle sans exposer au transport
     * s'il s'agit d'une Condition ou d'une Action.
     */
    public Dialogue deleteRule(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey
    ) {
        Objects.requireNonNull(ownerKey, "ownerKey");
        Objects.requireNonNull(ruleKey, "ruleKey");

        return mutateRules(
                dialogueKey,
                ownerKey,
                rules -> ruleService.deleteRule(
                        rules,
                        ruleKey
                ),
                graph -> ruleService.deleteRule(
                        graph,
                        ownerKey,
                        ruleKey
                )
        );
    }

    /** Ajoute une Action à une Reply, un Choice ou au Dialogue lui-même. */
    public Dialogue addAction(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            String provider,
            String expression
    ) {
        Objects.requireNonNull(ownerKey, "ownerKey");

        return mutateRules(
                dialogueKey,
                ownerKey,
                rules -> ruleService.addAction(
                        rules,
                        provider,
                        expression
                ),
                graph -> ruleService.addAction(
                        graph,
                        ownerKey,
                        provider,
                        expression
                )
        );
    }

    /** Modifie une Action existante. */
    public Dialogue updateAction(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey,
            String provider,
            String expression
    ) {
        Objects.requireNonNull(ownerKey, "ownerKey");
        Objects.requireNonNull(ruleKey, "ruleKey");

        return mutateRules(
                dialogueKey,
                ownerKey,
                rules -> ruleService.updateAction(
                        rules,
                        ruleKey,
                        provider,
                        expression
                ),
                graph -> ruleService.updateAction(
                        graph,
                        ownerKey,
                        ruleKey,
                        provider,
                        expression
                )
        );
    }

    /** Supprime une Action. */
    public Dialogue deleteAction(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey
    ) {
        Objects.requireNonNull(ownerKey, "ownerKey");
        Objects.requireNonNull(ruleKey, "ruleKey");

        return mutateRules(
                dialogueKey,
                ownerKey,
                rules -> ruleService.deleteAction(
                        rules,
                        ruleKey
                ),
                graph -> ruleService.deleteAction(
                        graph,
                        ownerKey,
                        ruleKey
                )
        );
    }

    /**
     * Route une mutation de règles vers le Dialogue si ownerKey désigne Start,
     * sinon vers le propriétaire Reply/Choice dans le graphe.
     */
    private Dialogue mutateRules(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            UnaryOperator<DialogueRules> dialogueMutation,
            UnaryOperator<DialogueGraph> graphMutation
    ) {
        Objects.requireNonNull(dialogueKey, "dialogueKey");
        Objects.requireNonNull(dialogueMutation, "dialogueMutation");
        Objects.requireNonNull(graphMutation, "graphMutation");

        Dialogue current = dialogueService.require(dialogueKey);

        if (current.graph().start().key().equals(ownerKey)) {
            DialogueRules updatedRules = Objects.requireNonNull(
                    dialogueMutation.apply(current.rules()),
                    "Une mutation de règles globales ne peut pas retourner null."
            );

            return dialogueService.replaceRules(
                    dialogueKey,
                    updatedRules
            );
        }

        DialogueGraph updatedGraph = Objects.requireNonNull(
                graphMutation.apply(current.graph()),
                "Une mutation de règles d'élément ne peut pas retourner null."
        );

        return dialogueService.replaceGraph(
                dialogueKey,
                updatedGraph
        );
    }

    /**
     * Charge le Dialogue, applique exactement
     * une mutation métier à son graphe puis
     * persiste le nouvel agrégat.
     */
    private Dialogue mutateGraph(
            DialogueKey dialogueKey,
            UnaryOperator<DialogueGraph> mutation
    ) {

        Objects.requireNonNull(
                dialogueKey,
                "dialogueKey"
        );

        Objects.requireNonNull(
                mutation,
                "mutation"
        );

        Dialogue current =
                dialogueService.require(
                        dialogueKey
                );

        DialogueGraph updatedGraph =
                Objects.requireNonNull(
                        mutation.apply(
                                current.graph()
                        ),
                        "Une mutation de dialogue "
                                + "ne peut pas retourner null."
                );

        return dialogueService.replaceGraph(
                dialogueKey,
                updatedGraph
        );
    }
}