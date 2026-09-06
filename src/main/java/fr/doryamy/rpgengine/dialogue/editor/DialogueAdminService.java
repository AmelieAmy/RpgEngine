package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueAdminTriggerView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueAdminView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueAdminViewMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Construit l'état de lecture nécessaire à la liste d'administration. */
public final class DialogueAdminService {

    private final DialogueService dialogueService;
    private final DialogueTriggerPresentationService triggerPresentationService;
    private final DialogueAdminViewMapper viewMapper;

    public DialogueAdminService(
            DialogueService dialogueService,
            DialogueTriggerPresentationService triggerPresentationService,
            DialogueAdminViewMapper viewMapper
    ) {
        this.dialogueService = Objects.requireNonNull(dialogueService, "dialogueService");
        this.triggerPresentationService = Objects.requireNonNull(triggerPresentationService, "triggerPresentationService");
        this.viewMapper = Objects.requireNonNull(viewMapper, "viewMapper");
    }

    public DialogueAdminView present() {
        List<Dialogue> dialogues = dialogueService.findAll();
        Map<String, List<DialogueAdminTriggerView>> triggersByDialogueKey = new HashMap<>();

        for (Dialogue dialogue : dialogues) {
            List<DialogueAdminTriggerView> triggerViews =
                    triggerPresentationService.present(dialogue.key()).stream()
                            .map(trigger -> new DialogueAdminTriggerView(
                                    trigger.id(),
                                    trigger.name(),
                                    trigger.type(),
                                    trigger.targetId(),
                                    trigger.displayTarget()
                            ))
                            .toList();

            triggersByDialogueKey.put(dialogue.key().value(), triggerViews);
        }

        return viewMapper.present(dialogues, triggersByDialogueKey);
    }
}
