package fr.doryamy.rpgengine.dialogue;

import java.util.UUID;

/**
 * Générateur d'identités basé sur UUID.
 */
public final class UuidDialogueElementKeyGenerator
        implements DialogueElementKeyGenerator {

    @Override
    public DialogueElementKey generate() {

        return new DialogueElementKey(
                UUID.randomUUID().toString()
        );
    }
}