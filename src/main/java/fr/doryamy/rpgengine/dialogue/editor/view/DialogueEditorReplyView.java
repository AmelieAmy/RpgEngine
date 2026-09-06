package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.List;
import java.util.Objects;

/**
 * Projection d'une réplique PNJ ou Joueur.
 */
public record DialogueEditorReplyView(
        String key,
        String speaker,
        String text,
        List<DialogueEditorConditionView> conditions,
        List<DialogueEditorActionView> actions
) implements DialogueEditorElementView {

    public DialogueEditorReplyView {

        key = requireNonBlank(
                key,
                "key"
        );

        speaker = requireNonBlank(
                speaker,
                "speaker"
        );

        text = requireNonBlank(
                text,
                "text"
        );

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