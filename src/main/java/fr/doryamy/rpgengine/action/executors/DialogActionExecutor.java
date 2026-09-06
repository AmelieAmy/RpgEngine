package fr.doryamy.rpgengine.action.executors;

import fr.doryamy.rpgengine.action.ActionExecutor;
import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueKey;
import fr.doryamy.rpgengine.dialogue.DialogueRunner;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.Objects;
import java.util.Optional;

/**
 * Déclenche un dialogue depuis une Action RPGEngine.
 *
 * <p>Provider :
 * DIALOG
 *
 * <p>L'expression contient exclusivement
 * la DialogueKey du dialogue à démarrer.
 */
public final class DialogActionExecutor
        implements ActionExecutor {

    private final DialogueService dialogueService;
    private final DialogueRunner dialogueRunner;

    public DialogActionExecutor(
            DialogueService dialogueService,
            DialogueRunner dialogueRunner
    ) {

        this.dialogueService =
                Objects.requireNonNull(
                        dialogueService,
                        "dialogueService"
                );

        this.dialogueRunner =
                Objects.requireNonNull(
                        dialogueRunner,
                        "dialogueRunner"
                );
    }

    @Override
    public String getProvider() {
        return "DIALOG";
    }

    @Override
    public void execute(
            TriggerContext context,
            Action action
    ) {

        Objects.requireNonNull(
                context,
                "context"
        );

        Objects.requireNonNull(
                action,
                "action"
        );

        DialogueKey dialogueKey;

        try {

            dialogueKey =
                    new DialogueKey(
                            action.getExpression()
                    );

        } catch (IllegalArgumentException e) {

            RpgLogger.error(
                    "Clé de dialogue invalide dans une action DIALOG : "
                            + action.getExpression()
            );

            return;
        }

        Optional<Dialogue> dialogue =
                dialogueService.find(
                        dialogueKey
                );

        if (dialogue.isEmpty()) {

            RpgLogger.error(
                    "Dialogue inconnu : "
                            + dialogueKey
            );

            return;
        }

        dialogueRunner.start(
                dialogue.get(),
                context
        );
    }
}