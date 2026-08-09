package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Affiche la liste des dialogues disponibles.
 *
 * Syntaxe :
 * /rpg dialog list
 */
public final class ListDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    /**
     * Construit la commande.
     *
     * @param dialogueService service des dialogues
     */
    public ListDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog list";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 0) {
            sender.sendMessage(
                    "Usage : /rpg dialog list"
            );
            return false;
        }

        List<Dialogue> dialogues =
                dialogueService.findAll();

        if (dialogues.isEmpty()) {
            sender.sendMessage(
                    "Aucun dialogue disponible."
            );

            return true;
        }

        sender.sendMessage(
                "Dialogues disponibles :"
        );

        for (Dialogue dialogue : dialogues) {

            sender.sendMessage(
                    "- "
                            + dialogue.getKey()
                            + " : "
                            + dialogue.getName()
            );
        }

        return true;
    }
}