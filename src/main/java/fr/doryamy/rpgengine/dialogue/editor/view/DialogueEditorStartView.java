package fr.doryamy.rpgengine.dialogue.editor.view;

import java.util.List;
import java.util.Objects;

/**
 * Projection du point de départ système d'un dialogue.
 *
 * <p>Les règles exposées ici restent des règles globales du Dialogue :
 * le Start sert uniquement de point d'ancrage visuel dans l'éditeur.
 */
public record DialogueEditorStartView(
        String key,
        List<DialogueEditorConditionView> conditions,
        List<DialogueEditorActionView> actions
) implements DialogueEditorElementView {

    public DialogueEditorStartView {
        key = Objects.requireNonNull(key, "key").trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                    "La clé du Start ne peut pas être vide."
            );
        }

        conditions = List.copyOf(Objects.requireNonNull(conditions, "conditions"));
        actions = List.copyOf(Objects.requireNonNull(actions, "actions"));

        if (conditions.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("conditions contient une entrée nulle.");
        }
        if (actions.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("actions contient une entrée nulle.");
        }
    }

    /** Constructeur de compatibilité sans règles exposées. */
    public DialogueEditorStartView(String key) {
        this(key, List.of(), List.of());
    }

}
