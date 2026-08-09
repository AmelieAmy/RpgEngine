package fr.doryamy.rpgengine.action.executors;

import fr.doryamy.rpgengine.action.ActionExecutor;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.trigger.TriggerContext;

/**
 * Executor responsable de l'affichage
 * d'un message au joueur.
 *
 * Cette action correspond au provider
 * "MESSAGE".
 *
 * L'expression de l'action représente
 * directement le texte à afficher.
 */
public final class MessageActionExecutor implements ActionExecutor {

    @Override
    public String getProvider() {
        return "MESSAGE";
    }

    /**
     * Envoie le message défini dans l'action
     * au joueur associé au TriggerContext.
     *
     * @param context contexte d'exécution
     * @param action action à exécuter
     */
    @Override
    public void execute(
            TriggerContext context,
            Action action
    ) {
        context.getPlayer()
                .sendMessage(
                        action.getExpression()
                );
    }
}