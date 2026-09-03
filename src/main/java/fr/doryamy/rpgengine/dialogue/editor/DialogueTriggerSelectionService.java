package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.editor.DialogueScenarioResolver.QuestBinding;
import fr.doryamy.rpgengine.model.Trigger;
import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.repository.TriggerRepository;
import fr.doryamy.rpgengine.trigger.TriggerService;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DialogueTriggerSelectionService {

    private final TriggerService triggerService;
    private final DialogueScenarioResolver scenarioResolver;

    private final Map<UUID, SelectionSession> sessions =
            new HashMap<>();

    public DialogueTriggerSelectionService(
            TriggerRepository triggerRepository,
            TriggerService triggerService
    ) {
        this.triggerService =
                triggerService;

        this.scenarioResolver =
                new DialogueScenarioResolver(
                        triggerRepository
                );
    }

    public boolean startSelection(
            UUID playerUuid,
            String dialogueKey
    ) {
        if (playerUuid == null
                || dialogueKey == null
                || dialogueKey.isBlank()) {

            return false;
        }

        Optional<Trigger> triggerResult =
                scenarioResolver.findTriggerForDialogue(
                        dialogueKey
                );

        if (triggerResult.isEmpty()) {
            return false;
        }

        Trigger referenceTrigger =
                triggerResult.get();

        Optional<QuestBinding> questBindingResult =
                scenarioResolver.findQuestBinding(
                        referenceTrigger
                );

        List<Trigger> scenarioTriggers =
                questBindingResult.isPresent()
                        ? scenarioResolver.findScenarioTriggers(
                        referenceTrigger,
                        questBindingResult.get()
                )
                        : List.of(
                        referenceTrigger
                );

        List<Integer> triggerIds =
                scenarioTriggers.stream()
                        .map(
                                Trigger::getId
                        )
                        .toList();

        if (triggerIds.isEmpty()) {
            return false;
        }

        sessions.put(
                playerUuid,
                new SelectionSession(
                        dialogueKey,
                        triggerIds
                )
        );

        return true;
    }

    public boolean isSelecting(
            UUID playerUuid
    ) {
        return sessions.containsKey(
                playerUuid
        );
    }

    public Optional<SelectionResult> selectNpc(
            UUID playerUuid,
            String npcId
    ) {
        SelectionSession session =
                sessions.get(
                        playerUuid
                );

        if (session == null
                || npcId == null
                || npcId.isBlank()) {

            return Optional.empty();
        }

        boolean updated =
                triggerService.updateTargets(
                        session.triggerIds(),
                        TriggerType.NPC,
                        npcId
                );

        if (!updated) {

            RpgLogger.error(
                    "Impossible de modifier le déclencheur du scénario "
                            + session.dialogueKey()
                            + " vers NPC "
                            + npcId
            );

            /*
             * La session reste active afin que le créateur
             * puisse simplement essayer un autre clic droit.
             */
            return Optional.empty();
        }

        sessions.remove(
                playerUuid
        );

        return Optional.of(
                new SelectionResult(
                        session.dialogueKey(),
                        npcId
                )
        );
    }

    public void cancelSelection(
            UUID playerUuid
    ) {
        sessions.remove(
                playerUuid
        );
    }

    private record SelectionSession(
            String dialogueKey,
            List<Integer> triggerIds
    ) {
    }

    public record SelectionResult(
            String dialogueKey,
            String npcId
    ) {
    }
}