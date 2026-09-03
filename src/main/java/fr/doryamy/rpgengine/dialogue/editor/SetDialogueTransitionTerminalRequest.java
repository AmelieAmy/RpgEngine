package fr.doryamy.rpgengine.dialogue.editor;

/**
 * Intention métier provenant de l'éditeur :
 * terminer une transition existante.
 *
 * <p>Le client ne fournit volontairement que les identifiants.
 * Le plugin conserve le type, le label, les conditions,
 * les actions et les éventuelles répliques Joueur.
 */
public record SetDialogueTransitionTerminalRequest(
        String dialogueKey,
        String transitionKey
) {
}
