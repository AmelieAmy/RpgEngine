package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

/**
 * Définit le node de départ d'un dialogue.
 *
 * Syntaxe :
 * /rpg dialog start <dialogueKey> <nodeKey>
 */
public final class SetStartNodeDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public SetStartNodeDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog start";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 2) {

            sender.sendMessage(
                    "Usage : /rpg dialog start <dialogueKey> <nodeKey>"
            );

            return false;
        }

        CommandResult result =
                dialogueService.setStartNode(
                        args[0],
                        args[1]
                );

        sender.sendMessage(
                result.getMessage()
        );

        return result.isSuccess();
    }
}