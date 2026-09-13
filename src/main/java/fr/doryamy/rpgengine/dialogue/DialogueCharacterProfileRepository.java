package fr.doryamy.rpgengine.dialogue;

import java.util.List;
import java.util.Optional;

/** Port de persistance de la bibliothèque globale des profils de personnages. */
public interface DialogueCharacterProfileRepository {
    Optional<DialogueCharacterProfile> findByKey(DialogueCharacterProfileKey key);
    Optional<DialogueCharacterProfile> findByCitizensNpcId(String citizensNpcId);
    List<DialogueCharacterProfile> findAll();
    void insert(DialogueCharacterProfile profile);
    void update(DialogueCharacterProfile profile);
    void delete(DialogueCharacterProfileKey key);
}
