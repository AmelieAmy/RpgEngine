package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.condition.ConditionManager;
import fr.doryamy.rpgengine.model.Condition;
import fr.doryamy.rpgengine.trigger.TriggerContext;

import java.util.List;
import java.util.Objects;

/**
 * Fournit les opérations de navigation et de sélection
 * nécessaires à l'exécution d'un dialogue.
 *
 * <p>Le Navigator ne modifie jamais le graphe,
 * n'exécute aucune Action et ne gère aucune session.
 */
public final class DialogueNavigator {

    private final ConditionManager conditionManager;

    public DialogueNavigator(
            ConditionManager conditionManager
    ) {

        this.conditionManager =
                Objects.requireNonNull(
                        conditionManager,
                        "conditionManager"
                );
    }

    /**
     * Indique si une réplique est disponible
     * dans le contexte courant.
     */
    public boolean isAvailable(
            TriggerContext context,
            DialogueReply reply
    ) {

        Objects.requireNonNull(
                context,
                "context"
        );

        Objects.requireNonNull(
                reply,
                "reply"
        );

        return checkRules(
                context,
                reply.rules()
        );
    }

    /**
     * Indique si un choix est disponible
     * dans le contexte courant.
     */
    public boolean isAvailable(
            TriggerContext context,
            DialogueChoice choice
    ) {

        Objects.requireNonNull(
                context,
                "context"
        );

        Objects.requireNonNull(
                choice,
                "choice"
        );

        return checkRules(
                context,
                choice.rules()
        );
    }

    /**
     * Retourne les choix actuellement disponibles
     * pour un embranchement.
     *
     * <p>L'ordre retourné correspond à l'ordre métier
     * défini par DialogueChoice.position().
     */
    public List<DialogueChoice> availableChoices(
            DialogueGraph graph,
            DialogueBranch branch,
            TriggerContext context
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                branch,
                "branch"
        );

        Objects.requireNonNull(
                context,
                "context"
        );

        graph.require(
                branch.key()
        );

        return graph.choicesOf(
                        branch.key()
                )
                .stream()
                .filter(choice ->
                        isAvailable(
                                context,
                                choice
                        )
                )
                .toList();
    }

    /**
     * Retourne l'unique continuation structurelle
     * d'un Start, d'une Reply ou d'un Choice.
     *
     * <p>Le graphe doit avoir été validé avant
     * son exécution.
     */
    public DialogueElementKey continuationOf(
            DialogueGraph graph,
            DialogueElementKey sourceKey
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                sourceKey,
                "sourceKey"
        );

        DialogueElement source =
                graph.require(
                        sourceKey
                );

        if (!(source instanceof DialogueStart)
                && !(source instanceof DialogueReply)
                && !(source instanceof DialogueChoice)) {

            throw new IllegalArgumentException(
                    "L'élément "
                            + sourceKey
                            + " ne possède pas de continuation unique."
            );
        }

        List<DialogueLink> outgoing =
                graph.outgoingLinks(
                        sourceKey
                );

        if (outgoing.size() != 1) {
            throw new IllegalStateException(
                    "L'élément "
                            + sourceKey
                            + " devrait posséder exactement "
                            + "une continuation. Trouvées : "
                            + outgoing.size()
                            + "."
            );
        }

        return outgoing.getFirst()
                .target();
    }

    /**
     * Indique si un ensemble de règles est satisfait
     * dans le contexte courant. Utilisé notamment
     * pour les règles globales d'un dialogue.
     */
    public boolean isAvailable(
            TriggerContext context,
            DialogueRules rules
    ) {

        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(rules, "rules");

        return checkRules(context, rules);
    }

    /**
     * Évalue les Conditions appartenant
     * à un élément ou au dialogue lui-même.
     */
    private boolean checkRules(
            TriggerContext context,
            DialogueRules rules
    ) {

        List<Condition> conditions =
                rules.conditions()
                        .stream()
                        .map(
                                DialogueConditionEntry::condition
                        )
                        .toList();

        return conditionManager.check(
                context,
                conditions
        );
    }
}