package fr.doryamy.rpgengine.condition;

import fr.doryamy.rpgengine.condition.expression.Expression;
import fr.doryamy.rpgengine.condition.expression.ExpressionEvaluator;
import fr.doryamy.rpgengine.condition.expression.ExpressionParser;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.model.Condition;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestre l'évaluation des conditions d'une règle.
 *
 * Cette classe sélectionne le ConditionProvider adapté,
 * analyse les expressions puis délègue leur comparaison
 * à ExpressionEvaluator.
 *
 * Elle ne connaît pas la provenance réelle des données
 * et ne contient aucune logique spécifique aux providers.
 *
 * Toutes les conditions doivent actuellement être validées
 * pour qu'une règle soit considérée comme valide.
 */
public final class ConditionManager {

    /**
     * Registre des providers disponibles,
     * indexés par leur nom.
     */
    private final Map<String, ConditionProvider> providers =
            new HashMap<>();

    private final ExpressionParser parser;
    private final ExpressionEvaluator evaluator;

    public ConditionManager() {
        this.parser = new ExpressionParser();
        this.evaluator = new ExpressionEvaluator();
    }

    /**
     * Enregistre un provider de conditions.
     *
     * Si un provider existe déjà sous le même nom, il est remplacé.
     *
     * @param provider provider à enregistrer
     */
    public void register(ConditionProvider provider) {
        providers.put(
                provider.getProvider(),
                provider
        );
    }

    /**
     * Vérifie toutes les conditions associées à une règle.
     *
     * Les conditions sont évaluées dans l'ordre où elles sont fournies.
     * L'évaluation s'arrête dès qu'une condition est invalide.
     *
     * Un trigger sans condition est toujours considéré comme valide.
     *
     * @param context contexte de l'événement courant
     * @param conditions conditions à vérifier
     *
     * @return true si toutes les conditions sont validées
     */
    public boolean check(
            TriggerContext context,
            List<Condition> conditions
    ) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        for (Condition condition : conditions) {

            ConditionProvider provider =
                    providers.get(
                            condition.getProvider()
                    );

            if (provider == null) {
                RpgLogger.error(
                        "Provider de condition inconnu : "
                                + condition.getProvider()
                                + " | expression="
                                + condition.getExpression()
                );
                return false;
            }

            Expression expression;

            try {
                expression =
                        parser.parse(
                                condition.getExpression()
                        );

            } catch (IllegalArgumentException e) {
                RpgLogger.error(
                        "Expression de condition invalide : "
                                + condition.getExpression()
                                + " | "
                                + e.getMessage()
                );
                return false;
            }

            String currentValue =
                    provider.resolve(
                            context,
                            expression.getKey()
                    );

            boolean valid =
                    evaluator.evaluate(
                            currentValue,
                            expression
                    );

            if (!valid) {
                return false;
            }
        }

        return true;
    }
}