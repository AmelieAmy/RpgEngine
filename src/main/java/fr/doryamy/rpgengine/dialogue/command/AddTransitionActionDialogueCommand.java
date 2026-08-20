package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Ajoute une action à une transition.
 *
 * Syntaxe :
 * /rpg dialog transition action add
 * <dialogueKey> <transitionKey> <provider> <expression>
 */
public final class AddTransitionActionDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public AddTransitionActionDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog transition action add";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length < 4) {

            sender.sendMessage(
                    "Usage : /rpg dialog transition action add "
                            + "<dialogueKey> <transitionKey> "
                            + "<provider> <expression>"
            );

            return false;
        }

        String dialogueKey =
                args[0];

        String transitionKey =
                args[1];

        String provider =
                args[2];

        String expression =
                String.join(
                        " ",
                        Arrays.copyOfRange(
                                args,
                                3,
                                args.length
                        )
                );

        CommandResult result =
                dialogueService.addTransitionAction(
                        dialogueKey,
                        transitionKey,
                        provider,
                        expression
                );

        sender.sendMessage(
                result.getMessage()
        );

        return result.isSuccess();
    }
}