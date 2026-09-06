package fr.doryamy.rpgengine.dialogue;

/**
 * Génère les identités des règles appartenant
 * aux éléments d'un dialogue.
 */
@FunctionalInterface
public interface DialogueRuleKeyGenerator {

    DialogueRuleKey generate();
}