package fr.doryamy.rpgengine.dialogue.editor.selection;

/** Intention serveur associée à une sélection Citizens en cours. */
public sealed interface NpcSelectionIntent
        permits CreateDialogueNpcSelectionIntent,
        EditDialogueTriggerNpcSelectionIntent {
}
