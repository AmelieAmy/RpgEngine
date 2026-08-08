package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

/**
 * Crée un nouveau dialogue.
 *
 * Syntaxe :
 * /rpg dialog create <key> <name>
 *
 * Exemple :
 * /rpg dialog create chief_test Dialogue de test
 */
public final class CreateDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public CreateDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog create";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length < 2) {
            sender.sendMessage(
                    "Usage : /rpg dialog create <key> <name>"
            );

            return false;
        }

        String key =
                args[0];

        StringBuilder name =
                new StringBuilder();

        for (int i = 1; i < args.length; i++) {

            if (i > 1) {
                name.append(" ");
            }

            name.append(args[i]);
        }

        CommandResult result =
                dialogueService.create(
                        key,
                        name.toString()
                );

        sender.sendMessage(
                result.getMessage()
        );

        return result.isSuccess();
    }
}