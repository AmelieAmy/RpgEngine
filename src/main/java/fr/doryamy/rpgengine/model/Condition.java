package fr.doryamy.rpgengine.model;

/**
 * Représente une condition associée à une règle RPG.
 *
 * Une condition décrit :
 *   le provider chargé de résoudre la donnée ;
 *   l'expression à évaluer.
 *
 * Cette classe est immuable et ne contient
 * aucune logique métier.
 */
public final class Condition {
    private final String provider;
    private final String expression;

    /**
     * Construit une condition.
     *
     * @param provider provider chargé de résoudre la donnée
     * @param expression expression à évaluer
     */
    public Condition(
            String provider,
            String expression
    ) {
        this.provider = provider;
        this.expression = expression;
    }

    public String getProvider() {
        return provider;
    }

    public String getExpression() {
        return expression;
    }
}