package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

/**
 * Supprime un dialogue.
 *
 * Syntaxe :
 * /rpg dialog delete <dialogueKey>
 *
 * Exemple :
 * /rpg dialog delete chief_test
 */
public final class DeleteDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public DeleteDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog delete";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 1) {

            sender.sendMessage(
                    "Usage : /rpg dialog delete <dialogueKey>"
            );

            return false;
        }

        CommandResult result =
                dialogueService.delete(
                        args[0]
                );

        sender.sendMessage(
                result.getMessage()
        );

        return result.isSuccess();
    }
}