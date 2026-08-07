package fr.doryamy.rpgengine.condition.expression;

import java.util.List;

/**
 * Analyse une expression de condition
 * et produit un objet Expression.
 *
 * Exemple :
 *
 * reputation>=50
 *
 * devient :
 *
 * key = reputation
 * operator = GREATER_OR_EQUAL
 * value = 50
 *
 * Cette classe ne résout aucune donnée
 * et n'évalue aucune condition.
 */
public final class ExpressionParser {

    /**
     * Ordre de recherche des opérateurs.
     *
     * Les opérateurs composés doivent être recherchés
     * avant leurs formes simples.
     *
     * Par exemple ">=" doit être détecté avant ">".
     */
    private static final List<ComparisonOperator> ORDER =
        List.of(
            ComparisonOperator.GREATER_OR_EQUAL,
            ComparisonOperator.LESS_OR_EQUAL,
            ComparisonOperator.NOT_EQUAL,
            ComparisonOperator.EQUAL,
            ComparisonOperator.GREATER,
            ComparisonOperator.LESS
        );

    /**
     * Analyse une expression textuelle.
     *
     * @param expression expression à analyser
     * @return expression structurée
     *
     * @throws IllegalArgumentException si l'expression est vide,
     *   incomplète ou ne contient aucun opérateur supporté
     */
    public Expression parse(String expression) {

        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Expression vide.");
        }

        for (ComparisonOperator operator : ORDER) {

            String symbol = operator.getSymbol();
            int index = expression.indexOf(symbol);

            if (index == -1) {
                continue;
            }

            String key = expression.substring(0, index).trim();
            String value = expression.substring(index + symbol.length()).trim();

            if (key.isEmpty()) {
                throw new IllegalArgumentException(
                        "Clé absente dans : " + expression
                );
            }

            if (value.isEmpty()) {
                throw new IllegalArgumentException(
                        "Valeur absente dans : " + expression
                );
            }

            return new Expression(
                    key,
                    operator,
                    value
            );
        }

        throw new IllegalArgumentException(
                "Aucun opérateur trouvé dans : " + expression
        );
    }

}