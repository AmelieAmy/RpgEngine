package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.DialogueKey;
import fr.doryamy.rpgengine.model.Trigger;
import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.npc.NpcService;
import fr.doryamy.rpgengine.trigger.TriggerQueryService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Résout les triggers d'un dialogue vers des données prêtes à présenter. */
public final class DialogueTriggerPresentationService {

    private final TriggerQueryService triggerQueryService;
    private final NpcService npcService;

    public DialogueTriggerPresentationService(
            TriggerQueryService triggerQueryService,
            NpcService npcService
    ) {
        this.triggerQueryService = Objects.requireNonNull(triggerQueryService, "triggerQueryService");
        this.npcService = Objects.requireNonNull(npcService, "npcService");
    }

    public List<DialogueTriggerPresentation> present(DialogueKey dialogueKey) {
        return triggerQueryService.findDialogTriggers(dialogueKey).stream()
                .map(this::presentTrigger)
                .sorted(Comparator.comparingInt(DialogueTriggerPresentation::id))
                .toList();
    }

    private DialogueTriggerPresentation presentTrigger(Trigger trigger) {
        return new DialogueTriggerPresentation(
                trigger.getId(),
                trigger.getName(),
                trigger.getType().name(),
                trigger.getTargetId(),
                resolveDisplayTarget(trigger)
        );
    }

    private String resolveDisplayTarget(Trigger trigger) {
        if (trigger.getType() == TriggerType.NPC) {
            return npcService.find(trigger.getTargetId())
                    .map(npc -> npc.name())
                    .orElse("NPC #" + trigger.getTargetId());
        }
        return trigger.getTargetId();
    }
}
