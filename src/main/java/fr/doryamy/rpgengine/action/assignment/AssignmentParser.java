package fr.doryamy.rpgengine.action.assignment;

import java.util.List;

/**
 * Analyse une expression d'affectation
 * et produit un objet Assignment.
 *
 * Exemple :
 *
 * gold+=10
 *
 * devient :
 *
 * key = gold
 * operator = ADD
 * value = 10
 */
public final class AssignmentParser {

    /**
     * Ordre de recherche des opérateurs.
     *
     * Les opérateurs composés doivent être testés
     * avant l'opérateur "=".
     */
    private static final List<AssignmentOperator> ORDER =
            List.of(
                    AssignmentOperator.ADD,
                    AssignmentOperator.SUBTRACT,
                    AssignmentOperator.MULTIPLY,
                    AssignmentOperator.DIVIDE,
                    AssignmentOperator.SET
            );

    /**
     * Analyse une affectation.
     *
     * @param assignment expression à analyser
     *
     * @return affectation analysée
     *
     * @throws IllegalArgumentException
     * si l'expression est vide, incomplète
     * ou ne contient aucun opérateur supporté
     */
    public Assignment parse(String assignment) {

        if (assignment == null || assignment.isBlank()) {
            throw new IllegalArgumentException(
                    "Assignment vide."
            );
        }

        for (AssignmentOperator operator : ORDER) {

            String symbol =
                    operator.getSymbol();

            int index =
                    assignment.indexOf(
                            symbol
                    );

            if (index == -1) {
                continue;
            }

            String key =
                    assignment.substring(
                            0,
                            index
                    ).trim();

            String value =
                    assignment.substring(
                            index + symbol.length()
                    ).trim();

            if (key.isEmpty()) {
                throw new IllegalArgumentException(
                        "Clé absente dans : "
                                + assignment
                );
            }

            if (value.isEmpty()) {
                throw new IllegalArgumentException(
                        "Valeur absente dans : "
                                + assignment
                );
            }

            return new Assignment(
                    key,
                    operator,
                    value
            );
        }

        throw new IllegalArgumentException(
                "Opérateur inconnu : "
                        + assignment
        );
    }
}