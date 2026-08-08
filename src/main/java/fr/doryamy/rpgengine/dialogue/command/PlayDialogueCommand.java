package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueRunner;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Joue un dialogue pour le joueur ayant exécuté la commande.
 *
 * Syntaxe :
 * /rpg dialog play <key>
 */
public final class PlayDialogueCommand implements RpgSubcommand {

    private final DialogueService dialogueService;
    private final DialogueRunner dialogueRunner;

    public PlayDialogueCommand(
            DialogueService dialogueService,
            DialogueRunner dialogueRunner
    ) {
        this.dialogueService = dialogueService;
        this.dialogueRunner = dialogueRunner;
    }

    @Override
    public String getPath() {
        return "dialog play";
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
                    "Usage : /rpg dialog play <key>"
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

        dialogueRunner.play(
                player,
                result.get()
        );

        return true;
    }
}