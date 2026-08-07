package fr.doryamy.rpgengine.action.assignment;

/**
 * Représente une affectation interprétée par le moteur.
 *
 * Une affectation est composée :
 * - d'une clé ;
 * - d'un opérateur ;
 * - d'une valeur.
 *
 * Exemples :
 *
 * gold=10
 * gold+=5
 * reputation-=2
 */
public final class Assignment {

    private final String key;
    private final AssignmentOperator operator;
    private final String value;

    /**
     * Construit une affectation.
     *
     * @param key variable ciblée
     * @param operator opérateur d'affectation
     * @param value valeur à appliquer
     */
    public Assignment(
            String key,
            AssignmentOperator operator,
            String value
    ) {
        this.key = key;
        this.operator = operator;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public AssignmentOperator getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }

}