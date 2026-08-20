package fr.doryamy.rpgengine.dialogue.editor.view;

/**
 * Représentation éditoriale du déclencheur
 * associé au scénario.
 *
 * @param type        type technique du trigger
 * @param targetId    identifiant de la cible
 * @param displayName nom lisible affiché dans l'interface
 */
public record DialogueEditorTriggerView(
        String type,
        String targetId,
        String displayName
) {
}