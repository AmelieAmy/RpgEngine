package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

/**
 * Supprime une action d'une transition.
 *
 * Syntaxe :
 * /rpg dialog transition action remove
 * <dialogueKey> <transitionKey> <position>
 */
public final class RemoveTransitionActionDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public RemoveTransitionActionDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog transition action remove";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 3) {

            sender.sendMessage(
                    "Usage : /rpg dialog transition action remove "
                            + "<dialogueKey> <transitionKey> <position>"
            );

            return false;
        }

        int position;

        try {
            position =
                    Integer.parseInt(
                            args[2]
                    );

        } catch (NumberFormatException e) {

            sender.sendMessage(
                    "La position doit être un nombre."
            );

            return false;
        }

        CommandResult result =
                dialogueService.removeTransitionAction(
                        args[0],
                        args[1],
                        position
                );

        sender.sendMessage(
                result.getMessage()
        );

        return result.isSuccess();
    }
}