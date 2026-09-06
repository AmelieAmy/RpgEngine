package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.Objects;

/**
 * Projection d'une liaison structurelle
 * entre deux éléments.
 *
 * <p>Une liaison ne possède aucune identité
 * ou règle métier propre.
 */
public record DialogueEditorLinkView(
        String sourceKey,
        String targetKey
) {

    public DialogueEditorLinkView {

        sourceKey = requireNonBlank(
                sourceKey,
                "sourceKey"
        );

        targetKey = requireNonBlank(
                targetKey,
                "targetKey"
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