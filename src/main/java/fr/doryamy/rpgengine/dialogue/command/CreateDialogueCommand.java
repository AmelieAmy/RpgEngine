package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

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

        String name =
                String.join(
                        " ",
                        Arrays.copyOfRange(
                                args,
                                1,
                                args.length
                        )
                );

        CommandResult result =
                dialogueService.create(
                        key,
                        name
                );

        sender.sendMessage(
                result.getMessage()
        );

        return result.isSuccess();
    }
}