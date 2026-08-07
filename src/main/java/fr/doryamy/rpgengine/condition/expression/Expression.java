package fr.doryamy.rpgengine.condition.expression;

/**
 * Représente une expression de comparaison interprétée par le moteur.
 *
 * Une expression est composée :
 * - d'une clé à résoudre ;
 * - d'un opérateur de comparaison ;
 * - d'une valeur attendue.
 *
 * Exemple :
 *
 * gold>=10
 *
 * devient :
 *
 * key = gold
 * operator = GREATER_OR_EQUAL
 * value = 10
 *
 * Cette classe est immuable et ne contient aucune logique métier.
 */
public final class Expression {

    private final String key;
    private final ComparisonOperator operator;
    private final String value;

    /**
     * Construit une expression de comparaison.
     *
     * @param key clé dont la valeur doit être résolue
     * @param operator opérateur de comparaison
     * @param value valeur attendue
     */
    public Expression(
            String key,
            ComparisonOperator operator,
            String value
    ) {
        this.key = key;
        this.operator = operator;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }
}
