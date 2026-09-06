package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.List;
import java.util.Objects;

/** Projection complète d'un Dialogue destinée à l'interface d'administration. */
public record DialogueEditorView(
        String dialogueKey,
        String name,
        List<DialogueEditorTriggerView> triggers,
        List<DialogueEditorQuestView> quests,
        List<DialogueEditorElementView> elements,
        List<DialogueEditorLinkView> links
) {
    public DialogueEditorView {
        dialogueKey = requireNonBlank(dialogueKey, "dialogueKey");
        name = requireNonBlank(name, "name");
        triggers = copyNonNull(triggers, "triggers");
        quests = copyNonNull(quests, "quests");
        elements = copyNonNull(elements, "elements");
        links = copyNonNull(links, "links");
    }

    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(name + " ne peut pas être vide.");
        return normalized;
    }

    private static <T> List<T> copyNonNull(List<T> values, String name) {
        List<T> copy = List.copyOf(Objects.requireNonNull(values, name));
        if (copy.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(name + " contient une entrée nulle.");
        }
        return copy;
    }
}
