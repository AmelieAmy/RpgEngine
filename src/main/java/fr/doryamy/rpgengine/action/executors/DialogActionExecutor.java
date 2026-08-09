package fr.doryamy.rpgengine.action.executors;

import fr.doryamy.rpgengine.action.ActionExecutor;
import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueRepository;
import fr.doryamy.rpgengine.dialogue.DialogueRunner;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.Optional;

/**
 * Executor responsable du déclenchement d'un dialogue depuis une action.
 * Cette action correspond au provider "DIALOG".
 * L'expression contient la clé métier du dialogue à exécuter.
 * L'affichage lui-même est délégué à DialogueRunner.
 */
public final class DialogActionExecutor
        implements ActionExecutor {

    private final DialogueRepository repository;
    private final DialogueRunner dialogueRunner;

    public DialogActionExecutor(
            DialogueRepository repository,
            DialogueRunner dialogueRunner
    ) {
        this.repository = repository;
        this.dialogueRunner = dialogueRunner;
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
        String dialogueKey =
                action.getExpression();

        Optional<Dialogue> result =
                repository.findByKey(
                        dialogueKey
                );

        if (result.isEmpty()) {

            RpgLogger.error(
                    "Dialogue inconnu : "
                            + dialogueKey
            );

            return;
        }

        dialogueRunner.start(
                context,
                result.get()
        );
    }
}