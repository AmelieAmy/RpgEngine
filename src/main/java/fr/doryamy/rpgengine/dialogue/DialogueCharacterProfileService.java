package fr.doryamy.rpgengine.dialogue;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Cycle de vie de la bibliothèque globale de personnages. */
public final class DialogueCharacterProfileService {
    private final DialogueCharacterProfileRepository repository;
    private final DialogueCharacterProfileKeyGenerator keyGenerator;

    public DialogueCharacterProfileService(DialogueCharacterProfileRepository repository, DialogueCharacterProfileKeyGenerator keyGenerator) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.keyGenerator = Objects.requireNonNull(keyGenerator, "keyGenerator");
    }

    public DialogueCharacterProfile create(String displayName, String portraitResource, String citizensNpcId) {
        DialogueCharacterProfileKey key = Objects.requireNonNull(keyGenerator.generate(), "Le générateur de clés a retourné null.");
        if (repository.findByKey(key).isPresent()) throw new IllegalStateException("Un profil existe déjà avec la clé " + key + ".");
        String npcId = normalizeNpcId(citizensNpcId);
        if (npcId != null && repository.findByCitizensNpcId(npcId).isPresent()) {
            throw new IllegalStateException("Un profil est déjà associé au PNJ Citizens " + npcId + ".");
        }
        DialogueCharacterProfile profile = new DialogueCharacterProfile(key, displayName, portraitResource, npcId);
        repository.insert(profile);
        return profile;
    }

    public DialogueCharacterProfile create(String displayName, String portraitResource) { return create(displayName, portraitResource, null); }

    public Optional<DialogueCharacterProfile> findByCitizensNpcId(String citizensNpcId) {
        String npcId = normalizeNpcId(citizensNpcId);
        return npcId == null ? Optional.empty() : repository.findByCitizensNpcId(npcId);
    }

    public Optional<DialogueCharacterProfile> find(DialogueCharacterProfileKey key) { return repository.findByKey(Objects.requireNonNull(key, "key")); }
    public DialogueCharacterProfile require(DialogueCharacterProfileKey key) { return find(key).orElseThrow(() -> new IllegalArgumentException("Profil introuvable : " + key)); }
    public List<DialogueCharacterProfile> findAll() { return List.copyOf(repository.findAll()); }

    public DialogueCharacterProfile update(DialogueCharacterProfileKey key, String displayName, String portraitResource, String citizensNpcId) {
        require(key);
        String npcId = normalizeNpcId(citizensNpcId);
        if (npcId != null) repository.findByCitizensNpcId(npcId).filter(existing -> !existing.key().equals(key)).ifPresent(existing -> {
            throw new IllegalStateException("Un profil est déjà associé au PNJ Citizens " + npcId + ".");
        });
        DialogueCharacterProfile updated = new DialogueCharacterProfile(key, displayName, portraitResource, npcId);
        repository.update(updated);
        return updated;
    }

    public DialogueCharacterProfile update(DialogueCharacterProfileKey key, String displayName, String portraitResource) {
        DialogueCharacterProfile current = require(key);
        return update(key, displayName, portraitResource, current.citizensNpcId());
    }

    public void delete(DialogueCharacterProfileKey key) { require(key); repository.delete(key); }

    private String normalizeNpcId(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty()) return null;
        try {
            int id = Integer.parseInt(normalized);
            if (id < 0) throw new IllegalArgumentException("L'identifiant Citizens doit être positif ou nul.");
            return Integer.toString(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Identifiant Citizens invalide : " + value, e);
        }
    }
}
