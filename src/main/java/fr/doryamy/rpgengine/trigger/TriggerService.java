package fr.doryamy.rpgengine.trigger;

import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;
import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.repository.ActionRepository;
import fr.doryamy.rpgengine.repository.ConditionRepository;
import fr.doryamy.rpgengine.repository.TriggerRepository;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.List;
import java.util.OptionalInt;

/**
 * Service chargé des opérations d'écriture
 * portant sur un trigger complet.
 *
 * <p>Il orchestre la création du trigger,
 * de ses conditions et de ses actions.
 */
public final class TriggerService {

    private final TriggerRepository triggerRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    public TriggerService(
            TriggerRepository triggerRepository,
            ConditionRepository conditionRepository,
            ActionRepository actionRepository
    ) {
        this.triggerRepository =
                triggerRepository;

        this.conditionRepository =
                conditionRepository;

        this.actionRepository =
                actionRepository;
    }

    /**
     * Crée un trigger complet.
     *
     * @return identifiant du trigger créé,
     *         ou vide en cas d'échec
     */
    public OptionalInt create(
            String name,
            TriggerType type,
            String targetId,
            List<Condition> conditions,
            List<Action> actions
    ) {
        OptionalInt triggerIdResult =
                triggerRepository.create(
                        name,
                        type,
                        targetId
                );

        if (triggerIdResult.isEmpty()) {
            return OptionalInt.empty();
        }

        int triggerId =
                triggerIdResult.getAsInt();

        for (Condition condition : conditions) {

            if (!conditionRepository.create(
                    triggerId,
                    condition
            )) {
                cleanup(triggerId);

                return OptionalInt.empty();
            }
        }

        for (Action action : actions) {

            if (!actionRepository.create(
                    triggerId,
                    action
            )) {
                cleanup(triggerId);

                return OptionalInt.empty();
            }
        }

        return OptionalInt.of(
                triggerId
        );
    }

    /**
     * Nettoie un trigger partiellement créé.
     *
     * <p>Grâce aux cascades SQLite, supprimer
     * le trigger supprime également les éventuelles
     * conditions/actions déjà insérées.
     */
    private void cleanup(
            int triggerId
    ) {
        if (!triggerRepository.delete(
                triggerId
        )) {
            RpgLogger.error(
                    "Impossible de nettoyer le trigger "
                            + triggerId
                            + " après un échec de création."
            );
        }
    }
}