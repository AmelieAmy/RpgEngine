package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import fr.doryamy.rpgengine.model.Action;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

/**
 * Affiche les actions associées
 * à une transition.
 *
 * Syntaxe :
 * /rpg dialog transition action list
 * <dialogueKey> <transitionKey>
 */
public final class ListTransitionActionDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public ListTransitionActionDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog transition action list";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 2) {

            sender.sendMessage(
                    "Usage : /rpg dialog transition action list "
                            + "<dialogueKey> <transitionKey>"
            );

            return false;
        }

        String dialogueKey =
                args[0];

        String transitionKey =
                args[1];

        Optional<List<Action>> result =
                dialogueService.getTransitionActions(
                        dialogueKey,
                        transitionKey
                );

        if (result.isEmpty()) {

            sender.sendMessage(
                    "Dialogue ou transition introuvable."
            );

            return false;
        }

        List<Action> actions =
                result.get();

        if (actions.isEmpty()) {

            sender.sendMessage(
                    "Aucune action sur la transition '"
                            + transitionKey
                            + "'."
            );

            return true;
        }

        sender.sendMessage(
                "Actions de la transition '"
                        + transitionKey
                        + "' :"
        );

        for (Action action : actions) {

            sender.sendMessage(
                    action.getPosition()
                            + " - "
                            + action.getProvider()
                            + " | "
                            + action.getExpression()
            );
        }

        return true;
    }
}