package fr.doryamy.rpgengine.action;

import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.HashMap;
import java.util.Map;

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
public class ActionManager {

    /**
     * Registre des executors disponibles, indexés par leur provider.
     */
    private final Map<String, ActionExecutor> executors = new HashMap<>();

    /**
     * Enregistre un executor.
     *
     * Si un executor existe déjà pour ce provider, il sera remplacé.
     *
     * @param executor executor à enregistrer
     */
    public void register(ActionExecutor executor) {
        executors.put(
                executor.getProvider(),
                executor
        );
    }

    /**
     * Exécute une action.
     *
     * Le manager sélectionne automatiquement l'executor correspondant
     * au provider défini dans l'action.
     *
     * Si aucun executor n'est enregistré, l'action est ignorée et
     * un message est envoyé au joueur.
     *
     * @param context contexte du trigger
     * @param action action à exécuter
     */
    public void execute(
            TriggerContext context,
            Action action
    ) {
        String provider = action.getProvider();
        ActionExecutor executor = executors.get(provider);

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