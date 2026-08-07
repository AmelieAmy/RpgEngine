package fr.doryamy.rpgengine.action.assignment;

/**
 * Exécute une affectation sur une valeur existante.
 *
 * Cette classe ne connaît pas l'origine
 * de la variable.
 *
 * Elle applique uniquement la logique
 * des opérateurs d'affectation.
 */
public final class AssignmentExecutor {

    /**
     * Applique une affectation.
     *
     * @param currentValue valeur actuelle
     * @param assignment affectation à appliquer
     *
     * @return nouvelle valeur
     */
    public String execute(
            String currentValue,
            Assignment assignment
    ) {

        if(currentValue == null) {
            currentValue = "";
        }

        AssignmentOperator operator = assignment.getOperator();
        String newValue = assignment.getValue();

        switch (operator) {

            case SET:
                return newValue;

            case ADD:
                return add(
                        currentValue,
                        newValue
                );

            case SUBTRACT:
                return subtract(
                        currentValue,
                        newValue
                );

            case MULTIPLY:
                return multiply(
                        currentValue,
                        newValue
                );

            case DIVIDE:
                return divide(
                        currentValue,
                        newValue
                );

            default:
                throw new IllegalArgumentException(
                        "Opérateur inconnu : "
                                + operator
                );
        }
    }



    private String add(
            String current,
            String value
    ) {
        if (isNumber(current) && isNumber(value)) {
            return String.valueOf(
                    Integer.parseInt(current)
                            +
                            Integer.parseInt(value)
            );
        }
        throw new IllegalArgumentException(
                "Impossible d'ajouter "
                        + current
                        + " et "
                        + value
        );
    }

    private String subtract(
            String current,
            String value
    ) {
        if (isNumber(current) && isNumber(value)) {
            return String.valueOf(
                    Integer.parseInt(current)
                            -
                            Integer.parseInt(value)
            );
        }
        throw new IllegalArgumentException(
                "Impossible de soustraire "
                        + current
                        + " et "
                        + value
        );
    }



    private String multiply(
            String current,
            String value
    ) {
        if (isNumber(current) && isNumber(value)) {
            return String.valueOf(
                    Integer.parseInt(current)
                            *
                            Integer.parseInt(value)
            );
        }
        throw new IllegalArgumentException(
                "Impossible de multiplier "
                        + current
                        + " et "
                        + value
        );
    }



    private String divide(
            String current,
            String value
    ) {

        if (isNumber(current) && isNumber(value)) {
            int divisor = Integer.parseInt(value);
            if(divisor == 0) {
                throw new IllegalArgumentException(
                        "Division par zéro"
                );
            }
            return String.valueOf(
                    Integer.parseInt(current)/divisor
            );
        }

        throw new IllegalArgumentException(
                "Impossible de diviser "
                        + current
                        + " et "
                        + value
        );
    }

    /**
     * Vérifie qu'une valeur représente un entier.
     */
    private boolean isNumber(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

}