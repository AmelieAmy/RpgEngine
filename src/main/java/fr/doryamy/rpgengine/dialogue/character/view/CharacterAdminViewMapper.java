package fr.doryamy.rpgengine.dialogue.character.view;

import fr.doryamy.rpgengine.dialogue.DialogueCharacterProfile;
import fr.doryamy.rpgengine.npc.NpcService;
import java.util.List;
import java.util.Objects;

/** Construit la projection d'administration de la bibliothèque de personnages. */
public final class CharacterAdminViewMapper {
    private final NpcService npcService;
    public CharacterAdminViewMapper(NpcService npcService){this.npcService=Objects.requireNonNull(npcService,"npcService");}

    public CharacterAdminView present(List<DialogueCharacterProfile> profiles){
        Objects.requireNonNull(profiles,"profiles");
        List<CharacterAdminEntryView> characters=profiles.stream().map(this::presentProfile).toList();
        return new CharacterAdminView(characters, List.of());
    }
    private CharacterAdminEntryView presentProfile(DialogueCharacterProfile profile){
        String npcId=profile.citizensNpcId();
        var npc=npcId==null?java.util.Optional.<fr.doryamy.rpgengine.npc.NpcSummary>empty():npcService.find(npcId);
        String npcName=npc.map(summary->summary.name()).orElse(npcId==null?null:"Citizens #"+npcId);
        String displayName=npc.map(summary->summary.name()).orElse(profile.displayName());
        return new CharacterAdminEntryView(profile.key().value(),displayName,profile.portraitResource(),npcId,npcName);
    }
}
