package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.quest.QuestState;

/**
 * Demande métier de changement de variante
 * d'état dans l'éditeur de dialogue.
 *
 * @param dialogueKey dialogue actuellement affiché
 * @param targetState état de quête cible
 */
public record SwitchDialogueEditorStateRequest(
        String dialogueKey,
        QuestState targetState
) {
}