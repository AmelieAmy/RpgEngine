package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

/**
 * Ajoute un node à un dialogue.
 *
 * Syntaxe :
 * /rpg dialog node add <dialogueKey> <nodeKey> <text>
 *
 * Exemple :
 * /rpg dialog node add chief_intro intro Bonjour aventurier !
 */
public final class AddNodeDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public AddNodeDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService = dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog node add";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length < 3) {
            sender.sendMessage(
                    "Usage : /rpg dialog node add <dialogueKey> <nodeKey> <text>"
            );

            return false;
        }

        String dialogueKey = args[0];
        String nodeKey = args[1];

        StringBuilder text = new StringBuilder();

        for (int i = 2; i < args.length; i++) {
            if (i > 2) {
                text.append(" ");
            }

            text.append(args[i]);
        }

        CommandResult result =
                dialogueService.addNode(
                        dialogueKey,
                        nodeKey,
                        text.toString()
                );

        sender.sendMessage(
                result.getMessage()
        );

        return result.isSuccess();
    }
}