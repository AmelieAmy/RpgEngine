package fr.doryamy.rpgengine.model;

import java.util.Objects;

/**
 * Représente une action configurée dans une règle RPG.
 *
 * Une action décrit :
 *   le provider chargé de l'exécuter ;
 *   l'expression associée ;
 *   sa position dans l'ordre d'exécution.
 *
 * Cette classe est immuable et ne contient
 * aucune logique métier.
 */
public final class Action {

    private final String provider;
    private final String expression;
    private final int position;

    /**
     * Construit une action.
     *
     * @param provider provider chargé de l'exécution
     * @param expression expression ou donnée de l'action
     * @param position ordre d'exécution dans le trigger
     */
    public Action(
            String provider,
            String expression,
            int position
    ) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException(
                    "Le provider d'une action ne peut pas être vide."
            );
        }

        this.provider = provider;

        this.expression = Objects.requireNonNull(
                expression,
                "L'expression d'une action ne peut pas être null."
        );

        if (position < 0) {
            throw new IllegalArgumentException(
                    "La position d'une action ne peut pas être négative."
            );
        }

        this.position = position;
    }

    public String getProvider() {
        return provider;
    }

    public String getExpression() {
        return expression;
    }

    public int getPosition() {
        return position;
    }
}