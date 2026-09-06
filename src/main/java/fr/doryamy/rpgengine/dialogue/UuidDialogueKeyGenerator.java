package fr.doryamy.rpgengine.dialogue;

import java.util.UUID;

/**
 * Générateur de clés de dialogue basé sur UUID.
 */
public final class UuidDialogueKeyGenerator
        implements DialogueKeyGenerator {

    @Override
    public DialogueKey generate() {

        return new DialogueKey(
                UUID.randomUUID().toString()
        );
    }
}