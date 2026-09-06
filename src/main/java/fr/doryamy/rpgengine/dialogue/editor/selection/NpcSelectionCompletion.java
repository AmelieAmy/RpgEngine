package fr.doryamy.rpgengine.dialogue.editor.selection;

import java.util.Objects;

/** Résultat atomique d'une sélection administrative Citizens. */
public record NpcSelectionCompletion(
        NpcSelectionIntent intent,
        NpcSelection selection
) {
    public NpcSelectionCompletion {
        Objects.requireNonNull(intent, "intent");
        Objects.requireNonNull(selection, "selection");
    }
}
