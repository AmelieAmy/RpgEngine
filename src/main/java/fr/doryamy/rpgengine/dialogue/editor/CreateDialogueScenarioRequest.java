package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.quest.QuestState;

/**
 * Données nécessaires à la création
 * d'un nouveau scénario depuis l'éditeur.
 *
 * <p>La clé technique du dialogue n'est pas fournie
 * par le client. Elle est générée côté plugin.
 */
public record CreateDialogueScenarioRequest(
        String name,
        TriggerType triggerType,
        String targetId,
        String questId,
        QuestState initialState
) {
}