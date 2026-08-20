package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Modifie le texte d'un node existant.
 *
 * Syntaxe :
 * /rpg dialog node text
 * <dialogueKey> <nodeKey> <text>
 *
 * Exemple :
 * /rpg dialog node text
 * chief_chickens chief_greeting Bonjour à nouveau aventurier !
 */
public final class UpdateNodeTextDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public UpdateNodeTextDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog node text";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length < 3) {

            sender.sendMessage(
                    "Usage : /rpg dialog node text "
                            + "<dialogueKey> <nodeKey> <text>"
            );

            return false;
        }

        String dialogueKey =
                args[0];

        String nodeKey =
                args[1];

        String text =
                String.join(
                        " ",
                        Arrays.copyOfRange(
                                args,
                                2,
                                args.length
                        )
                );

        CommandResult result =
                dialogueService.updateNodeText(
                        dialogueKey,
                        nodeKey,
                        text
                );

        sender.sendMessage(
                result.getMessage()
        );

        return result.isSuccess();
    }
}