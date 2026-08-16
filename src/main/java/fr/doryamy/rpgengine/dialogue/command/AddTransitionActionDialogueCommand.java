package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

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

        StringBuilder expression =
                new StringBuilder();

        for (int i = 3; i < args.length; i++) {

            if (i > 3) {
                expression.append(" ");
            }

            expression.append(
                    args[i]
            );
        }

        CommandResult result =
                dialogueService.addTransitionAction(
                        dialogueKey,
                        transitionKey,
                        provider,
                        expression.toString()
                );

        sender.sendMessage(
                result.getMessage()
        );

        return result.isSuccess();
    }
}