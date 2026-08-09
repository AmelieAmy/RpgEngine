package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.condition.ConditionManager;

import java.util.List;
import java.util.Optional;

/**
 * Gère la navigation à l'intérieur d'un graphe de dialogue.
 *
 * Cette classe est responsable :
 *   de rechercher les transitions depuis le node courant ;
 *   de filtrer ces transitions à l'aide du ConditionManager ;
 *   de résoudre le node cible d'une transition.
 *
 * Elle ne modifie jamais la session et n'exécute aucune action.
 */
public final class DialogueNavigator {

    private final ConditionManager conditionManager;

    /**
     * Construit le navigateur.
     *
     * @param conditionManager système d'évaluation des conditions
     */
    public DialogueNavigator(
            ConditionManager conditionManager
    ) {
        this.conditionManager =
                conditionManager;
    }

    /**
     * Retourne les transitions actuellement disponibles depuis le node courant.
     *
     * Une transition est disponible si :
     *   son node source correspond au node courant ;
     *   toutes ses conditions sont validées.
     *
     * Les transitions sont retournées dans leur ordre de position.
     *
     * @param dialogue dialogue courant
     * @param session session du joueur
     *
     * @return transitions disponibles
     */
    public List<DialogueTransition>
    getAvailableTransitions(
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
     * Recherche le node cible d'une transition.
     *
     * Une transition END ne possède volontairement aucun node cible.
     *
     * @param dialogue dialogue courant
     * @param transition transition choisie
     *
     * @return node cible s'il existe
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