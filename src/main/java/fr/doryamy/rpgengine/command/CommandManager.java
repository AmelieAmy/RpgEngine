package fr.doryamy.rpgengine.command;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Orchestre les sous-commandes de RPGEngine.
 *
 * Les commandes sont enregistrées dynamiquement
 * à partir de leur chemin.
 *
 * Le manager ne contient aucune logique métier
 * propre aux fonctionnalités administrées.
 */
public final class CommandManager {

    private final Map<String, RpgSubcommand> commands =
            new HashMap<>();

    /**
     * Enregistre une sous-commande.
     *
     * Si une commande existe déjà sous le même chemin,
     * elle est remplacée.
     *
     * @param command commande à enregistrer
     */
    public void register(RpgSubcommand command) {
        commands.put(
                normalize(command.getPath()),
                command
        );
    }

    /**
     * Recherche puis exécute la sous-commande
     * correspondant aux arguments reçus.
     *
     * @param sender expéditeur
     * @param args arguments transmis après /rpg
     *
     * @return true si une commande a été trouvée
     */
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length == 0) {
            sender.sendMessage(
                    "Usage : /rpg <commande>"
            );
            return false;
        }

        /*
         * Recherche du chemin le plus spécifique.
         *
         * Exemple :
         *
         * "dialog create" est testé avant "dialog".
         */
        String matchingPath =
                commands.keySet()
                        .stream()
                        .filter(path ->
                                matches(
                                        path,
                                        args
                                )
                        )
                        .max(
                                Comparator.comparingInt(
                                        path ->
                                                path.split(" ").length
                                )
                        )
                        .orElse(null);

        if (matchingPath == null) {
            sender.sendMessage(
                    "Commande RPG inconnue."
            );
            return false;
        }

        RpgSubcommand command =
                commands.get(matchingPath);

        int consumedArguments =
                matchingPath.split(" ").length;

        String[] remainingArguments =
                Arrays.copyOfRange(
                        args,
                        consumedArguments,
                        args.length
                );

        return command.execute(
                sender,
                remainingArguments
        );
    }

    private boolean matches(
            String path,
            String[] args
    ) {
        String[] parts =
                path.split(" ");

        if (args.length < parts.length) {
            return false;
        }

        for (int i = 0; i < parts.length; i++) {

            if (!parts[i].equalsIgnoreCase(args[i])) {
                return false;
            }
        }

        return true;
    }

    private String normalize(String path) {
        return path
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", " ");
    }
}