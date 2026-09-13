package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.*;
import fr.doryamy.rpgengine.model.Trigger;
import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.npc.NpcService;
import fr.doryamy.rpgengine.npc.NpcSummary;
import fr.doryamy.rpgengine.trigger.TriggerQueryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Résout et maintient le participant PNJ par défaut d'un dialogue NPC. */
public final class DialogueReplyParticipantInitializationService {
    private final DialogueService dialogueService;
    private final DialogueCharacterProfileService characterProfileService;
    private final TriggerQueryService triggerQueryService;
    private final NpcService npcService;

    public DialogueReplyParticipantInitializationService(DialogueService dialogueService,
                                                         DialogueCharacterProfileService characterProfileService, TriggerQueryService triggerQueryService,
                                                         NpcService npcService) {
        this.dialogueService = Objects.requireNonNull(dialogueService,"dialogueService");
        this.characterProfileService = Objects.requireNonNull(characterProfileService,"characterProfileService");
        this.triggerQueryService = Objects.requireNonNull(triggerQueryService,"triggerQueryService");
        this.npcService = Objects.requireNonNull(npcService,"npcService");
    }

    public DialogueParticipant resolveDefaultNpcParticipant(DialogueKey dialogueKey) {
        DialogueCharacterProfile profile = resolveProfile(requireSingleNpcTrigger(dialogueKey).getTargetId());
        Dialogue dialogue = dialogueService.require(dialogueKey);

        List<DialogueParticipant> matchingParticipants = npcParticipants(dialogue).stream()
                .filter(participant -> profile.key().equals(participant.characterProfileKey()))
                .toList();

        if (matchingParticipants.size() > 1) {
            throw new IllegalStateException(
                    "Plusieurs participants NPC utilisent le profil par défaut "
                            + profile.key() + " dans le dialogue " + dialogueKey + "."
            );
        }

        if (matchingParticipants.size() == 1) {
            return matchingParticipants.getFirst();
        }

        DialogueParticipant participant = new DialogueParticipant(
                nextParticipantKey(dialogue, profile.key()),
                DialogueParticipantType.NPC,
                profile.key()
        );

        List<DialogueParticipant> participants = new ArrayList<>(dialogue.participants().values());
        participants.add(participant);
        dialogueService.replaceParticipants(dialogueKey, new DialogueParticipants(participants));
        return participant;
    }

    /**
     * Réaffecte le participant NPC par défaut uniquement lorsque le dialogue
     * n'en possède qu'un. Dès que plusieurs personnages explicites existent,
     * aucun participant n'est déduit comme "par défaut" : la sélection faite
     * sur les Reply reste alors autoritaire.
     */
    public void rebindDefaultNpcParticipant(DialogueKey dialogueKey, String citizensNpcId) {
        DialogueCharacterProfile profile = resolveProfile(citizensNpcId);
        Dialogue dialogue = dialogueService.require(dialogueKey);
        List<DialogueParticipant> npcParticipants = npcParticipants(dialogue);

        if (npcParticipants.isEmpty()) return;
        if (npcParticipants.size() > 1) return;

        DialogueParticipant current = npcParticipants.getFirst();
        if (profile.key().equals(current.characterProfileKey())) return;

        List<DialogueParticipant> participants = dialogue.participants().values().stream()
                .map(participant -> participant.key().equals(current.key())
                        ? new DialogueParticipant(
                        participant.key(),
                        participant.type(),
                        profile.key()
                )
                        : participant)
                .toList();

        dialogueService.replaceParticipants(
                dialogueKey,
                new DialogueParticipants(participants)
        );
    }

    private DialogueCharacterProfile resolveProfile(String citizensNpcId) {
        NpcSummary npc = npcService.find(citizensNpcId).orElseThrow(() ->
                new IllegalStateException("PNJ Citizens introuvable : " + citizensNpcId));
        return characterProfileService.findByCitizensNpcId(npc.id())
                .orElseGet(() -> characterProfileService.create(npc.name(), null, npc.id()));
    }

    private Trigger requireSingleNpcTrigger(DialogueKey dialogueKey) {
        List<Trigger> triggers = triggerQueryService.findDialogTriggers(dialogueKey);
        if (triggers.size()!=1) throw new IllegalStateException("L'initialisation d'une réplique PNJ exige exactement un trigger associé au dialogue " + dialogueKey + ". Trouvés : " + triggers.size() + ".");
        Trigger trigger=triggers.get(0);
        if(trigger.getType()!=TriggerType.NPC) throw new IllegalStateException("Le trigger associé au dialogue " + dialogueKey + " n'est pas de type NPC.");
        return trigger;
    }

    private List<DialogueParticipant> npcParticipants(Dialogue dialogue) {
        return dialogue.participants().values().stream().filter(p -> p.type()==DialogueParticipantType.NPC).toList();
    }
    private DialogueParticipantKey nextParticipantKey(Dialogue dialogue, DialogueCharacterProfileKey profileKey) {
        String base="npc-"+profileKey.value(), candidate=base; int suffix=2;
        while(dialogue.participants().find(new DialogueParticipantKey(candidate)).isPresent()) candidate=base+"-"+suffix++;
        return new DialogueParticipantKey(candidate);
    }
}
