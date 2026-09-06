package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.List;
import java.util.Objects;

/**
 * Projection de la liste d'administration
 * des dialogues.
 */
public record DialogueAdminView(
        List<DialogueAdminEntryView> dialogues
) {

    public DialogueAdminView {

        Objects.requireNonNull(
                dialogues,
                "dialogues"
        );

        dialogues =
                List.copyOf(
                        dialogues
                );

        if (dialogues.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "La liste des dialogues contient une entrée nulle."
            );
        }
    }
}