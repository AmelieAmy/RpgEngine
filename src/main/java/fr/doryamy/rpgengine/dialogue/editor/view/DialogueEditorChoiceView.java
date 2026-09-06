package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.List;
import java.util.Objects;

/**
 * Projection d'un choix appartenant
 * à un embranchement.
 */
public record DialogueEditorChoiceView(
        String key,
        String text,
        int position,
        List<DialogueEditorConditionView> conditions,
        List<DialogueEditorActionView> actions
) implements DialogueEditorElementView {

    public DialogueEditorChoiceView {

        key = requireNonBlank(
                key,
                "key"
        );

        text = requireNonBlank(
                text,
                "text"
        );

        if (position < 0) {
            throw new IllegalArgumentException(
                    "La position d'un choix ne peut pas être négative."
            );
        }

        conditions =
                List.copyOf(
                        Objects.requireNonNull(
                                conditions,
                                "conditions"
                        )
                );

        actions =
                List.copyOf(
                        Objects.requireNonNull(
                                actions,
                                "actions"
                        )
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