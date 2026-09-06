package fr.doryamy.rpgengine.dialogue;

import java.util.UUID;

/**
 * Générateur de clés de règles basé sur UUID.
 */
public final class UuidDialogueRuleKeyGenerator
        implements DialogueRuleKeyGenerator {

    @Override
    public DialogueRuleKey generate() {

        return new DialogueRuleKey(
                UUID.randomUUID().toString()
        );
    }
}