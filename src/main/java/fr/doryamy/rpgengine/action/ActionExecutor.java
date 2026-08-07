package fr.doryamy.rpgengine.action;

import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.model.Action;

/**
 * Contrat d'exécution d'une action du moteur RPG.
 *
 * Chaque implémentation est responsable d'un provider
 * particulier (MESSAGE, PLAYER, etc.).
 *
 * Les executors appliquent les effets d'une action,
 * mais ne contiennent aucune logique de sélection.
 *
 * Cette responsabilité appartient à ActionManager.
 */
public interface ActionExecutor {

    /**
     * Retourne le nom du provider pris en charge par cet executor.
     *
     * Exemple :
     * MESSAGE
     * PLAYER
     *
     * @return nom du provider
     */
    String getProvider();

    /**
     * Exécute une action à partir du contexte courant.
     *
     * Le TriggerContext contient toutes les informations
     * nécessaires à l'exécution de l'action
     * (joueur, type de trigger, cible, attributs...).
     *
     * @param context contexte d'exécution
     * @param action action à appliquer
     */
    void execute(
            TriggerContext context,
            Action action
    );
}