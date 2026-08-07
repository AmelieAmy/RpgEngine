package fr.doryamy.rpgengine.condition.expression;

/**
 * Evalue les expressions de comparaison du moteur.
 *
 * Cette classe est indépendante de la provenance
 * des données. Elle compare uniquement une valeur
 * courante avec une Expression.
 *
 * Les comparaisons supportent actuellement :
 * les booléens ;
 * les entiers ;
 * les nombres décimaux ;
 * les chaînes de caractères.
 */
public final class ExpressionEvaluator {

    /**
     * Evalue une expression à partir de la valeur
     * actuellement résolue par un ConditionProvider.
     *
     * Le type de comparaison est déterminé dynamiquement :
     * boolean ;
     * integer ;
     * double ;
     * string.
     *
     * @param currentValue valeur actuelle à comparer
     * @param expression expression contenant l'opérateur
     *                   et la valeur attendue
     *
     * @return true si la comparaison est satisfaite
     */
    public boolean evaluate(
            String currentValue,
            Expression expression
    ) {

        if (currentValue == null) {
            currentValue = "";
        }

        String expectedValue = expression.getValue();
        ComparisonOperator operator = expression.getOperator();

        // ---------- BOOLEAN ----------

        if (isBoolean(currentValue) && isBoolean(expectedValue)) {

            boolean left = Boolean.parseBoolean(currentValue);
            boolean right = Boolean.parseBoolean(expectedValue);

            return switch (operator) {

                case EQUAL -> left == right;

                case NOT_EQUAL -> left != right;

                default -> false;

            };

        }

        // ---------- INTEGER ----------

        if (isInteger(currentValue) && isInteger(expectedValue)) {

            return compare(
                    Integer.parseInt(currentValue),
                    Integer.parseInt(expectedValue),
                    operator
            );

        }

        // ---------- DOUBLE ----------

        if (isDouble(currentValue) && isDouble(expectedValue)) {

            return compare(
                    Double.parseDouble(currentValue),
                    Double.parseDouble(expectedValue),
                    operator
            );

        }

        // ---------- STRING ----------

        return compare(
                currentValue,
                expectedValue,
                operator
        );

    }

    private boolean compare(
            int left,
            int right,
            ComparisonOperator operator
    ) {

        return switch (operator) {

            case EQUAL -> left == right;
            case NOT_EQUAL -> left != right;
            case GREATER -> left > right;
            case GREATER_OR_EQUAL -> left >= right;
            case LESS -> left < right;
            case LESS_OR_EQUAL -> left <= right;

        };

    }

    private boolean compare(
            double left,
            double right,
            ComparisonOperator operator
    ) {

        return switch (operator) {

            case EQUAL -> left == right;
            case NOT_EQUAL -> left != right;
            case GREATER -> left > right;
            case GREATER_OR_EQUAL -> left >= right;
            case LESS -> left < right;
            case LESS_OR_EQUAL -> left <= right;

        };

    }

    private boolean compare(
            String left,
            String right,
            ComparisonOperator operator
    ) {

        return switch (operator) {

            case EQUAL -> left.equals(right);

            case NOT_EQUAL -> !left.equals(right);

            default -> false;

        };

    }

    private boolean isBoolean(String value) {

        return value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("false");

    }

    private boolean isInteger(String value) {

        try {

            Integer.parseInt(value);

            return true;

        } catch (NumberFormatException e) {

            return false;

        }

    }

    private boolean isDouble(String value) {

        try {

            Double.parseDouble(value);

            return true;

        } catch (NumberFormatException e) {

            return false;

        }

    }

}