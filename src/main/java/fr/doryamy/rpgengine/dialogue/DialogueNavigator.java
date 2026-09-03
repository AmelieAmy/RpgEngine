package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.condition.ConditionManager;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Gère la navigation à l'intérieur d'un graphe de dialogue.
 */
public final class DialogueNavigator {

    private final ConditionManager conditionManager;

    public DialogueNavigator(
            ConditionManager conditionManager
    ) {
        this.conditionManager = conditionManager;
    }

    /**
     * Retourne les transitions disponibles depuis le node courant.
     */
    public List<DialogueTransition> getAvailableTransitions(
            Dialogue dialogue,
            DialogueSession session
    ) {
        return dialogue.getTransitionsFrom(
                        session.getCurrentNodeKey()
                )
                .stream()
                .filter(transition ->
                        conditionManager.check(
                                session.getContext(),
                                transition.getConditions()
                        )
                )
                .toList();
    }

    /**
     * Retourne les répliques Joueur dont toutes les conditions sont vraies.
     */
    public List<DialoguePlayerReply> getAvailablePlayerReplies(
            DialogueTransition transition,
            DialogueSession session
    ) {
        return transition.getPlayerReplies()
                .stream()
                .filter(reply ->
                        conditionManager.check(
                                session.getContext(),
                                reply.getConditions()
                        )
                )
                .sorted(
                        Comparator.comparingInt(
                                DialoguePlayerReply::getPosition
                        )
                )
                .toList();
    }

    /**
     * Recherche la prochaine réplique Joueur disponible après une position.
     */
    public Optional<DialoguePlayerReply> findNextAvailablePlayerReply(
            DialogueTransition transition,
            DialogueSession session,
            int afterPosition
    ) {
        return getAvailablePlayerReplies(
                transition,
                session
        )
                .stream()
                .filter(reply ->
                        reply.getPosition() > afterPosition
                )
                .findFirst();
    }

    /**
     * Recherche le node cible d'une transition.
     */
    public Optional<DialogueNode> getNextNode(
            Dialogue dialogue,
            DialogueTransition transition
    ) {
        if (transition.getType()
                == DialogueTransitionType.END) {
            return Optional.empty();
        }

        return dialogue.findNode(
                transition.getTargetNodeKey()
        );
    }
}
