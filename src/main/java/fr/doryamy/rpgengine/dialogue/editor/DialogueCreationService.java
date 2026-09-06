package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.trigger.TriggerService;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Orchestre la création métier d'un dialogue
 * déclenché par un PNJ Citizens.
 *
 * <p>Le client ne connaît pas la décomposition interne :
 * le plugin crée le Dialogue, le Trigger NPC et l'Action
 * DIALOG qui relie le trigger au dialogue.
 */
public final class DialogueCreationService {

    private static final String DIALOG_ACTION_PROVIDER =
            "DIALOG";

    private final DialogueService dialogueService;
    private final TriggerService triggerService;

    public DialogueCreationService(
            DialogueService dialogueService,
            TriggerService triggerService
    ) {

        this.dialogueService =
                Objects.requireNonNull(
                        dialogueService,
                        "dialogueService"
                );

        this.triggerService =
                Objects.requireNonNull(
                        triggerService,
                        "triggerService"
                );
    }

    /**
     * Crée un dialogue et son trigger NPC.
     *
     * <p>Le nom du trigger reprend actuellement
     * le nom du dialogue. L'identité du PNJ est portée
     * exclusivement par targetId.
     *
     * <p>Si la création du trigger échoue, le dialogue
     * créé juste avant est supprimé afin de ne pas laisser
     * un agrégat orphelin.
     */
    public Dialogue createNpcDialogue(
            String dialogueName,
            String npcId
    ) {

        Objects.requireNonNull(
                dialogueName,
                "dialogueName"
        );

        Objects.requireNonNull(
                npcId,
                "npcId"
        );

        String normalizedName =
                dialogueName.trim();

        String normalizedNpcId =
                npcId.trim();

        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom du dialogue ne peut pas être vide."
            );
        }

        if (normalizedNpcId.isBlank()) {
            throw new IllegalArgumentException(
                    "L'identifiant du PNJ ne peut pas être vide."
            );
        }

        Dialogue dialogue =
                dialogueService.create(
                        normalizedName
                );

        Action dialogAction =
                new Action(
                        DIALOG_ACTION_PROVIDER,
                        dialogue.key().value(),
                        0
                );

        OptionalInt triggerId =
                triggerService.create(
                        normalizedName,
                        TriggerType.NPC,
                        normalizedNpcId,
                        List.of(),
                        List.of(
                                dialogAction
                        )
                );

        if (triggerId.isPresent()) {
            return dialogue;
        }

        IllegalStateException failure =
                new IllegalStateException(
                        "Impossible de créer le trigger NPC du dialogue "
                                + dialogue.key()
                                + "."
                );

        try {
            dialogueService.delete(
                    dialogue.key()
            );
        } catch (RuntimeException cleanupFailure) {
            failure.addSuppressed(
                    cleanupFailure
            );
        }

        throw failure;
    }
}
