package fr.doryamy.rpgengine.dialogue.character;

import fr.doryamy.rpgengine.bridge.NeoForgeBridge;
import fr.doryamy.rpgengine.dialogue.DialogueCharacterProfileKey;
import fr.doryamy.rpgengine.dialogue.DialogueCharacterProfileService;
import fr.doryamy.rpgengine.dialogue.editor.selection.DialogueAdminNpcSelectionService;
import fr.doryamy.rpgengine.dialogue.character.portrait.CharacterPortraitAssetService;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Orchestre les mutations d'administration de la bibliothèque de personnages. */
public final class CharacterAdminController {
    private final DialogueCharacterProfileService profileService;
    private final CharacterAdminService adminService;
    private final DialogueAdminNpcSelectionService npcSelectionService;
    private final CharacterPortraitAssetService portraitAssetService;
    private final NeoForgeBridge neoForgeBridge;

    public CharacterAdminController(
            DialogueCharacterProfileService profileService,
            CharacterAdminService adminService,
            DialogueAdminNpcSelectionService npcSelectionService,
            CharacterPortraitAssetService portraitAssetService,
            NeoForgeBridge neoForgeBridge
    ) {
        this.profileService = Objects.requireNonNull(profileService, "profileService");
        this.adminService = Objects.requireNonNull(adminService, "adminService");
        this.npcSelectionService = Objects.requireNonNull(npcSelectionService, "npcSelectionService");
        this.portraitAssetService = Objects.requireNonNull(portraitAssetService, "portraitAssetService");
        this.neoForgeBridge = Objects.requireNonNull(neoForgeBridge, "neoForgeBridge");
    }

    public void openAdmin(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        if (!neoForgeBridge.openCharacterAdmin(playerUuid, adminService.present()))
            RpgLogger.error("Impossible d'envoyer la bibliothèque de personnages au joueur " + playerUuid);
    }

    public void createCharacter(UUID playerUuid, Map<String, String> request) {
        try {
            String portraitResource = optional(request, "portraitResource");
            portraitAssetService.requireAvailableManagedReference(portraitResource);
            profileService.create(
                    required(request, "name"),
                    portraitResource,
                    optional(request, "citizensNpcId")
            );
        } catch (RuntimeException e) {
            RpgLogger.error("Impossible de créer le personnage : " + e.getMessage());
        }
        openAdmin(playerUuid);
    }

    public void updateCharacter(UUID playerUuid, Map<String, String> request) {
        try {
            DialogueCharacterProfileKey key =
                    new DialogueCharacterProfileKey(required(request, "key"));
            String previousPortrait = profileService.require(key).portraitResource();
            String portraitResource = optional(request, "portraitResource");

            portraitAssetService.requireAvailableManagedReference(portraitResource);
            profileService.update(
                    key,
                    required(request, "name"),
                    portraitResource,
                    optional(request, "citizensNpcId")
            );

            if (!Objects.equals(previousPortrait, portraitResource)) {
                portraitAssetService.deleteIfUnreferenced(
                        previousPortrait,
                        profileService.findAll()
                );
            }
        } catch (RuntimeException e) {
            RpgLogger.error("Impossible de modifier le personnage : " + e.getMessage());
        }
        openAdmin(playerUuid);
    }

    public void deleteCharacter(UUID playerUuid, String keyValue) {
        try {
            DialogueCharacterProfileKey key = new DialogueCharacterProfileKey(keyValue);
            String previousPortrait = profileService.require(key).portraitResource();
            profileService.delete(key);
            portraitAssetService.deleteIfUnreferenced(
                    previousPortrait,
                    profileService.findAll()
            );
        } catch (RuntimeException e) {
            RpgLogger.error(
                    "Impossible de supprimer le personnage "
                            + keyValue
                            + " : "
                            + e.getMessage()
            );
        }
        openAdmin(playerUuid);
    }

    public void uploadPortrait(UUID playerUuid, byte[] imageBytes) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(imageBytes, "imageBytes");

        try {
            String portraitResource = portraitAssetService.importPortrait(imageBytes);
            if (!neoForgeBridge.showCharacterPortraitUploadResult(
                    playerUuid,
                    true,
                    portraitResource,
                    "Portrait importé."
            )) {
                RpgLogger.error(
                        "Impossible de renvoyer le portrait importé au joueur "
                                + playerUuid
                );
            }
        } catch (RuntimeException e) {
            RpgLogger.error("Impossible d'importer le portrait : " + e.getMessage());
            neoForgeBridge.showCharacterPortraitUploadResult(
                    playerUuid,
                    false,
                    "",
                    e.getMessage() == null ? "Import du portrait impossible." : e.getMessage()
            );
        }
    }

    public void beginNpcSelection(UUID playerUuid) {
        npcSelectionService.beginCharacterNpc(Objects.requireNonNull(playerUuid, "playerUuid"));
    }

    public void completeNpcSelection(UUID playerUuid, String npcId, String npcName) {
        if (!neoForgeBridge.showCharacterNpcSelectionResult(playerUuid, npcId, npcName))
            RpgLogger.error("Impossible de renvoyer le PNJ Citizens sélectionné au joueur " + playerUuid + " | npcId=" + npcId);
    }

    private String required(Map<String,String> request, String key) {
        Objects.requireNonNull(request,"request"); String value=request.get(key);
        if (value==null || value.isBlank()) throw new IllegalArgumentException("Champ requis manquant : " + key);
        return value.trim();
    }
    private String optional(Map<String,String> request, String key) {
        String value=request.get(key); if(value==null) return null; value=value.trim(); return value.isEmpty()?null:value;
    }
}
