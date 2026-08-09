package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

/**
 * Ajoute une transition à un dialogue.
 *
 * La transition créée est initialement
 * de type END.
 *
 * Syntaxe :
 * /rpg dialog transition add
 * <dialogueKey> <sourceNodeKey> <transitionKey>
 *
 * Exemple :
 * /rpg dialog transition add
 * chief_intro intro intro_end
 */
public final class AddTransitionDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public AddTransitionDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog transition add";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 3) {

            sender.sendMessage(
                    "Usage : /rpg dialog transition add "
                            + "<dialogueKey> <sourceNodeKey> <transitionKey>"
            );

            return false;
        }

        CommandResult result =
                dialogueService.addTransition(
                        args[0],
                        args[1],
                        args[2]
                );

        sender.sendMessage(
                result.getMessage()
        );

        return result.isSuccess();
    }
}