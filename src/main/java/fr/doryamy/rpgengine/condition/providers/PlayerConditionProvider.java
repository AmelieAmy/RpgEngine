package fr.doryamy.rpgengine.condition.providers;

import fr.doryamy.rpgengine.condition.ConditionProvider;
import fr.doryamy.rpgengine.repository.PlayerVariableRepository;
import fr.doryamy.rpgengine.trigger.TriggerContext;

/**
 * Provider de conditions associé aux variables joueur.
 *
 * Cette implémentation résout les clés du provider
 * "PLAYER" à partir de PlayerVariableRepository.
 *
 * Une variable inexistante ou vide est actuellement interprétée comme "false".
 */
public final class PlayerConditionProvider implements ConditionProvider {

    private final PlayerVariableRepository repository;

    /**
     * Construit le provider à partir du dépôt
     * des variables joueur.
     *
     * @param repository dépôt des variables joueur
     */
    public PlayerConditionProvider(
            PlayerVariableRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public String getProvider() {
        return "PLAYER";
    }

    /**
     * Résout une variable appartenant au joueur
     * contenu dans le contexte courant.
     *
     * @param context contexte d'exécution
     * @param key nom de la variable à lire
     *
     * @return valeur stockée, ou "false"
     *         si la variable est inexistante ou vide
     */
    @Override
    public String resolve(
            TriggerContext context,
            String key
    ) {

        String value =
                repository.get(
                        context.getPlayer()
                                .getUniqueId(),
                        key
                );

        /*
         * Valeur par défaut des variables joueur.
         *
         * Une variable inexistante est considérée
         * comme false.
         */
        if (value == null || value.isBlank()) {
            return "false";
        }

        return value;
    }
}