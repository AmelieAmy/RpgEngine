package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.*;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.trigger.TriggerService;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Orchestre la création métier d'un dialogue déclenché par un PNJ Citizens.
 *
 * <p>La création du dialogue ne crée pas de personnage. Le profil PNJ par
 * défaut est résolu depuis le trigger au moment où une réplique PNJ est
 * effectivement ajoutée au dialogue.
 */
public final class DialogueCreationService {

    private static final String DIALOG_ACTION_PROVIDER = "DIALOG";
    private static final DialogueParticipantKey PLAYER_KEY =
            new DialogueParticipantKey("player");

    private final DialogueService dialogueService;
    private final TriggerService triggerService;

    public DialogueCreationService(
            DialogueService dialogueService,
            TriggerService triggerService
    ) {
        this.dialogueService =
                Objects.requireNonNull(dialogueService, "dialogueService");
        this.triggerService =
                Objects.requireNonNull(triggerService, "triggerService");
    }

    public Dialogue createNpcDialogue(
            String dialogueName,
            String npcId
    ) {
        String normalizedName =
                requireText(dialogueName, "Le nom du dialogue");
        String normalizedNpcId =
                requireText(npcId, "L'identifiant du PNJ");

        Dialogue dialogue = null;

        try {
            DialogueParticipants participants =
                    new DialogueParticipants(
                            List.of(
                                    new DialogueParticipant(
                                            PLAYER_KEY,
                                            DialogueParticipantType.PLAYER,
                                            null
                                    )
                            )
                    );

            dialogue =
                    dialogueService.create(
                            normalizedName,
                            participants
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
                            List.of(dialogAction)
                    );

            if (triggerId.isPresent()) {
                return dialogue;
            }

            throw new IllegalStateException(
                    "Impossible de créer le trigger NPC du dialogue "
                            + dialogue.key() + "."
            );

        } catch (RuntimeException failure) {
            if (dialogue != null) {
                try {
                    dialogueService.delete(
                            dialogue.key()
                    );
                } catch (RuntimeException cleanupFailure) {
                    failure.addSuppressed(
                            cleanupFailure
                    );
                }
            }

            throw failure;
        }
    }

    private String requireText(
            String value,
            String label
    ) {
        Objects.requireNonNull(
                value,
                label
        );

        String normalized =
                value.trim();

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                    label + " ne peut pas être vide."
            );
        }

        return normalized;
    }
}
