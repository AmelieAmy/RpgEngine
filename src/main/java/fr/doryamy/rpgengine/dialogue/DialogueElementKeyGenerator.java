package fr.doryamy.rpgengine.dialogue;

/**
 * Génère les identités techniques stables
 * des éléments d'un dialogue.
 */
@FunctionalInterface
public interface DialogueElementKeyGenerator {

    DialogueElementKey generate();
}