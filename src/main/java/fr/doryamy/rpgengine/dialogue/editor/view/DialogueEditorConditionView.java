package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.Objects;

/**
 * Projection d'une Condition appartenant
 * à une réplique ou un choix.
 *
 * @param key identité stable de la règle
 * @param provider provider générique RPGEngine
 * @param expression expression configurée
 */
public record DialogueEditorConditionView(
        String key,
        String provider,
        String expression
) {

    public DialogueEditorConditionView {

        key = requireNonBlank(
                key,
                "key"
        );

        provider = requireNonBlank(
                provider,
                "provider"
        );

        Objects.requireNonNull(
                expression,
                "expression"
        );
    }

    private static String requireNonBlank(
            String value,
            String name
    ) {

        Objects.requireNonNull(
                value,
                name
        );

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    name + " ne peut pas être vide."
            );
        }

        return normalized;
    }
}