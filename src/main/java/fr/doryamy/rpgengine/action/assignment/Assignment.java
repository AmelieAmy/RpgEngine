package fr.doryamy.rpgengine.action.assignment;

import java.util.Objects;

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
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "La clé d'une affectation ne peut pas être vide."
            );
        }

        this.key = key;

        this.operator = Objects.requireNonNull(
                operator,
                "L'opérateur d'une affectation ne peut pas être null."
        );

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "La valeur d'une affectation ne peut pas être vide."
            );
        }

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