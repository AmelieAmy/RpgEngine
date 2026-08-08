package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueLine;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

import java.util.Optional;

/**
 * Affiche les informations détaillées d'un dialogue.
 *
 * Syntaxe :
 *
 * /rpg dialog info <key>
 */
public final class InfoDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    /**
     * Construit la commande.
     *
     * @param dialogueService service des dialogues
     */
    public InfoDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog info";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 1) {
            sender.sendMessage(
                    "Usage : /rpg dialog info <key>"
            );

            return false;
        }

        String key = args[0];

        Optional<Dialogue> result =
                dialogueService.find(key);

        if (result.isEmpty()) {
            sender.sendMessage(
                    "Dialogue introuvable : "
                            + key
            );

            return false;
        }

        Dialogue dialogue =
                result.get();

        sender.sendMessage(
                "------------------------------"
        );

        sender.sendMessage(
                "Dialogue : "
                        + dialogue.getKey()
        );

        sender.sendMessage(
                "Nom : "
                        + dialogue.getName()
        );

        sender.sendMessage("");

        if (dialogue.getLines().isEmpty()) {

            sender.sendMessage(
                    "Aucune ligne."
            );

        } else {

            sender.sendMessage(
                    "Lignes :"
            );

            for (DialogueLine line :
                    dialogue.getLines()) {

                sender.sendMessage(
                        line.getPosition()
                                + " | "
                                + line.getText()
                );
            }
        }

        sender.sendMessage("");

        sender.sendMessage(
                "Total : "
                        + dialogue.getLines().size()
                        + " ligne(s)"
        );

        sender.sendMessage(
                "------------------------------"
        );

        return true;
    }
}