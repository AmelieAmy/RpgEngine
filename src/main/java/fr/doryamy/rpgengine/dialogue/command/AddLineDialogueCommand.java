package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

/**
 * Ajoute une ligne à la fin d'un dialogue.
 *
 * Syntaxe :
 * /rpg dialog addline <key> <text>
 *
 * Exemple :
 * /rpg dialog addline chief_intro Bienvenue aventurier !
 */
public final class AddLineDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    /**
     * Construit la commande.
     *
     * @param dialogueService service des dialogues
     */
    public AddLineDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog addline";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length < 2) {
            sender.sendMessage(
                    "Usage : /rpg dialog addline <key> <text>"
            );

            return false;
        }

        String key =
                args[0];

        StringBuilder text =
                new StringBuilder();

        for (int i = 1; i < args.length; i++) {

            if (i > 1) {
                text.append(" ");
            }

            text.append(args[i]);
        }

        CommandResult result =
                dialogueService.addLine(
                        key,
                        text.toString()
                );

        sender.sendMessage(
                result.getMessage()
        );

        return result.isSuccess();
    }
}