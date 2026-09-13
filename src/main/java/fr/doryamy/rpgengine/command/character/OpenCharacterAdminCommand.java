package fr.doryamy.rpgengine.command.character;

import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.character.CharacterAdminController;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

/** Ouvre la bibliothèque globale de personnages. */
public final class OpenCharacterAdminCommand implements RpgSubcommand {

    private final CharacterAdminController controller;

    public OpenCharacterAdminCommand(CharacterAdminController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    @Override
    public String getPath() {
        return "character";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Cette commande doit être exécutée par un joueur.");
            return false;
        }

        if (args.length != 0) {
            sender.sendMessage("Usage : /rpg character");
            return false;
        }

        controller.openAdmin(player.getUniqueId());
        return true;
    }
}
