package fr.doryamy.rpgengine.condition.expression;

/**
 * Opérateurs de comparaison supportés
 * par le moteur de conditions RPGEngine.
 */
public enum ComparisonOperator {

    /** Egalité. */
    EQUAL("=="),

    /** Différence. */
    NOT_EQUAL("!="),

    /** Strictement supérieur. */
    GREATER(">"),

    /** Supérieur ou égal. */
    GREATER_OR_EQUAL(">="),

    /** Strictement inférieur. */
    LESS("<"),

    /** Inférieur ou égal. */
    LESS_OR_EQUAL("<=");

    private final String symbol;

    ComparisonOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Retourne le symbole textuel utilisé
     * dans les expressions.
     *
     * @return symbole de l'opérateur
     */
    public String getSymbol() {
        return symbol;
    }
}
