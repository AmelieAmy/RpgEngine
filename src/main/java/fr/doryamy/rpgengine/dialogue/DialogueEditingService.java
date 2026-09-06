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
            DialogueReplySpeaker speaker,
            String text
    ) {

        Objects.requireNonNull(
                insertionPoint,
                "insertionPoint"
        );

        Objects.requireNonNull(
                speaker,
                "speaker"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        graphService.insertReply(
                                graph,
                                insertionPoint,
                                speaker,
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
     * Ajoute une Condition à une Reply ou un Choice.
     */
    public Dialogue addCondition(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            String provider,
            String expression
    ) {

        Objects.requireNonNull(
                ownerKey,
                "ownerKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        ruleService.addCondition(
                                graph,
                                ownerKey,
                                provider,
                                expression
                        )
        );
    }

    /**
     * Modifie une Condition existante.
     */
    public Dialogue updateCondition(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey,
            String provider,
            String expression
    ) {

        Objects.requireNonNull(
                ownerKey,
                "ownerKey"
        );

        Objects.requireNonNull(
                ruleKey,
                "ruleKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        ruleService.updateCondition(
                                graph,
                                ownerKey,
                                ruleKey,
                                provider,
                                expression
                        )
        );
    }

    /**
     * Supprime une Condition.
     */
    public Dialogue deleteCondition(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey
    ) {

        Objects.requireNonNull(
                ownerKey,
                "ownerKey"
        );

        Objects.requireNonNull(
                ruleKey,
                "ruleKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        ruleService.deleteCondition(
                                graph,
                                ownerKey,
                                ruleKey
                        )
        );
    }

    /**
     * Ajoute une Action à une Reply ou un Choice.
     */
    public Dialogue addAction(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            String provider,
            String expression
    ) {

        Objects.requireNonNull(
                ownerKey,
                "ownerKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        ruleService.addAction(
                                graph,
                                ownerKey,
                                provider,
                                expression
                        )
        );
    }

    /**
     * Modifie une Action existante.
     */
    public Dialogue updateAction(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey,
            String provider,
            String expression
    ) {

        Objects.requireNonNull(
                ownerKey,
                "ownerKey"
        );

        Objects.requireNonNull(
                ruleKey,
                "ruleKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        ruleService.updateAction(
                                graph,
                                ownerKey,
                                ruleKey,
                                provider,
                                expression
                        )
        );
    }

    /**
     * Supprime une Action.
     */
    public Dialogue deleteAction(
            DialogueKey dialogueKey,
            DialogueElementKey ownerKey,
            DialogueRuleKey ruleKey
    ) {

        Objects.requireNonNull(
                ownerKey,
                "ownerKey"
        );

        Objects.requireNonNull(
                ruleKey,
                "ruleKey"
        );

        return mutateGraph(
                dialogueKey,
                graph ->
                        ruleService.deleteAction(
                                graph,
                                ownerKey,
                                ruleKey
                        )
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