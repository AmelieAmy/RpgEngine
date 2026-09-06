package fr.doryamy.rpgengine.dialogue;

/**
 * Génère les identités stables des dialogues.
 */
@FunctionalInterface
public interface DialogueKeyGenerator {

    DialogueKey generate();
}