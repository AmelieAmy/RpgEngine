package fr.doryamy.rpgengine.action;

import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Orchestre l'exécution des actions d'un trigger.
 *
 * Cette classe sélectionne l'ActionExecutor adapté
 * en fonction du provider de chaque action.
 *
 * Elle ne contient aucune logique métier propre
 * aux actions elles-mêmes.
 *
 * Chaque ActionExecutor est responsable
 * d'un seul type d'action.
 */
public final class ActionManager {

    /**
     * Registre des executors disponibles,
     * indexés par leur provider.
     */
    private final Map<String, ActionExecutor> executors =
            new HashMap<>();

    /**
     * Enregistre un executor.
     *
     * Deux executors ne peuvent pas être enregistrés
     * pour le même provider.
     *
     * @param executor executor à enregistrer
     *
     * @throws NullPointerException
     * si l'executor est null
     *
     * @throws IllegalArgumentException
     * si son provider est vide
     *
     * @throws IllegalStateException
     * si un executor est déjà enregistré
     * pour ce provider
     */
    public void register(
            ActionExecutor executor
    ) {
        Objects.requireNonNull(
                executor,
                "ActionExecutor ne peut pas être null."
        );

        String provider =
                executor.getProvider();

        if (provider == null
                || provider.isBlank()) {

            throw new IllegalArgumentException(
                    "Le provider d'un ActionExecutor ne peut pas être vide."
            );
        }

        ActionExecutor existing =
                executors.putIfAbsent(
                        provider,
                        executor
                );

        if (existing != null) {
            throw new IllegalStateException(
                    "ActionExecutor déjà enregistré pour le provider : "
                            + provider
            );
        }
    }

    /**
     * Exécute une action.
     *
     * Le manager sélectionne automatiquement l'executor correspondant
     * au provider défini dans l'action.
     *
     * Si aucun executor n'est enregistré pour ce provider,
     * l'action n'est pas exécutée et l'erreur est journalisée.
     *
     * @param context contexte du trigger
     * @param action action à exécuter
     */
    public void execute(
            TriggerContext context,
            Action action
    ) {
        Objects.requireNonNull(
                context,
                "TriggerContext ne peut pas être null."
        );

        Objects.requireNonNull(
                action,
                "Action ne peut pas être null."
        );

        String provider =
                action.getProvider();

        ActionExecutor executor =
                executors.get(
                        provider
                );

        if (executor == null) {

            RpgLogger.error(
                    "Provider d'action inconnu : "
                            + action.getProvider()
                            + " | expression="
                            + action.getExpression()
            );

            return;
        }

        executor.execute(
                context,
                action
        );
    }
}