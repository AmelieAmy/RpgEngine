package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.DialogueKey;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import fr.doryamy.rpgengine.trigger.TriggerService;

import java.util.Objects;

/**
 * Orchestre la suppression métier d'un dialogue.
 *
 * <p>Les références provenant du moteur de triggers sont retirées avant
 * l'agrégat Dialogue afin qu'aucune action DIALOG ne puisse conserver une
 * clé devenue inexistante.
 */
public final class DialogueDeletionService {

    private static final String DIALOG_PROVIDER = "DIALOG";

    private final DialogueService dialogueService;
    private final TriggerService triggerService;

    public DialogueDeletionService(
            DialogueService dialogueService,
            TriggerService triggerService
    ) {
        this.dialogueService = Objects.requireNonNull(dialogueService, "dialogueService");
        this.triggerService = Objects.requireNonNull(triggerService, "triggerService");
    }

    public void delete(DialogueKey dialogueKey) {
        Objects.requireNonNull(dialogueKey, "dialogueKey");

        // Vérifie l'existence avant toute mutation des références externes.
        dialogueService.require(dialogueKey);

        if (!triggerService.removeActionReferences(
                DIALOG_PROVIDER,
                dialogueKey.value()
        )) {
            throw new IllegalStateException(
                    "Impossible de supprimer les références de trigger du dialogue "
                            + dialogueKey + "."
            );
        }

        dialogueService.delete(dialogueKey);
    }
}
