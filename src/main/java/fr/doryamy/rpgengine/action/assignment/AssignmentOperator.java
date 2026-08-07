package fr.doryamy.rpgengine.action.assignment;

/**
 * Opérateurs d'affectation supportés par RPGEngine.
 */
public enum AssignmentOperator {

    /**
     * Affectation directe.
     */
    SET("="),

    /**
     * Addition.
     */
    ADD("+="),

    /**
     * Soustraction.
     */
    SUBTRACT("-="),

    /**
     * Multiplication.
     */
    MULTIPLY("*="),

    /**
     * Division.
     */
    DIVIDE("/=");

    private final String symbol;

    AssignmentOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Retourne le symbole utilisé dans une expression.
     */
    public String getSymbol() {
        return symbol;
    }

}