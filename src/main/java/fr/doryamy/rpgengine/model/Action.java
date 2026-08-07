package fr.doryamy.rpgengine.model;

/**
 * Représente une action configurée dans une règle RPG.
 *
 * Une action décrit :
 *   le provider chargé de l'exécuter ;
 *   l'expression associée ;
 *   sa position dans l'ordre d'exécution.
 *
 * Cette classe est immuable et ne contient
 * aucune logique métier.
 */
public final class Action {

    private final String provider;
    private final String expression;
    private final int position;

    /**
     * Construit une action.
     *
     * @param provider provider chargé de l'exécution
     * @param expression expression ou donnée de l'action
     * @param position ordre d'exécution dans le trigger
     */
    public Action(
            String provider,
            String expression,
            int position
    ) {
        this.provider = provider;
        this.expression = expression;
        this.position = position;
    }

    public String getProvider() {return provider;}

    public String getExpression() {
        return expression;
    }

    public int getPosition() {
        return position;
    }

}