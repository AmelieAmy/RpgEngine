package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueRunner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sélectionne un choix dans le dialogue
 * actuellement actif du joueur.
 *
 * Syntaxe :
 * /rpg dialog choose <position>
 */
public final class ChooseDialogueCommand
        implements RpgSubcommand {

    private final DialogueRunner dialogueRunner;

    public ChooseDialogueCommand(
            DialogueRunner dialogueRunner
    ) {
        this.dialogueRunner =
                dialogueRunner;
    }

    @Override
    public String getPath() {
        return "dialog choose";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {

            sender.sendMessage(
                    "Cette commande doit être exécutée par un joueur."
            );

            return false;
        }

        if (args.length != 1) {

            sender.sendMessage(
                    "Usage : /rpg dialog choose <position>"
            );

            return false;
        }

        int position;

        try {
            position =
                    Integer.parseInt(
                            args[0]
                    );

        } catch (NumberFormatException e) {

            sender.sendMessage(
                    "La position doit être un nombre."
            );

            return false;
        }

        boolean success =
                dialogueRunner.choose(
                        player.getUniqueId(),
                        position
                );

        if (!success) {

            sender.sendMessage(
                    "Aucun choix correspondant n'est disponible."
            );

            return false;
        }

        return true;
    }
}