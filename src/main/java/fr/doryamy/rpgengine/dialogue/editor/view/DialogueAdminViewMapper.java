package fr.doryamy.rpgengine.dialogue.editor.view;

import fr.doryamy.rpgengine.dialogue.Dialogue;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Construit la projection résumée de l'administration
 * à partir de données déjà résolues par la couche applicative.
 */
public final class DialogueAdminViewMapper {

    public DialogueAdminView present(
            List<Dialogue> dialogues,
            Map<String, List<DialogueAdminTriggerView>> triggersByDialogueKey
    ) {

        Objects.requireNonNull(dialogues, "dialogues");
        Objects.requireNonNull(
                triggersByDialogueKey,
                "triggersByDialogueKey"
        );

        List<DialogueAdminEntryView> entries =
                dialogues.stream()
                        .map(dialogue ->
                                presentEntry(
                                        dialogue,
                                        triggersByDialogueKey
                                                .getOrDefault(
                                                        dialogue.key().value(),
                                                        List.of()
                                                )
                                )
                        )
                        .sorted(
                                Comparator.comparing(
                                        DialogueAdminEntryView::name,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .toList();

        return new DialogueAdminView(
                entries
        );
    }

    private DialogueAdminEntryView presentEntry(
            Dialogue dialogue,
            List<DialogueAdminTriggerView> triggers
    ) {

        Objects.requireNonNull(dialogue, "dialogue");
        Objects.requireNonNull(triggers, "triggers");

        return new DialogueAdminEntryView(
                dialogue.key().value(),
                dialogue.name(),
                triggers
        );
    }
}
