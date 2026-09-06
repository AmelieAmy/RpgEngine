package fr.doryamy.rpgengine.dialogue.editor.selection;

import fr.doryamy.rpgengine.dialogue.DialogueKey;
import java.util.Objects;

/** Sélection d'un nouveau PNJ pour les triggers NPC d'un dialogue existant. */
public record EditDialogueTriggerNpcSelectionIntent(
        DialogueKey dialogueKey
) implements NpcSelectionIntent {
    public EditDialogueTriggerNpcSelectionIntent {
        Objects.requireNonNull(dialogueKey, "dialogueKey");
    }
}
