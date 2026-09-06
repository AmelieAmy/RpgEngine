package fr.doryamy.rpgengine.condition;

import fr.doryamy.rpgengine.condition.expression.Expression;
import fr.doryamy.rpgengine.condition.expression.ExpressionEvaluator;
import fr.doryamy.rpgengine.condition.expression.ExpressionParser;
import fr.doryamy.rpgengine.model.Condition;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    /**
     * Construit le manager de conditions.
     *
     * @param parser analyseur des expressions
     * @param evaluator évaluateur des expressions
     */
    public ConditionManager(
            ExpressionParser parser,
            ExpressionEvaluator evaluator
    ) {
        this.parser = Objects.requireNonNull(
                parser,
                "ExpressionParser ne peut pas être null."
        );

        this.evaluator = Objects.requireNonNull(
                evaluator,
                "ExpressionEvaluator ne peut pas être null."
        );
    }

    /**
     * Enregistre un provider de conditions.
     *
     * Deux providers ne peuvent pas être enregistrés
     * sous le même nom.
     *
     * @param provider provider à enregistrer
     *
     * @throws NullPointerException
     * si le provider est null
     *
     * @throws IllegalArgumentException
     * si son nom est vide
     *
     * @throws IllegalStateException
     * si un provider est déjà enregistré sous ce nom
     */
    public void register(
            ConditionProvider provider
    ) {
        Objects.requireNonNull(
                provider,
                "ConditionProvider ne peut pas être null."
        );

        String providerName =
                provider.getProvider();

        if (providerName == null
                || providerName.isBlank()) {

            throw new IllegalArgumentException(
                    "Le nom d'un ConditionProvider ne peut pas être vide."
            );
        }

        ConditionProvider existing =
                providers.putIfAbsent(
                        providerName,
                        provider
                );

        if (existing != null) {
            throw new IllegalStateException(
                    "ConditionProvider déjà enregistré : "
                            + providerName
            );
        }
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
        Objects.requireNonNull(
                context,
                "TriggerContext ne peut pas être null."
        );

        Objects.requireNonNull(
                conditions,
                "La liste des conditions ne peut pas être null."
        );

        if (conditions.isEmpty()) {
            return true;
        }

        for (Condition condition : conditions) {

            Objects.requireNonNull(
                    condition,
                    "Une condition ne peut pas être null."
            );

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