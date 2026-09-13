package fr.doryamy.rpgengine.dialogue.character.portrait;

import java.util.Set;

/**
 * Port de stockage des données binaires des portraits gérés par RPGEngine.
 *
 * <p>Le domaine Character ne manipule jamais de chemin de fichier. Il conserve
 * uniquement une ressource logique produite par {@link CharacterPortraitAssetService}.
 */
public interface CharacterPortraitAssetStore {

    boolean exists(String assetId);

    void putIfAbsent(String assetId, byte[] pngBytes);

    byte[] read(String assetId);

    void delete(String assetId);

    Set<String> listAssetIds();
}
