package fr.doryamy.rpgengine.trigger;

import fr.doryamy.rpgengine.dialogue.DialogueKey;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Trigger;
import fr.doryamy.rpgengine.repository.TriggerRepository;

import java.util.List;
import java.util.Objects;

/**
 * Service de lecture des triggers.
 *
 * <p>Il centralise notamment la convention générique
 * Action(provider=DIALOG, expression=DialogueKey) utilisée
 * pour relier un trigger à un dialogue.
 */
public final class TriggerQueryService {

    private static final String DIALOG_PROVIDER = "DIALOG";

    private final TriggerRepository triggerRepository;

    public TriggerQueryService(TriggerRepository triggerRepository) {
        this.triggerRepository = Objects.requireNonNull(triggerRepository, "triggerRepository");
    }

    public List<Trigger> findAll() {
        return List.copyOf(triggerRepository.findAll());
    }

    public List<Trigger> findDialogTriggers(DialogueKey dialogueKey) {
        Objects.requireNonNull(dialogueKey, "dialogueKey");
        String key = dialogueKey.value();

        return triggerRepository.findAll().stream()
                .filter(trigger -> trigger.getActions().stream()
                        .anyMatch(action -> targetsDialogue(action, key)))
                .toList();
    }

    private boolean targetsDialogue(Action action, String dialogueKey) {
        return DIALOG_PROVIDER.equals(action.getProvider())
                && dialogueKey.equals(action.getExpression());
    }
}
