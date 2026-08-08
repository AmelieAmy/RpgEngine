package fr.doryamy.rpgengine.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Point d'entrée Bukkit de la commande /rpg.
 *
 * Cette classe ne contient aucune logique métier.
 * Elle transmet simplement les arguments
 * au CommandManager.
 */
public final class RpgCommand
        implements CommandExecutor {

    private final CommandManager commandManager;

    public RpgCommand(
            CommandManager commandManager
    ) {
        this.commandManager =
                commandManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        return commandManager.execute(
                sender,
                args
        );
    }
}