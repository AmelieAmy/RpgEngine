package fr.doryamy.rpgengine.trigger;

import fr.doryamy.rpgengine.action.ActionManager;
import fr.doryamy.rpgengine.condition.ConditionManager;
import fr.doryamy.rpgengine.model.Trigger;
import fr.doryamy.rpgengine.repository.TriggerRepository;

import java.sql.Connection;
import java.util.List;

/**
 * Orchestre l'exécution des règles correspondant à un événement RPG.
 *
 * Cette classe :
 * - recherche les triggers correspondants ;
 * - vérifie leurs conditions ;
 * - exécute leurs actions.
 *
 * Elle ne contient aucune logique propre à Citizens, Bukkit
 * ou à un type particulier de déclencheur.
 */
public class TriggerManager {

    private final TriggerRepository triggerRepository;
    private final ActionManager actionManager;
    private final ConditionManager conditionManager;

    public TriggerManager(
            Connection connection,
            ConditionManager conditionManager,
            ActionManager actionManager
    ) {
        this.triggerRepository =
                new TriggerRepository(connection);

        this.conditionManager =
                conditionManager;

        this.actionManager =
                actionManager;
    }

    /**
     * Traite un événement transmis au moteur RPG.
     *
     * @param context contexte contenant le joueur, le type de trigger
     *                et l'identifiant de la cible
     */
    public void handleTrigger(TriggerContext context) {

        List<Trigger> triggers =
                triggerRepository.find(
                        context.getTriggerType(),
                        context.getTargetId()
                );

        /*
         * Première phase :
         *
         * Toutes les conditions sont évaluées avant toute modification de l'état du jeu.
         *
         * Cela garantit que les actions d'un trigger ne peuvent pas influencer
         * l'évaluation des triggers suivants pendant le même événement.
         */
        List<Trigger> matchingTriggers =
                triggers.stream()
                        .filter(trigger ->
                                conditionManager.check(
                                        context,
                                        trigger.getConditions()
                                )
                        )
                        .toList();

        /*
         * Deuxième phase :
         *
         * Les actions sont exécutées uniquement pour les triggers qui
         * étaient déjà valides lors de la première phase.
         *
         * L'ordre d'exécution ne modifie donc pas
         * le résultat de l'évaluation des règles.
         */
        for (Trigger trigger : matchingTriggers) {

            trigger.getActions().forEach(action ->
                    actionManager.execute(
                            context,
                            action
                    )
            );
        }
    }
}