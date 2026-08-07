package fr.doryamy.rpgengine.condition;

import fr.doryamy.rpgengine.trigger.TriggerContext;

/**
 * Contrat de résolution d'une donnée utilisée
 * par le système de conditions.
 *
 * Chaque implémentation est responsable
 * d'une source de données particulière
 * (joueur, quête, permission, item...).
 *
 * Un provider ne décide jamais si une condition
 * est vraie ou fausse.
 * Il retourne uniquement la valeur courante
 * associée à une clé.
 *
 * L'évaluation appartient à ExpressionEvaluator.
 */
public interface ConditionProvider {

    /**
     * Retourne le nom du provider.
     *
     * Exemple :
     * PLAYER
     * QUEST
     * PERMISSION
     */
    String getProvider();

    /**
     * Résout la valeur associée à une clé
     * à partir du contexte courant.
     *
     * @param context contexte du trigger en cours
     * @param key clé à résoudre
     *
     * @return valeur correspondante
     */
    String resolve(
            TriggerContext context,
            String key
    );
}