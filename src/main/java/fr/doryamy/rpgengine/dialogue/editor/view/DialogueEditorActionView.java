package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.Objects;

/**
 * Projection d'une Action appartenant
 * à une réplique ou un choix.
 *
 * @param key identité stable de la règle
 * @param provider provider générique RPGEngine
 * @param expression expression configurée
 * @param position ordre d'exécution
 */
public record DialogueEditorActionView(
        String key,
        String provider,
        String expression,
        int position
) {

    public DialogueEditorActionView {

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

        if (position < 0) {
            throw new IllegalArgumentException(
                    "La position d'une action ne peut pas être négative."
            );
        }
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