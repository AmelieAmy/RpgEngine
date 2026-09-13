package fr.doryamy.rpgengine.dialogue.character;

import fr.doryamy.rpgengine.dialogue.DialogueCharacterProfileService;
import fr.doryamy.rpgengine.dialogue.character.view.CharacterAdminView;
import fr.doryamy.rpgengine.dialogue.character.view.CharacterAdminViewMapper;
import java.util.Objects;

/** Service de lecture de la bibliothèque globale de personnages. */
public final class CharacterAdminService {
    private final DialogueCharacterProfileService profileService;
    private final CharacterAdminViewMapper viewMapper;

    public CharacterAdminService(DialogueCharacterProfileService profileService, CharacterAdminViewMapper viewMapper) {
        this.profileService=Objects.requireNonNull(profileService,"profileService");
        this.viewMapper=Objects.requireNonNull(viewMapper,"viewMapper");
    }
    public CharacterAdminView present(){ return viewMapper.present(profileService.findAll()); }
}
