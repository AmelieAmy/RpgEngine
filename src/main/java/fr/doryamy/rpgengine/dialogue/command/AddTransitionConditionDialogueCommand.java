package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Ajoute une condition à une transition.
 *
 * Syntaxe :
 * /rpg dialog transition condition add
 * <dialogueKey> <transitionKey> <provider> <expression>
 */
public final class AddTransitionConditionDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public AddTransitionConditionDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog transition condition add";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length < 4) {

            sender.sendMessage(
                    "Usage : /rpg dialog transition condition add "
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
                dialogueService.addTransitionCondition(
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