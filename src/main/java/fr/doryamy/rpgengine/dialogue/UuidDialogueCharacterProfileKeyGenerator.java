package fr.doryamy.rpgengine.dialogue;

import java.util.UUID;

public final class UuidDialogueCharacterProfileKeyGenerator
        implements DialogueCharacterProfileKeyGenerator {

    @Override
    public DialogueCharacterProfileKey generate() {
        return new DialogueCharacterProfileKey(
                UUID.randomUUID().toString()
        );
    }
}
